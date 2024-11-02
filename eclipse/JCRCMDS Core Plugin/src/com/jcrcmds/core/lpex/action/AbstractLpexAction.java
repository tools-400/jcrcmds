/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.lpex.action;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.lpex.core.LpexAction;
import com.ibm.lpex.core.LpexDocumentLocation;
import com.ibm.lpex.core.LpexView;
import com.jcrcmds.base.rse.shared.connection.ConnectionHelper;
import com.jcrcmds.core.Messages;
import com.jcrcmds.core.runtime.monitor.JCRSubMonitor;

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

    public void setContent(String[] sourceLines, IProgressMonitor monitor) {
        replaceRange(sourceLines, 1, view.elements(), monitor);
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

    public void replaceSelectedSourceLines(String[] sourceLines, IProgressMonitor monitor) {
        replaceRange(sourceLines, getFirstSelected(), getLastSelected(), monitor);
    }

    private void replaceRange(String[] sourceLines, int firstLine, int lastLine, IProgressMonitor monitor) {

        int currentElement = view.currentElement();
        int currentPosition = view.currentPosition();

        JCRSubMonitor subMonitor = null;
        if (monitor != null) {
            subMonitor = JCRSubMonitor.convert(monitor, sourceLines.length);
            subMonitor.setTaskName(Messages.Monitor_Replacing_source_lines);
        }

        long lastUpdateTime = Calendar.getInstance().getTimeInMillis();
        int numberOfLinesReplaced = 0;
        int totalNumberOfLinesReplaced = 0;
        int totalNumberOfLinesToReplace = sourceLines.length;

        int lineNbr = firstLine;

        // Replace source lines
        while (lineNbr <= lastLine && totalNumberOfLinesReplaced < totalNumberOfLinesToReplace) {
            // view.doCommand("locate element " + lineNbr);
            view.doCommand(new LpexDocumentLocation(lineNbr, 0), "replaceText " + sourceLines[totalNumberOfLinesReplaced]); //$NON-NLS-1$
            lineNbr++;
            totalNumberOfLinesReplaced++;
            numberOfLinesReplaced++;
            if (mustUpdateMonitor(lastUpdateTime, numberOfLinesReplaced)) {
                lastUpdateTime = updateProgressMonitor(subMonitor, numberOfLinesReplaced, totalNumberOfLinesReplaced, totalNumberOfLinesToReplace);
                numberOfLinesReplaced = 0;
            }
        }

        // Remove additional source lines
        if (lineNbr <= lastLine) {
            view.doCommand(new LpexDocumentLocation(lineNbr, 0), "delete " + (lastLine - lineNbr + 1)); //$NON-NLS-1$
        }

        // Insert additional source lines
        lineNbr--;
        while (totalNumberOfLinesReplaced < totalNumberOfLinesToReplace) {
            view.doCommand(new LpexDocumentLocation(lineNbr, 0), "insert " + sourceLines[totalNumberOfLinesReplaced]); //$NON-NLS-1$
            lineNbr++;
            totalNumberOfLinesReplaced++;
            numberOfLinesReplaced++;
            if (mustUpdateMonitor(lastUpdateTime, numberOfLinesReplaced)) {
                lastUpdateTime = updateProgressMonitor(subMonitor, numberOfLinesReplaced, totalNumberOfLinesReplaced, totalNumberOfLinesToReplace);
                numberOfLinesReplaced = 0;
            }
        }

        updateProgressMonitor(subMonitor, numberOfLinesReplaced, totalNumberOfLinesReplaced, totalNumberOfLinesToReplace);

        if (currentElement <= view.elements()) {
            view.doCommand("locate element " + currentElement); //$NON-NLS-1$
        } else {
            view.doCommand("locate element " + view.elements()); //$NON-NLS-1$
        }
        view.doCommand("set position " + currentPosition); //$NON-NLS-1$

        view.doCommand("screenShow"); //$NON-NLS-1$
    }

    private long updateProgressMonitor(JCRSubMonitor subMonitor, int numberOfLinesReplaced, int totalNumberOfLinesReplaced,
        int totalNumberOfLinesToReplace) {

        if (subMonitor != null) {
            subMonitor.newChild(numberOfLinesReplaced);
            subMonitor.setTaskName(Messages.bind(Messages.Monitor_Replacing_source_lines_A_of_B, Integer.valueOf(totalNumberOfLinesReplaced),
                Integer.valueOf(totalNumberOfLinesToReplace)));
        }

        return Calendar.getInstance().getTimeInMillis();
    }

    private boolean mustUpdateMonitor(long lastUpdateTime, int numberOfLinesReplaced) {

        if (Calendar.getInstance().getTimeInMillis() - lastUpdateTime >= 3000 || numberOfLinesReplaced >= 3) {
            return true;
        }

        return false;
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
