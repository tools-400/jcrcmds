/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.host;

import com.jcrcmds.base.rse.shared.host.AbstractRemoteSourceFile;

public class RemoteFile extends AbstractRemoteSourceFile {

    public RemoteFile(String connectionName, String library, String file) {
        super(connectionName, library, file);
    }

}
