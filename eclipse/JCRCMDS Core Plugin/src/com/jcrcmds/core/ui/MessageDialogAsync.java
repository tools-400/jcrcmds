package com.jcrcmds.core.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.jcrcmds.core.Messages;
import com.jcrcmds.core.ui.jobs.MessageDialogUIJob;

public class MessageDialogAsync {

    public static void displayError(Shell shell, String message) {
        int kind = MessageDialog.ERROR;
        MessageDialog dialog = new MessageDialog(shell, Messages.E_R_R_O_R, null, message, kind, getButtonLabels(kind), 0);
        MessageDialogUIJob job = new MessageDialogUIJob(shell.getDisplay(), dialog);
        job.schedule();
    }

    public static void displayError(final String title, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
            }
        });
    }

    public static void displayError(final String message) {
        displayError(Messages.E_R_R_O_R, message);
    }

    /**
     * @param kind
     * @return
     */
    static String[] getButtonLabels(int kind) {
        String[] dialogButtonLabels;
        switch (kind) {
        case MessageDialog.ERROR:
        case MessageDialog.INFORMATION:
        case MessageDialog.WARNING: {
            dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL };
            break;
        }
        case MessageDialog.QUESTION: {
            dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL };
            break;
        }
        default: {
            throw new IllegalArgumentException("Illegal value for kind in MessageDialog.open()"); //$NON-NLS-1$
        }
        }
        return dialogButtonLabels;
    }

}
