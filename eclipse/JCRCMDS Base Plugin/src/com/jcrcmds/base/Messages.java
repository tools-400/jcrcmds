/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.jcrcmds.base.messages"; //$NON-NLS-1$

    public static String E_R_R_O_R;

    public static String UnexpectedErrorException;
    public static String CommandFailureException;
    public static String LibraryNotFoundException;

    public static String No_further_information_available_Check_the_Eclipse_error_log_for_details;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
