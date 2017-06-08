/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.host.jcrhfd;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.ibm.as400.access.AS400;
import com.jcrcmds.base.shared.helper.ExceptionHelper;
import com.jcrcmds.base.shared.host.CLCommand;
import com.jcrcmds.base.shared.logger.Logger;
import com.jcrcmds.core.Messages;
import com.jcrcmds.core.host.RemoteFile;
import com.jcrcmds.core.host.RemoteLibrary;
import com.jcrcmds.core.host.RemoteSourceMember;
import com.jcrcmds.core.preferences.Preferences;
import com.jcrcmds.core.ui.MessageDialogAsync;

public class JCRHFDConverter {

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

        CLCommand clCommand = new CLCommand(system);

        String currentLibrary = null;
        String[] convertedSourceLines = new String[0];

        try {
            currentLibrary = clCommand.getCurrentLibrary();
        } catch (Exception e) {
            Logger.logError("*** Could not retrieve current library ***", e); //$NON-NLS-1$
            MessageDialogAsync.displayError(Messages.Could_not_retrieve_current_library);
            return null;
        }

        try {
            clCommand.setCurrentLibrary(workLibrary);
        } catch (Exception e) {
            Logger.logError("*** Could not set current library ***", e); //$NON-NLS-1$
            MessageDialogAsync.displayError(Messages.Could_not_set_current_library_to_A);
            return null;
        }

        String workFile = preferences.getWorkFile();
        String workMember1 = preferences.getWorkMember() + "1"; //$NON-NLS-1$
        String workMember2 = preferences.getWorkMember() + "2"; //$NON-NLS-1$
        int workFileRecordLength = preferences.getWorkFileRecordLength();

        try {

            /*
             * Prepare work members
             */

            RemoteLibrary library = new RemoteLibrary(connectionName, workLibrary);

            if (!library.exists()) {
                MessageDialogAsync.displayError(Messages.bind(Messages.Library_A_not_found, workLibrary));
                return null;
            }

            RemoteFile sourceFile = new RemoteFile(connectionName, workLibrary, workFile);

            if (!sourceFile.exists()) {
                sourceFile.setDescription("JCRCMDS Work File"); //$NON-NLS-1$
                String message = sourceFile.create(workFileRecordLength);
                if (message != null) {
                    MessageDialogAsync.displayError(Messages.bind(Messages.Could_not_create_source_file_B_in_library_A_Reason_C, new String[] {
                        workLibrary, workFile, message }));
                    return null;
                }
            }

            RemoteSourceMember sourceMember1 = new RemoteSourceMember(connectionName, workLibrary, workFile, workMember1);

            if (!sourceMember1.exists()) {
                String message = sourceMember1.create();
                if (message != null) {
                    MessageDialogAsync.displayError(Messages.bind(Messages.Could_not_create_source_member_C_in_file_B_in_library_A_Reason_D,
                        new String[] { workLibrary, workFile, workMember1, message }));
                    return null;
                }
            } else {
                String message = sourceMember1.clear();
                if (message != null) {
                    MessageDialogAsync.displayError(Messages.bind(Messages.Could_not_clear_source_member_C_in_file_B_in_library_A_Reason_D,
                        new String[] { workLibrary, workFile, workMember1, message }));
                    return null;
                }
            }

            sourceMember1.setTypeAndDescription(memberType, "JCRHFD Input Data"); //$NON-NLS-1$

            RemoteSourceMember sourceMember2 = new RemoteSourceMember(connectionName, workLibrary, workFile, workMember2);

            if (!sourceMember2.exists()) {
                String message = sourceMember2.create();
                if (message != null) {
                    MessageDialogAsync.displayError(Messages.bind(Messages.Could_not_create_source_member_C_in_file_B_in_library_A_Reason_D,
                        new String[] { workLibrary, workFile, workMember2, message }));
                    return null;
                }
            } else {
                String message = sourceMember2.clear();
                if (message != null) {
                    MessageDialogAsync.displayError(Messages.bind(Messages.Could_not_clear_source_member_C_in_file_B_in_library_A_Reason_D,
                        new String[] { workLibrary, workFile, workMember2, message }));
                    return null;
                }
            }

            String message = sourceMember2.setTypeAndDescription(memberType, "JCRHFD Output Data"); //$NON-NLS-1$
            if (message != null) {
                MessageDialogAsync.displayError(message);
                return null;
            }

            /*
             * Store input data
             */

            sourceMember1.upload(sourceLines);

            /*
             * Call JCRHFD
             */

            String jcrHfdCommand = preferences.getProductLibrary() + "/" + preferences.getJcrHfdCommand(); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IL", workLibrary); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IF", workFile); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&IM", workMember1); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OL", workLibrary); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OF", workFile); //$NON-NLS-1$
            jcrHfdCommand = jcrHfdCommand.replaceAll("&OM", workMember2); //$NON-NLS-1$
            message = clCommand.executeCommand(jcrHfdCommand);
            if (message != null) {
                MessageDialogAsync.displayError(message);
                return null;
            }

            /*
             * Read converted data
             */

            convertedSourceLines = sourceMember2.download();

        } finally {

            try {
                clCommand.setCurrentLibrary(currentLibrary);
            } catch (Exception e) {
                Logger.logError("*** Could not restore current library to: " + currentLibrary + " ***", e); //$NON-NLS-1$ //$NON-NLS-2$
                MessageDialogAsync.displayError(Messages.Could_restore_current_library_to_A);
            }
        }

        return convertedSourceLines;
    }
}
