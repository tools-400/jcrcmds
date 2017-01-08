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
import com.ibm.etools.iseries.core.api.ISeriesFile;
import com.ibm.etools.systems.core.messages.SystemMessageException;
import com.jcrcmds.base.rse.internal.AbstractRemoteObject;
import com.jcrcmds.base.shared.exceptions.UnexpectedErrorException;
import com.jcrcmds.base.shared.logger.Logger;

public abstract class AbstractRemoteSourceFile extends AbstractRemoteObject {

    private String sourceFileName;
    private String sourceFileLibraryName;
    private ISeriesConnection connection;
    private ISeriesFile sourceFile;

    private String textDescription;

    public AbstractRemoteSourceFile(String connectionName, String library, String file) {
        super(connectionName, new QSYSObjectPathName(library, file, "FILE")); //$NON-NLS-1$

        this.sourceFileName = file;
        this.sourceFileLibraryName = library;

        try {
            connection = ISeriesConnection.getConnection(connectionName);
            this.sourceFile = loadRemoteSourceFile(connection, library, file);
        } catch (Exception e) {
            Logger.logError("*** Unexpected error when retrieving library " + library + " ***", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private ISeriesFile loadRemoteSourceFile(ISeriesConnection connection, String library, String file) throws SystemMessageException {
        return connection.getISeriesFile(null, library, file);
    }

    public void setDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    public boolean exists() throws Exception {

        try {
            if (sourceFile == null) {
                return false;
            }
            return sourceFile.exists();
        } catch (Exception e) {
            if (sourceFile == null) {
                Logger.logError("*** Could not check library [null] for existence ***", e); //$NON-NLS-1$
            } else {
                Logger.logError("*** Could not check source file " + getLibraryName() + "/" + getFileName() + " for existence ***", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            throw new UnexpectedErrorException(e);
        }
    }

    public String getLibraryName() {
        if (sourceFile == null) {
            return sourceFileLibraryName;
        }
        return sourceFile.getLibrary();
    }

    public String getFileName() {
        if (sourceFile == null) {
            return sourceFileName;
        }
        return sourceFile.getName();
    }

    public String getDescription() {
        if (sourceFile == null) {
            return textDescription;
        }
        return sourceFile.getDescription();
    }

    public int getRecordLength() throws Exception {
        try {
            return sourceFile.getRecordLength();
        } catch (Exception e) {
            if (sourceFile == null) {
                Logger.logError("*** Could not check library [null] for existence ***", e); //$NON-NLS-1$
            } else {
                Logger.logError("*** Could not check source file " + getLibraryName() + "/" + getFileName() + " for existence ***", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            throw new UnexpectedErrorException(e);
        }
    }

    public String create(int recordLength) throws Exception {

        StringBuilder command = new StringBuilder();
        command.append("CRTSRCPF FILE("); //$NON-NLS-1$
        command.append(getLibraryName());
        command.append("/"); //$NON-NLS-1$
        command.append(getFileName());
        command.append(") RCDLEN("); //$NON-NLS-1$
        command.append(recordLength);
        command.append(") TEXT('"); //$NON-NLS-1$
        command.append(getDescription().replaceAll("'", "''")); //$NON-NLS-1$ //$NON-NLS-2$
        command.append("')"); //$NON-NLS-1$

        String message = executeCommand(command.toString());
        if (message == null) {
            this.sourceFile = loadRemoteSourceFile(connection, getLibraryName(), getFileName());
        }

        return message;
    }
}
