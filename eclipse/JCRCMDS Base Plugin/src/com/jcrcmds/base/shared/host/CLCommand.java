/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.shared.host;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Job;
import com.jcrcmds.base.Messages;

public class CLCommand {

    private AS400 system;

    public CLCommand(AS400 system) {
        this.system = system;
    }

    public String getCurrentLibrary() throws Exception {

        String currentLibrary = null;

        Job[] jobs = system.getJobs(AS400.COMMAND);

        if (jobs.length == 1) {

            if (!jobs[0].getCurrentLibraryExistence()) {
                currentLibrary = "*CRTDFT"; //$NON-NLS-1$
            } else {
                currentLibrary = jobs[0].getCurrentLibrary();
            }

        }

        return currentLibrary;
    }

    public boolean setCurrentLibrary(String currentLibrary) throws Exception {

        String command = "CHGCURLIB CURLIB(" + currentLibrary + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        CommandCall commandCall = new CommandCall(system);

        if (commandCall.run(command)) {
            return true;
        } else {
            return false;
        }

    }

    public String executeCommand(String command) throws Exception {

        CommandCall commandCall = new CommandCall(system);
        if (!commandCall.run(command.toString())) {
            AS400Message[] messageList = commandCall.getMessageList();
            if (messageList.length > 0) {
                for (int idx = 0; idx < messageList.length; idx++) {
                    if (messageList[idx].getType() == AS400Message.ESCAPE) {
                        return Messages.bind(Messages.CommandFailureException, command, messageList[idx].getText());
                    }
                }
                return Messages.bind(Messages.CommandFailureException, command, messageList[messageList.length - 1].getText());
            }
        }

        return null;
    }
}
