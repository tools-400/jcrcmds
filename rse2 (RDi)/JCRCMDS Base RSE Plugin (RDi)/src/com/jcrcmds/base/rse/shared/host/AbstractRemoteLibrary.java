/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.rse.shared.host;

import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;
import com.ibm.etools.iseries.subsystems.qsys.objects.QSYSObjectSubSystem;
import com.ibm.etools.iseries.subsystems.qsys.objects.QSYSRemoteLibrary;
import com.ibm.etools.iseries.subsystems.qsys.objects.RemoteObjectContext;
import com.jcrcmds.base.rse.internal.AbstractRemoteObject;
import com.jcrcmds.base.shared.exceptions.UnexpectedErrorException;
import com.jcrcmds.base.shared.logger.Logger;

public abstract class AbstractRemoteLibrary extends AbstractRemoteObject {

    private QSYSRemoteLibrary library;

    public AbstractRemoteLibrary(String connectionName, String library) {
        super(connectionName, new QSYSObjectPathName("QSYS", library, "LIB")); //$NON-NLS-1$ //$NON-NLS-2$

        QSYSObjectSubSystem subSystem = (QSYSObjectSubSystem)IBMiConnection.getConnection(getConnectionName()).getCommandSubSystem()
            .getObjectSubSystem();
        this.library = new QSYSRemoteLibrary();
        this.library.setLibrary(library);
        this.library.setRemoteObjectContext(new RemoteObjectContext(subSystem));
    }

    public void setDescription(String textDescription) {
        this.library.setDescription(textDescription);
    }

    public boolean exists() throws Exception {

        try {
            return library.exists();
        } catch (Exception e) {
            Logger.logError("*** Could not check library " + library + " for existence ***", e); //$NON-NLS-1$ //$NON-NLS-2$
            throw new UnexpectedErrorException(e);
        }
    }

    public String getLibraryName() {
        return library.getLibrary();
    }

}
