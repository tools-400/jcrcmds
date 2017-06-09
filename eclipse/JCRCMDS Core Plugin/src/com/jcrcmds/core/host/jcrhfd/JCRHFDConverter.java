/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.host.jcrhfd;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;
import com.jcrcmds.base.shared.exceptions.CommandFailureException;
import com.jcrcmds.base.shared.exceptions.LibraryNotFoundException;
import com.jcrcmds.base.shared.helper.ExceptionHelper;
import com.jcrcmds.base.shared.logger.Logger;
import com.jcrcmds.core.preferences.Preferences;
import com.jcrcmds.core.ui.MessageDialogAsync;

public class JCRHFDConverter {

    /** Record field */
    public static final int SRCSEQ_INDEX = 0;
    /** Record field */
    public static final int SRCDAT_INDEX = 1;
    /** Record field */
    public static final int SRCDTA_INDEX = 2;

    private String connectionName;
    private AS400 system;

    public JCRHFDConverter(String connectionName, AS400 system) {
        this.connectionName = connectionName;
        this.system = system;
    }

    public void convertAsync(final String[] sourceLines, final String memberType, final IResultReceiver receiver) throws Exception {

        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor arg0) {
                try {
                    String[] result = convert(sourceLines, memberType);
                    receiver.setResult(result);
                } catch (Exception e) {
                    MessageDialogAsync.displayError(ExceptionHelper.getLocalizedMessage(e));
                }
            }
        });
    }

    private String[] convert(String[] sourceLines, String memberType) throws Exception {

        Preferences preferences = Preferences.getInstance();
        String workLibrary = preferences.getWorkLibrary();

        String workFile = preferences.getWorkFile();
        String workMember1 = preferences.getWorkMember() + "1"; //$NON-NLS-1$
        String workMember2 = preferences.getWorkMember() + "2"; //$NON-NLS-1$

        SequentialFile remoteInputWorkFile = new SequentialFile(system, new QSYSObjectPathName(workLibrary, workFile, workMember1, "MBR").getPath());

        if (!checkLibrary(remoteInputWorkFile)) {
            throw new LibraryNotFoundException(remoteInputWorkFile.getLibraryName());
        }

        if (!checkFile(remoteInputWorkFile)) {
            createFile(remoteInputWorkFile);
        }

        if (!checkFileMember(remoteInputWorkFile)) {
            createFileMember(remoteInputWorkFile, workMember1, "JCR input data");
        }

        initializeFileMember(remoteInputWorkFile, workMember1, memberType);

        SequentialFile remoteOutputWorkFile = new SequentialFile(system, new QSYSObjectPathName(workLibrary, workFile, workMember2, "MBR").getPath());

        if (!checkLibrary(remoteOutputWorkFile)) {
            throw new LibraryNotFoundException(remoteOutputWorkFile.getLibraryName());
        }

        if (!checkFile(remoteOutputWorkFile)) {
            createFile(remoteOutputWorkFile);
        }

        if (!checkFileMember(remoteOutputWorkFile)) {
            createFileMember(remoteOutputWorkFile, workMember2, "JCR output data");
        }

        initializeFileMember(remoteOutputWorkFile, workMember2, memberType);

        /*
         * Upload selected source lines
         */
        upload(remoteInputWorkFile, sourceLines);

        /*
         * Call JCRHFD
         */
        try {

            String jcrHfdCommand = preferences.getProductLibrary() + "/" + preferences.getJcrHfdCommand(); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IL", workLibrary); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IF", workFile); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IM", workMember1); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OL", workLibrary); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OF", workFile); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OM", workMember2); //$NON-NLS-1$
            executeCommand(remoteInputWorkFile, jcrHfdCommand);

        } catch (Exception e) {
            MessageDialogAsync.displayError(ExceptionHelper.getLocalizedMessage(e));
        }

        /*
         * Read converted data
         */
        String[] convertedSourceLines = download(remoteOutputWorkFile);

        return convertedSourceLines;
    }

    private void initializeFileMember(AS400File as400File, String member, String memberType) throws IOException, InterruptedException,
        AS400SecurityException, CommandFailureException {

        // Clear file member
        // Monitor message ID "CPC3101" - Member cleared
        executeCommand(as400File, getCommandClearFileMember(as400File.getLibraryName(), as400File.getFileName(), member), "CPC3101");

        // Change source type of file member
        // Monitor message ID "CPC3201" - Member changed
        executeCommand(as400File, getCommandChangeSourceType(as400File.getLibraryName(), as400File.getFileName(), member, memberType), "CPC3201");
    }

    private void createFile(AS400File as400File) throws IOException, InterruptedException, AS400SecurityException, CommandFailureException {

        int workFileRecordLength = Preferences.getInstance().getWorkFileRecordLength();
        // Monitor message ID "CPC7301" - File created
        executeCommand(as400File, getCommandCrtSrcPf(as400File.getLibraryName(), as400File.getFileName(), workFileRecordLength), "CPC7301");
    }

    private void createFileMember(AS400File as400File, String member, String text) throws Exception {

        as400File.addPhysicalFileMember(member, text);

        // QSYSObjectPathName pathName = new
        // QSYSObjectPathName(as400File.getPath());
        // pathName.setMemberName(member);
        //
        // as400File.setPath(pathName.getPath());
    }

    private boolean checkFileMember(AS400File as400File) {

        try {

            executeCommand(as400File, getCommandCheckFileMember(as400File.getLibraryName(), as400File.getFileName(), as400File.getMemberName()));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkFile(AS400File as400File) {

        try {

            executeCommand(as400File, getCommandCheckFile(as400File.getLibraryName(), as400File.getFileName()));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkLibrary(AS400File as400File) {

        try {

            executeCommand(as400File, getCommandCheckLibrary(as400File.getLibraryName()));
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

    private String getCommandChangeSourceType(String library, String file, String member, String srcType) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("CHGPFM FILE(");
        cmd.append(library);
        cmd.append("/");
        cmd.append(file);
        cmd.append(") MBR(");
        cmd.append(member);
        cmd.append(") SRCTYPE(");
        cmd.append(srcType);
        cmd.append(")");

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

    public void upload(AS400File sourceFile, String[] sourceLines) throws Exception {

        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(sourceFile.getSystem(), sourceFile.getPath());

        try {

            RecordFormat[] format = recordDescription.retrieveRecordFormat();

            sourceFile.setRecordFormat(format[0]);
            sourceFile.open(AS400File.WRITE_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

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

            sourceFile.write(records);

            sourceFile.close();

        } finally {
            try {
                sourceFile.close();
            } catch (Exception e) {
                Logger.logError("*** Could not close file ***", e); //$NON-NLS-1$
            }
        }
    }

    public String[] download(AS400File sourceFile) throws Exception {

        List<String> sourceLines = new LinkedList<String>();

        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(sourceFile.getSystem(), sourceFile.getPath());

        try {

            RecordFormat[] format = recordDescription.retrieveRecordFormat();

            sourceFile.setRecordFormat(format[0]);
            sourceFile.open(AS400File.READ_ONLY, 1000, AS400File.COMMIT_LOCK_LEVEL_NONE);

            Record record = sourceFile.readNext();
            while (record != null) {
                String sourceData = (String)record.getField(SRCDTA_INDEX);
                sourceLines.add(sourceData);
                record = sourceFile.readNext();
            }

            sourceFile.close();

        } finally {
            try {
                sourceFile.close();
            } catch (Exception e) {
                Logger.logError("*** Could not close file ***", e); //$NON-NLS-1$
            }
        }

        return sourceLines.toArray(new String[sourceLines.size()]);
    }
}
