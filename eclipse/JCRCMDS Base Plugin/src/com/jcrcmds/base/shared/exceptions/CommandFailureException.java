/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.shared.exceptions;

import com.jcrcmds.base.Messages;

public class CommandFailureException extends Exception {

    private static final long serialVersionUID = -403779952674430363L;

    private String command;
    private String message;

    public CommandFailureException(String command, String message) {
        this.command = command;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return Messages.bind(Messages.CommandFailureException, command, message);
    }
}
