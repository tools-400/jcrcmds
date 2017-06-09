/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.jcrcmds.core.messages"; //$NON-NLS-1$

    // Error messages
    public static String E_R_R_O_R;
    public static String Could_not_get_the_active_editor;
    public static String Could_not_get_AS400_object_for_connection_A;
    public static String Could_not_determine_the_name_of_the_remote_connection;
    public static String Library_A_not_found;
    public static String Could_not_create_source_file_B_in_library_A_Reason_C;
    public static String Could_not_retrieve_current_library;
    public static String Could_not_set_current_library_to_A;
    public static String Could_restore_current_library_to_A;
    public static String Could_not_create_source_member_C_in_file_B_in_library_A_Reason_D;
    public static String Could_not_clear_source_member_C_in_file_B_in_library_A_Reason_D;

    public static String Name_must_not_exceed_A_characters_because_suffixes_are_added;
    public static String Invalid_numeric_data;
    public static String Value_must_not_exceed_A;

    // Information messages
    public static String Information;
    public static String No_source_lines_selected;

    // Menue items
    public static String Menu_Convert_HFD;
    public static String Menu_Convert_selection_HFD;

    // Labels and tooltips
    public static String Label_product_library_name;
    public static String Tooltip_product_library_name;
    public static String Label_work_library_name;
    public static String Tooltip_work_library_name;
    public static String Label_work_file_name;
    public static String Tooltip_work_file_name;
    public static String Label_work_member_name;
    public static String Tooltip_work_member_name;
    public static String Label_work_file_record_length;
    public static String Tooltip_work_file_record_length;
    public static String Label_JCRHFD_command;
    public static String Tooltip_JCRHFD_command;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
