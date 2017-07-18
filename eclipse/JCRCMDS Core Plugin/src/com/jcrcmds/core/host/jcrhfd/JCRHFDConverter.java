/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.host.jcrhfd;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.UIJob;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.FieldDescription;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;
import com.jcrcmds.base.shared.exceptions.CommandFailureException;
import com.jcrcmds.base.shared.exceptions.DataTruncationException;
import com.jcrcmds.base.shared.exceptions.LibraryNotFoundException;
import com.jcrcmds.base.shared.helper.ExceptionHelper;
import com.jcrcmds.base.shared.logger.Logger;
import com.jcrcmds.core.Messages;
import com.jcrcmds.core.preferences.Preferences;
import com.jcrcmds.core.runtime.monitor.JCRSubMonitor;
import com.jcrcmds.core.ui.MessageDialogAsync;

public class JCRHFDConverter {

    /** Record field */
    public static final int SRCSEQ_INDEX = 0;
    /** Record field */
    public static final int SRCDAT_INDEX = 1;
    /** Record field */
    public static final int SRCDTA_INDEX = 2;

    private AS400 system;

    public JCRHFDConverter(AS400 system) {
        this.system = system;
    }

    public void convertAsync(final String[] sourceLines, final String memberType, final int recordLength, final IResultReceiver receiver)
        throws Exception {

        IRunnableWithProgress job = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                doConvert(monitor);
            }

