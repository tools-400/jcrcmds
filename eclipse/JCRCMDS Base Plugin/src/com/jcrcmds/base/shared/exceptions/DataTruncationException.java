/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.shared.exceptions;

import com.jcrcmds.base.Messages;

public class DataTruncationException extends Exception {

    private static final long serialVersionUID = 3117419856832871407L;

    private String library;
    private String file;
    private Integer actualRecordLength;
    private Integer requiredRecordLength;

    public DataTruncationException(String library, String file, int actualRecordLength, int requiredRecordLength) {
        super();

        this.library = library;
        this.file = file;
        this.actualRecordLength = actualRecordLength;
        this.requiredRecordLength = requiredRecordLength;
    }

    @Override
    public String getMessage() {
        return Messages.bind(Messages.DataTruncationException, new Object[] { library, file, actualRecordLength, requiredRecordLength });
    }

}
