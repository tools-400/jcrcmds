/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.rse.shared.host;

import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesLibrary;
import com.jcrcmds.base.rse.internal.AbstractRemoteObject;
import com.jcrcmds.base.shared.exceptions.UnexpectedErrorException;
import com.jcrcmds.base.shared.logger.Logger;

public abstract class AbstractRemoteLibrary extends AbstractRemoteObject {

    private ISeriesLibrary library;

    public AbstractRemoteLibrary(String connectionName, String library) {
        super(connectionName, new QSYSObjectPathName("QSYS", library, "LIB")); //$NON-NLS-1$ //$NON-NLS-2$

        try {
            ISeriesConnection connection = ISeriesConnection.getConnection(connectionName);
            this.library = connection.getISeriesLibrary(null, library);
        } catch (Exception e) {
            Logger.logError("*** Unexpected error when retrieving library " + library + " ***", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void setDescription(String textDescription) {
        this.library.setDescription(textDescription);
    }

    public boolean exists() throws Exception {

        try {
            return library != null;
        } catch (Exception e) {
            Logger.logError("*** Could not check library " + library + " for existence ***", e); //$NON-NLS-1$ //$NON-NLS-2$
            throw new UnexpectedErrorException(e);
        }
    }

    public String getLibraryName() {
        return library.getLibrary();
    }

}