            private IStatus doConvert(IProgressMonitor monitor) {

                try {
                    final JCRSubMonitor subMonitor = JCRSubMonitor.convert(monitor, 100);
                    final String[] result = convert(sourceLines, memberType, recordLength, subMonitor);
                    UIJob job = new UIJob(Messages.Monitor_Replacing_source_lines) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor2) {
                            JCRSubMonitor subMonitor = JCRSubMonitor.convert(monitor2, 100);
                            receiver.setResult(result, subMonitor);
                            subMonitor.newChild(0);
                            return Status.OK_STATUS;
                        }
                    };
                    job.schedule();
                } catch (Exception e) {
                    MessageDialogAsync.displayError(Messages.E_R_R_O_R, ExceptionHelper.getLocalizedMessage(e));
                }

                return Status.OK_STATUS;
            }
        };

        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.busyCursorWhile(job);
    }

    private String[] convert(String[] sourceLines, String memberType, int requiredRecordLength, IProgressMonitor monitor) throws Exception {

        JCRSubMonitor subMonitor = JCRSubMonitor.convert(monitor, 100);

        Preferences preferences = Preferences.getInstance();
        String workLibrary = preferences.getWorkLibrary();

        String workFile = preferences.getWorkFile();
        String workMember1 = preferences.getWorkMember() + "1"; //$NON-NLS-1$
        String workMember2 = preferences.getWorkMember() + "2"; //$NON-NLS-1$

        int defaultRecordLength = Preferences.getInstance().getWorkFileRecordLength();
        if (requiredRecordLength < defaultRecordLength) {
            requiredRecordLength = defaultRecordLength;
        }

        // Check preconditions
        subMonitor.newChild(25);
        subMonitor.setTaskName(Messages.Monitor_Checking_preconditions);

        SequentialFile remoteInputWorkFile = new SequentialFile(system, new QSYSObjectPathName(workLibrary, workFile, workMember1, "MBR").getPath());

        if (!checkLibrary(remoteInputWorkFile)) {
            throw new LibraryNotFoundException(getLibraryName(remoteInputWorkFile));
        }

        if (!checkFile(remoteInputWorkFile)) {
            createFile(remoteInputWorkFile, requiredRecordLength);
        } else {
            checkRecordLength(remoteInputWorkFile, requiredRecordLength);
        }

        if (!checkFileMember(remoteInputWorkFile)) {
            createFileMember(remoteInputWorkFile, workMember1);
        }

        initializeFileMember(remoteInputWorkFile, workMember1, memberType, "JCR input data"); //$NON-NLS-1$

        SequentialFile remoteOutputWorkFile = new SequentialFile(system, new QSYSObjectPathName(workLibrary, workFile, workMember2, "MBR").getPath());

        if (!checkLibrary(remoteOutputWorkFile)) {
            throw new LibraryNotFoundException(getLibraryName(remoteOutputWorkFile));
        }

        if (!checkFile(remoteOutputWorkFile)) {
            createFile(remoteOutputWorkFile, requiredRecordLength);
        } else {
            checkRecordLength(remoteOutputWorkFile, requiredRecordLength);
        }

        if (!checkFileMember(remoteOutputWorkFile)) {
            createFileMember(remoteOutputWorkFile, workMember2);
        }

        initializeFileMember(remoteOutputWorkFile, workMember2, memberType, "JCR output data"); //$NON-NLS-1$

        /*
         * Upload selected source lines
         */
        subMonitor.newChild(25);
        subMonitor.setTaskName(Messages.Monitor_Uploading_source_lines_to_host);
        upload(remoteInputWorkFile, sourceLines);

        /*
         * Add JCR product library
         */
        boolean isProductLibrary = addProductLibrary(remoteInputWorkFile, preferences.getProductLibrary(), "*FIRST"); //$NON-NLS-1$

        /*
         * Call JCRHFD
         */
        try {

            subMonitor.newChild(25);
            subMonitor.setTaskName(Messages.Monitor_Converting_source_lines);
            String jcrHfdCommand = preferences.getProductLibrary() + "/" + preferences.getJcrHfdCommand(); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IL", workLibrary); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IF", workFile); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IM", workMember1); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OL", workLibrary); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OF", workFile); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OM", workMember2); //$NON-NLS-1$
            executeCommand(remoteInputWorkFile, jcrHfdCommand);

        } finally {

            /*
             * Remove JCR product library
             */
            if (isProductLibrary) {
                removeProductLibrary(remoteInputWorkFile, preferences.getProductLibrary());
            }
        }

        /*
         * Read converted data
         */
        subMonitor.newChild(25);
        subMonitor.setTaskName(Messages.Monitor_Downloading_source_lines_from_host);
        String[] convertedSourceLines = download(remoteOutputWorkFile);

        subMonitor.newChild(0);
        return convertedSourceLines;
    }

    private boolean addProductLibrary(AS400File as400File, String library, String position) {

        String command = "ADDLIBLE LIB(" + library + ") POSITION(" + position + ")";

        try {
            AS400Message[] messages = as400File.runCommand(command);
            for (AS400Message message : messages) {
                if ("CPC2196".equals(message.getID())) { // Library added
                    return true;
                }
            }
            return false;
        } catch (AS400SecurityException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean removeProductLibrary(AS400File as400File, String library) {

        String command = "RMVLIBLE LIB(" + library + ")";

        try {
            AS400Message[] messages = as400File.runCommand(command);
            for (AS400Message message : messages) {
                if ("CPC2197".equals(message.getID())) { // Library added
                    return true;
                }
            }
            return false;
        } catch (AS400SecurityException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private void initializeFileMember(AS400File as400File, String member, String memberType, String text) throws IOException, InterruptedException,
        AS400SecurityException, CommandFailureException {

        // Clear file member
        // Monitor message ID "CPC3101" - Member cleared
        executeCommand(as400File, getCommandClearFileMember(getLibraryName(as400File), as400File.getFileName(), member), "CPC3101");

        // Change source type of file member
        // Monitor message ID "CPC3201" - Member changed
        executeCommand(as400File,
            getCommandChangeSourceMemberTypeAndText(getLibraryName(as400File), as400File.getFileName(), member, memberType, text), "CPC3201");
    }

    private void createFile(AS400File as400File, int recordLength) throws IOException, InterruptedException, AS400SecurityException,
        CommandFailureException {

        // Monitor message ID "CPC7301" - File created
        executeCommand(as400File, getCommandCrtSrcPf(getLibraryName(as400File), as400File.getFileName(), recordLength), "CPC7301");
    }

    private void createFileMember(AS400File as400File, String member) throws Exception {

        as400File.addPhysicalFileMember(member, ""); //$NON-NLS-1$
    }

    private void checkRecordLength(AS400File as400File, int requiredRecordLength) throws AS400Exception, AS400SecurityException,
        InterruptedException, IOException, DataTruncationException {

        int actualRecordLength = getRecordLength(as400File);
        if (actualRecordLength < requiredRecordLength) {
            throw new DataTruncationException(getLibraryName(as400File), as400File.getFileName(), actualRecordLength, requiredRecordLength);
        }
    }

    private boolean checkFileMember(AS400File as400File) {

        try {

            executeCommand(as400File, getCommandCheckFileMember(getLibraryName(as400File), as400File.getFileName(), as400File.getMemberName()));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkFile(AS400File as400File) {

        try {

            executeCommand(as400File, getCommandCheckFile(getLibraryName(as400File), as400File.getFileName()));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkLibrary(AS400File as400File) {

        try {

            executeCommand(as400File, getCommandCheckLibrary(getLibraryName(as400File)));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private void executeCommand(AS400File as400File, String command, String... monitoredMessageIDs) throws IOException, InterruptedException,
        AS400SecurityException, CommandFailureException {

        boolean isError = false;

        AS400Message[] messages = as400File.runCommand(command);
        if (messages == null || messages.length == 0 || messages[0].getID() == null || messages[0].getID().trim().length() == 0) {
            return;
        }

        StringBuilder buffer = new StringBuilder();
        for (AS400Message message : messages) {
            if (!isMonitoredMessage(message.getID(), monitoredMessageIDs)) {
                isError = true;
                if (buffer.length() > 0) {
                    buffer.append(" ");
                }
                buffer.append(message.getID());
                buffer.append(": ");
                buffer.append(message.getText());
            }
        }

        if (isError) {
            throw new CommandFailureException(command, buffer.toString());
        }
    }

    private boolean isMonitoredMessage(String messageID, String[] monitoredMessageIDs) {

        for (String monitoredMessageID : monitoredMessageIDs) {
            if (messageID.equalsIgnoreCase(monitoredMessageID)) {
                return true;
            }
        }

        return false;
    }

    private String getCommandCrtSrcPf(String library, String file, int recordLength) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("CRTSRCPF FILE(");
        cmd.append(library);
        cmd.append("/");
        cmd.append(file);
        cmd.append(") RCDLEN(");
        cmd.append(recordLength);
        cmd.append(") TEXT('JCR work file')");

        return cmd.toString();
    }

    private String getCommandChangeSourceMemberTypeAndText(String library, String file, String member, String srcType, String text) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("CHGPFM FILE(");
        cmd.append(library);
        cmd.append("/");
        cmd.append(file);
        cmd.append(") MBR(");
        cmd.append(member);
        cmd.append(") SRCTYPE(");
        cmd.append(srcType);
        cmd.append(") TEXT('");
        cmd.append(text);
        cmd.append("')");

        return cmd.toString();
    }

    private String getCommandClearFileMember(String library, String file, String member) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("CLRPFM FILE(");
        cmd.append(library);
        cmd.append("/");
        cmd.append(file);
        cmd.append(") MBR(");
        cmd.append(member);
        cmd.append(")");

        return cmd.toString();
    }

    private String getCommandCheckFileMember(String library, String file, String member) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("CHKOBJ OBJ(");
        cmd.append(library);
        cmd.append("/");
        cmd.append(file);
        cmd.append(") OBJTYPE(*FILE) MBR(");
        cmd.append(member);
        cmd.append(")");

        return cmd.toString();
    }

    private String getCommandCheckFile(String library, String file) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("CHKOBJ OBJ(");
        cmd.append(library);
        cmd.append("/");
        cmd.append(file);
        cmd.append(") OBJTYPE(*FILE)");

        return cmd.toString();
    }

    private String getCommandCheckLibrary(String workLibrary) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("CHKOBJ OBJ(");
        cmd.append(workLibrary);
        cmd.append(") OBJTYPE(*LIB)");

        return cmd.toString();
    }

    private int getRecordLength(AS400File as400File) throws AS400Exception, AS400SecurityException, InterruptedException, IOException {

        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(as400File.getSystem(), as400File.getPath());
        RecordFormat[] format = recordDescription.retrieveRecordFormat();
        int recordFormatLength = 0;
        for (int i = 0; i < format[0].getNumberOfFields(); i++) {
            FieldDescription fieldDescription = format[0].getFieldDescription(i);
            recordFormatLength = recordFormatLength + fieldDescription.getLength();
        }

        return recordFormatLength;
    }

    private void upload(AS400File as400File, String[] sourceLines) throws Exception {

        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(as400File.getSystem(), as400File.getPath());

        try {

            RecordFormat[] format = recordDescription.retrieveRecordFormat();

            as400File.setRecordFormat(format[0]);
            as400File.open(AS400File.WRITE_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

            BigDecimal srcSeq = new BigDecimal(0);
            BigDecimal srcInc = new BigDecimal(1);
            Record[] records = new Record[sourceLines.length];

            for (int i = 0; i < sourceLines.length; i++) {
                srcSeq.add(srcInc);
                Record record = new Record(format[0]);
                record.setField(SRCSEQ_INDEX, srcSeq);
                record.setField(SRCDTA_INDEX, sourceLines[i]);
                records[i] = record;
            }

            as400File.write(records);

            as400File.close();

        } finally {
            try {
                as400File.close();
            } catch (Exception e) {
                Logger.logError("*** Could not close file ***", e); //$NON-NLS-1$
            }
        }
    }

    private String[] download(AS400File as400File) throws Exception {

        List<String> sourceLines = new LinkedList<String>();

        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(as400File.getSystem(), as400File.getPath());

        try {

            RecordFormat[] format = recordDescription.retrieveRecordFormat();

            as400File.setRecordFormat(format[0]);
            as400File.open(AS400File.READ_ONLY, 1000, AS400File.COMMIT_LOCK_LEVEL_NONE);

            Record record = as400File.readNext();
            while (record != null) {
                String sourceData = (String)record.getField(SRCDTA_INDEX);
                sourceLines.add(sourceData);
                record = as400File.readNext();
            }

            as400File.close();

        } finally {
            try {
                as400File.close();
            } catch (Exception e) {
                Logger.logError("*** Could not close file ***", e); //$NON-NLS-1$
            }
        }

        return sourceLines.toArray(new String[sourceLines.size()]);
    }

    private String getLibraryName(AS400File as400File) {

        String path = as400File.getPath();
        QSYSObjectPathName pathName = new QSYSObjectPathName(path);

        return pathName.getLibraryName();
    }
}
