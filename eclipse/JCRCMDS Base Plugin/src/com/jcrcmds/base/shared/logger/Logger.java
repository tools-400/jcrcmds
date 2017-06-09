/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.shared.logger;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import com.jcrcmds.base.JcrCmdsBasePlugin;

public final class Logger {

    /**
     * Convenience method to log error messages to the application log.
     * 
     * @param message Message
     * @param e The exception that has produced the error
     */
    public static void logError(String message, Throwable e) {

        if (getPlugin() == null) {
            System.err.println(message);
            e.printStackTrace();
            return;
        }
        getPlugin().getLog().log(new Status(Status.ERROR, JcrCmdsBasePlugin.PLUGIN_ID, Status.ERROR, message, e));
    }

    private static Plugin getPlugin() {
        return JcrCmdsBasePlugin.getDefault();
    }

}
