/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.rse.internal;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;
import com.jcrcmds.base.rse.shared.connection.ConnectionHelper;
import com.jcrcmds.base.shared.host.CLCommand;

public abstract class AbstractRemoteObject {

    private String connectionName;
    private QSYSObjectPathName objectPathName;

    private AS400 system;
    private IBMiConnection connection;
    private CLCommand clCommand;

    public AbstractRemoteObject(String connectionName, QSYSObjectPathName objectPathName) {
        this.connectionName = connectionName;
        this.objectPathName = objectPathName;
    }

    protected String getConnectionName() {
        return connectionName;
    }

    protected AS400 getSystem() {

        if (system == null) {
            system = ConnectionHelper.getSystem(getConnectionName());
        }

        return system;
    }

    protected QSYSObjectPathName getObjectPathName() {
        return objectPathName;
    }

    protected IBMiConnection getConnection() {

        if (connection == null) {
            connection = IBMiConnection.getConnection(getConnectionName());
        }

        return connection;
    }

    protected String executeCommand(String command) throws Exception {

        if (clCommand == null) {
            clCommand = new CLCommand(getSystem());
        }

        return clCommand.executeCommand(command);
    }
}
