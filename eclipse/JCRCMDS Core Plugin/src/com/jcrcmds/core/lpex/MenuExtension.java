/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.lpex;

import java.util.ArrayList;
import java.util.List;

import com.ibm.lpex.core.LpexView;
import com.jcrcmds.core.JcrCmdsCorePlugin;
import com.jcrcmds.core.lpex.action.ConvertHFDAction;
import com.jcrcmds.core.lpex.action.ConvertSelectionHFDAction;

/**
 * This class extends the popup menue of the Lpex editor. It adds the following
 * options:
 * <ul>
 * <li>Convert selection (H,F,D)</li>
 * </ul>
 */
public class MenuExtension {

    private static final String MENU_NAME = "JCRCMDS"; //$NON-NLS-1$
    private static final String BEGIN_SUB_MENU = "beginSubmenu"; //$NON-NLS-1$
    private static final String END_SUB_MENU = "endSubmenu"; //$NON-NLS-1$
    private static final String SEPARATOR = "separator"; //$NON-NLS-1$
    private static final String DOUBLE_QUOTES = "\""; //$NON-NLS-1$
    private static final String SPACE = " "; //$NON-NLS-1$
    private static final String NULL = "null"; //$NON-NLS-1$

    public void initializeLpexEditor() {

        JcrCmdsCorePlugin.getDefault().setLpexMenuExtension(this);

        LpexView.doGlobalCommand("set default.updateProfile.userActions " //$NON-NLS-1$
            + getLPEXEditorUserActions(LpexView.globalQuery("current.updateProfile.userActions"))); //$NON-NLS-1$
        LpexView.doGlobalCommand("set default.updateProfile.userKeyActions " //$NON-NLS-1$
            + getLPEXEditorUserKeyActions(LpexView.globalQuery("current.updateProfile.userKeyActions"))); //$NON-NLS-1$
        LpexView.doGlobalCommand("set default.popup " + getLPEXEditorPopupMenu(LpexView.globalQuery("current.popup"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void uninstall() {

        removeUserActions();
        removeUserKeyActions();
        removePopupMenu();
    }

    private void removeUserActions() {

        StringBuilder existingActions = new StringBuilder(LpexView.globalQuery("current.updateProfile.userActions")); //$NON-NLS-1$
        ArrayList<String> actions = getUserActions();

        int start;
        for (String action : actions) {
            if ((start = existingActions.indexOf(action)) >= 0) {
                int end = start + action.length();
                existingActions.replace(start, end, ""); //$NON-NLS-1$
            }
        }

        LpexView.doGlobalCommand("set default.updateProfile.userActions " + existingActions.toString().trim()); //$NON-NLS-1$
    }

    private void removeUserKeyActions() {

        StringBuilder existingActions = new StringBuilder(LpexView.globalQuery("current.updateProfile.userKeyActions")); //$NON-NLS-1$
        ArrayList<String> actions = getUserKeyActions();

        int start;
        for (String action : actions) {
            if ((start = existingActions.indexOf(action)) >= 0) {
                int end = start + action.length();
                existingActions.replace(start, end, ""); //$NON-NLS-1$
            }
        }

        LpexView.doGlobalCommand("set default.updateProfile.userKeyActions " + existingActions.toString().trim()); //$NON-NLS-1$
    }

    private void removePopupMenu() {

        String popupMenu = LpexView.globalQuery("current.popup"); //$NON-NLS-1$
        popupMenu = removeSubMenu(MENU_NAME, popupMenu);

        LpexView.doGlobalCommand("set default.popup " + popupMenu.trim()); //$NON-NLS-1$
    }

    private String getLPEXEditorUserActions(String existingActions) {

        ArrayList<String> actions = getUserActions();

        StringBuilder newUserActions = new StringBuilder();

        if ((existingActions == null) || (existingActions.equalsIgnoreCase(NULL))) {
            for (String action : actions) {
                newUserActions.append(action + SPACE);
            }
        }

        else {
            newUserActions.append(existingActions + SPACE);
            for (String action : actions) {
                if (existingActions.indexOf(action) < 0) {
                    newUserActions.append(action + SPACE);
                }
            }
        }

        return newUserActions.toString();
    }

    private ArrayList<String> getUserActions() {

        ArrayList<String> actions = new ArrayList<String>();
        actions.add(ConvertHFDAction.ID + SPACE + ConvertHFDAction.class.getName());
        actions.add(ConvertSelectionHFDAction.ID + SPACE + ConvertSelectionHFDAction.class.getName());

        return actions;
    }

    private String getLPEXEditorUserKeyActions(String existingUserKeyActions) {

        ArrayList<String> actions = getUserKeyActions();

        StringBuilder newUserKeyActions = new StringBuilder();

        if ((existingUserKeyActions == null) || (existingUserKeyActions.equalsIgnoreCase(NULL))) {
            for (String action : actions) {
                newUserKeyActions.append(action + SPACE);
            }
        }

        else {
            newUserKeyActions.append(existingUserKeyActions + SPACE);
            for (String action : actions) {
                if (existingUserKeyActions.indexOf(action) < 0) {
                    newUserKeyActions.append(action + SPACE);
                }
            }
        }

        return newUserKeyActions.toString();
    }

    private ArrayList<String> getUserKeyActions() {

        ArrayList<String> actions = new ArrayList<String>();
        // actions.add("c-1" + SPACE + EditHeaderAction.ID);
        // actions.add("c-2" + SPACE + RemoveHeaderAction.ID);

        return actions;
    }

    private StringBuilder appendKeyAction(StringBuilder newKeyActions, String keyAction) {

        if (newKeyActions.indexOf(keyAction) < 0) {
            if (newKeyActions.length() != 0) {
                newKeyActions.append(SPACE);
            }
            newKeyActions.append(keyAction);
        }

        return newKeyActions;
    }

    private String getLPEXEditorPopupMenu(String popupMenu) {

        ArrayList<String> menuActions = new ArrayList<String>();

        menuActions.add(ConvertHFDAction.getLPEXMenuAction());
        menuActions.add(ConvertSelectionHFDAction.getLPEXMenuAction());
        // menuActions.add(null); // delimiter

        popupMenu = removeSubMenu(MENU_NAME, popupMenu);
        String newMenu = createSubMenu(MENU_NAME, menuActions);

        if (popupMenu != null && popupMenu.contains(newMenu)) {
            return popupMenu;
        }

        if (popupMenu != null) {
            return newMenu + SPACE + popupMenu;
        }

        return newMenu;
    }

    private String removeSubMenu(String subMenu, String menu) {

        int start = menu.indexOf(createStartMenuTag(subMenu));
        if (start < 0) {
            return menu;
        }

        String endSubMenu = createEndMenuTag();
        int end = menu.indexOf(endSubMenu, start);
        if (end < 0) {
            return menu;
        }

        StringBuilder newMenu = new StringBuilder();
        newMenu.append(menu.substring(0, start));
        newMenu.append(menu.substring(end + endSubMenu.length()));

        return newMenu.toString();
    }

    private String createSubMenu(String menu, List<String> menuActions) {

        String startMenu = createStartMenuTag(menu);

        StringBuilder newMenu = new StringBuilder();
        newMenu.append(startMenu);
        newMenu.append(SPACE);
        for (String action : menuActions) {
            if (action == null) {
                newMenu.append(createMenuItem(SEPARATOR));
            } else {
                newMenu.append(createMenuItem(action));
            }
        }

        newMenu.append(createEndMenuTag());

        return newMenu.toString();
    }

    private String createStartMenuTag(String subMenu) {
        return BEGIN_SUB_MENU + SPACE + DOUBLE_QUOTES + subMenu + DOUBLE_QUOTES;
    }

    private String createMenuItem(String action) {
        return action + SPACE;
    }

    private String createEndMenuTag() {
        return END_SUB_MENU + SPACE + createMenuItem(SEPARATOR);
    }
}
