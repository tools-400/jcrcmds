/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.host;

import com.jcrcmds.base.rse.shared.host.AbstractRemoteSourceMember;

public class RemoteSourceMember extends AbstractRemoteSourceMember {

    public RemoteSourceMember(String connectionName, String library, String file, String member) {
        super(connectionName, library, file, member);
    }

}
