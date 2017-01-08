/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.lpex.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.as400.access.AS400;
import com.ibm.lpex.core.LpexView;
import com.jcrcmds.base.rse.shared.connection.ConnectionHelper;
import com.jcrcmds.base.shared.helper.ExceptionHelper;
import com.jcrcmds.base.shared.logger.Logger;
import com.jcrcmds.core.Messages;
import com.jcrcmds.core.host.jcrhfd.IResultReceiver;
import com.jcrcmds.core.host.jcrhfd.JCRHFDConverter;

public class ConvertSelectionHFDAction extends AbstractLpexAction implements IResultReceiver {

    public static final String ID = "JCRCMDS.ConvertSelectionHFD"; //$NON-NLS-1$

    public void doAction(LpexView view) {

        setView(view);

        try {

            String[] sourceLines = getSelectedSourceLines();
            if (sourceLines.length == 0) {
                MessageDialog.openInformation(getShell(), Messages.Information, Messages.No_source_lines_selected);
                return;
            }

            IEditorPart editor = getActiveEditor();
            if (editor == null) {
                MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Could_not_get_the_active_editor);
                return;
            }

            String connectionName = getConnectionName(editor);
            if (connectionName == null) {
                MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Could_not_determine_the_name_of_the_remote_connection);
                return;
            }

            AS400 system = ConnectionHelper.getSystem(connectionName);
            if (system == null) {
                MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                    Messages.bind(Messages.Could_not_get_AS400_object_for_connection_A, connectionName));
                return;
            }

            JCRHFDConverter converter = new JCRHFDConverter(connectionName, system);
            converter.convertAsync(sourceLines, getMemberType(editor), this);

        } catch (Throwable e) {
            Logger.logError("*** Unexpected error when attempting to convert selected source line (HFD) ***", e); //$NON-NLS-1$
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, ExceptionHelper.getLocalizedMessage(e));
        }
    }

    public void setResult(String[] sourceLines) {
        if (sourceLines != null) {
            replaceSelectedSourceLines(sourceLines);
        }
    }

    private String getMemberType(IEditorPart editor) {

        IEditorInput editorInput = getActiveEditor().getEditorInput();
        if (editorInput instanceof FileEditorInput) {
            FileEditorInput fileEditorInput = (FileEditorInput)editorInput;
            return fileEditorInput.getFile().getFileExtension();
        }

        return null;
    }

    public static String getLPEXMenuAction() {
        return "\"" + Messages.Menu_Convert_selection_HFD + "\" " + ConvertSelectionHFDAction.ID; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean available(LpexView view) {
        setView(view);
        return (getCountSelected() != 0) && (!view.queryOn("readonly")); //$NON-NLS-1$
    }
}
