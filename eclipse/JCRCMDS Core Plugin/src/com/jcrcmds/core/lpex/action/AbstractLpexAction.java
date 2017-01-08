/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.lpex.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.lpex.core.LpexAction;
import com.ibm.lpex.core.LpexDocumentLocation;
import com.ibm.lpex.core.LpexView;
import com.jcrcmds.base.rse.shared.connection.ConnectionHelper;

public abstract class AbstractLpexAction implements LpexAction {

    private LpexView view;

    public void setView(LpexView view) {
        this.view = view;
    }

    public boolean available(LpexView view) {
        setView(view);
        return (view.elements() != 0) && (!view.queryOn("readonly")); //$NON-NLS-1$
    }

    public String[] getContent() {

        List<String> sourceLines = new LinkedList<String>();

        int numElements = view.elements();
        for (int i = 1; i <= numElements; i++) {
            sourceLines.add(view.elementText(i));
        }

        return sourceLines.toArray(new String[sourceLines.size()]);
    }

    public void setContent(String[] sourceLines) {
        replaceRange(sourceLines, 1, view.elements());
    }

    public int getCount() {
        return view.elements();
    }

    public String[] getSelectedSourceLines() {

        int topLine = getFirstSelected();
        int bottomLine = getLastSelected();

        if (getCountSelected() <= 0) {
            return new String[0];
        }

        List<String> sourceLines = new LinkedList<String>();

        for (int i = topLine; i <= bottomLine; i++) {
            sourceLines.add(view.elementText(i));
        }

        return sourceLines.toArray(new String[sourceLines.size()]);
    }

    public void replaceSelectedSourceLines(String[] sourceLines) {
        replaceRange(sourceLines, getFirstSelected(), getLastSelected());
    }

    private void replaceRange(String[] sourceLines, int firstLine, int lastLine) {

        int i = 0;

        int lineNbr = firstLine;

        // Replace source lines
        while (lineNbr <= lastLine && i < sourceLines.length) {
            // view.doCommand("locate element " + lineNbr);
            view.doCommand(new LpexDocumentLocation(lineNbr, 0), "replaceText " + sourceLines[i]); //$NON-NLS-1$
            lineNbr++;
            i++;
        }

        // Remove additional source lines
        if (lineNbr <= lastLine) {
            view.doCommand(new LpexDocumentLocation(lineNbr, 0), "delete " + (lastLine - lineNbr + 1)); //$NON-NLS-1$
        }

        // Insert additional source lines
        lineNbr--;
        while (i < sourceLines.length) {
            view.doCommand(new LpexDocumentLocation(lineNbr, 0), "insert " + sourceLines[i]); //$NON-NLS-1$
            lineNbr++;
            i++;
        }
    }

    public int getCountSelected() {

        if (getLastSelected() <= 0 || getFirstSelected() <= 0) {
            return 0;
        }

        int countSelected = getLastSelected() - getFirstSelected();
        countSelected++;

        return countSelected;
    }

    public int getFirstSelected() {
        return view.queryInt("block.topElement"); //$NON-NLS-1$
    }

    public int getLastSelected() {

        int lastSelected = view.queryInt("block.bottomElement"); //$NON-NLS-1$
        int bottomPosition = view.queryInt("block.bottomPosition"); //$NON-NLS-1$

        if (bottomPosition <= 1) {
            lastSelected--;
        }

        return lastSelected;
    }

    protected Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    protected IEditorPart getActiveEditor() {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage activePage = window.getActivePage();
            if (activePage != null) {
                return activePage.getActiveEditor();
            }
        }

        return null;
    }

    protected String getConnectionName(IEditorPart editor) {
        String connectionName = ConnectionHelper.getConnectionName(editor);
        if (connectionName == null) {
            return null;
        }
        return connectionName;
    }
}
