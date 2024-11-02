/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import com.jcrcmds.core.JcrCmdsCorePlugin;

/**
 * Class to manage access to the preferences of the plugin.
 * <p>
 * Eclipse stores the preferences as <i>diffs</i> to their default values in
 * directory
 * <code>[workspace]\.metadata\.plugins\org.eclipse.core.runtime\.settings\</code>.
 * 
 * @author Thomas Raddatz
 */
public final class Preferences {

    /**
     * The instance of this Singleton class.
     */
    private static Preferences instance;

    /**
     * Global preferences of the plugin.
     */
    private IPreferenceStore preferenceStore;

    /*
     * Preferences keys:
     */

    private static final String DOMAIN = JcrCmdsCorePlugin.PLUGIN_ID + "."; //$NON-NLS-1$

    private static final String PRODUCT_LIBRARY = DOMAIN + "PRODUCT_LIBRARY"; //$NON-NLS-1$
    private static final String WORK_LIBRARY = DOMAIN + "WORK_LIBRARY"; //$NON-NLS-1$
    private static final String WORK_FILE = DOMAIN + "WORK_FILE"; //$NON-NLS-1$
    private static final String WORK_MEMBER = DOMAIN + "WORK_MEMBER"; //$NON-NLS-1$
    private static final String WORK_FILE_RECORD_LENGTH = DOMAIN + "WORK_FILE_RECORD_LENGTH"; //$NON-NLS-1$
    private static final String JCRHFD_COMMAND = DOMAIN + "JCRHFD_COMMAND"; //$NON-NLS-1$

    /*
     * Initial values
     */

    private static final String INITIAL_PRODUCT_LIBRARY = "JCRCMDS"; //$NON-NLS-1$
    private static final String INITIAL_WORK_LIBRARY = "QTEMP"; //$NON-NLS-1$
    private static final String INITIAL_WORK_FILE = "QJCRHFD"; //$NON-NLS-1$
    private static final String INITIAL_WORK_MEMBER = "JCRHFD"; //$NON-NLS-1$
    private static final int INITIAL_WORK_FILE_RECORD_LENGTH = 112;
    private static final String INITIAL_JCRHFD_COMMAND = "JCRHFD RPG4MBR(&IM) INFILE(&IL/&IF) FREEMBR(&OM) FREEFIL(&OL/&OF)"; //$NON-NLS-1$

    /**
     * Private constructor to ensure the Singleton pattern.
     */
    private Preferences() {
    }

    /**
     * Thread-safe method that returns the instance of this Singleton class.
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
            instance.preferenceStore = JcrCmdsCorePlugin.getDefault().getPreferenceStore();
        }
        return instance;
    }

    /**
     * Thread-safe method that disposes the instance of this Singleton class.
     * <p>
     * This method is intended to be call from
     * {@link org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)}
     * to free the reference to itself.
     */
    public static void dispose() {
        if (instance != null) {
            instance = null;
        }
    }

    /*
     * Preferences: GETTER
     */

    public String getProductLibrary() {
        return preferenceStore.getString(PRODUCT_LIBRARY);
    }

    public String getWorkLibrary() {
        return preferenceStore.getString(WORK_LIBRARY);
    }

    public String getWorkFile() {
        return preferenceStore.getString(WORK_FILE);
    }

    public String getWorkMember() {
        return preferenceStore.getString(WORK_MEMBER);
    }

    public int getWorkFileRecordLength() {
        return preferenceStore.getInt(WORK_FILE_RECORD_LENGTH);
    }

    public String getJcrHfdCommand() {
        return preferenceStore.getString(JCRHFD_COMMAND);
    }

    /*
     * Preferences: SETTER
     */

    public void setProductLibrary(String library) {
        preferenceStore.setValue(PRODUCT_LIBRARY, library);
    }

    public void setWorkLibrary(String library) {
        preferenceStore.setValue(WORK_LIBRARY, library);
    }

    public void setWorkFile(String file) {
        preferenceStore.setValue(WORK_FILE, file);
    }

    public void setWorkMember(String member) {
        preferenceStore.setValue(WORK_MEMBER, member);
    }

    public void setWorkFileRecordLength(int recordLength) {
        preferenceStore.setValue(WORK_FILE_RECORD_LENGTH, recordLength);
    }

    public void setJcrHfdCommand(String command) {
        preferenceStore.setValue(JCRHFD_COMMAND, command);
    }

    /*
     * Preferences: Default Initializer
     */

    public void initializeDefaultPreferences() {

        preferenceStore.setDefault(PRODUCT_LIBRARY, getInitialProductLibrary());
        preferenceStore.setDefault(WORK_LIBRARY, getInitialWorkLibrary());
        preferenceStore.setDefault(WORK_FILE, getInitialWorkFile());
        preferenceStore.setDefault(WORK_MEMBER, getInitialWorkMember());
        preferenceStore.setDefault(WORK_FILE_RECORD_LENGTH, getInitialWorkFileRecordLength());
        preferenceStore.setDefault(JCRHFD_COMMAND, getInitialJcrHfdCommand());
    }

    /*
     * Preferences: Default Values
     */

    public String getInitialProductLibrary() {
        return INITIAL_PRODUCT_LIBRARY;
    }

    public String getInitialWorkLibrary() {
        return INITIAL_WORK_LIBRARY;
    }

    public String getInitialWorkFile() {
        return INITIAL_WORK_FILE;
    }

    public String getInitialWorkMember() {
        return INITIAL_WORK_MEMBER;
    }

    public int getInitialWorkFileRecordLength() {
        return INITIAL_WORK_FILE_RECORD_LENGTH;
    }

    public String getInitialJcrHfdCommand() {
        return INITIAL_JCRHFD_COMMAND;
    }
}