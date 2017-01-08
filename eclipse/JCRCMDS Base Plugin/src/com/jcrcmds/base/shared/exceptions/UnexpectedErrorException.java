/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.shared.exceptions;

import com.jcrcmds.base.Messages;

public class UnexpectedErrorException extends Exception {

    private static final long serialVersionUID = -4561375234451451423L;

    public UnexpectedErrorException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        if (super.getCause() == null) {
            return Messages
                .bind(Messages.UnexpectedErrorException, Messages.No_further_information_available_Check_the_Eclipse_error_log_for_details);
        } else {
            return Messages.bind(Messages.UnexpectedErrorException, super.getCause().getMessage());
        }
    }

}
