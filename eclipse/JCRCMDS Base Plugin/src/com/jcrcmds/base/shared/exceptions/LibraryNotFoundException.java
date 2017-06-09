/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.shared.exceptions;

import com.jcrcmds.base.Messages;

public class LibraryNotFoundException extends Exception {

    private static final long serialVersionUID = -8540311098881297311L;

    private String library;

    public LibraryNotFoundException(String library) {
        this.library = library;
    }

    @Override
    public String getMessage() {
        return Messages.bind(Messages.LibraryNotFoundException, library);
    }
}
