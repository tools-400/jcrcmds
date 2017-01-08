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
import com.ibm.etools.iseries.subsystems.qsys.objects.QSYSRemoteSourceFile;
import com.ibm.etools.iseries.subsystems.qsys.objects.RemoteObjectContext;
import com.jcrcmds.base.rse.internal.AbstractRemoteObject;
import com.jcrcmds.base.shared.exceptions.UnexpectedErrorException;
import com.jcrcmds.base.shared.logger.Logger;

public abstract class AbstractRemoteSourceFile extends AbstractRemoteObject {

    private QSYSRemoteSourceFile sourceFile;

    public AbstractRemoteSourceFile(String connectionName, String library, String file) {
        super(connectionName, new QSYSObjectPathName(library, file, "FILE")); //$NON-NLS-1$

        QSYSObjectSubSystem subSystem = (QSYSObjectSubSystem)IBMiConnection.getConnection(connectionName).getCommandSubSystem().getObjectSubSystem();
        this.sourceFile = new QSYSRemoteSourceFile();
        this.sourceFile.setLibrary(library);
        this.sourceFile.setName(file);
        this.sourceFile.setRemoteObjectContext(new RemoteObjectContext(subSystem));
    }

    public void setDescription(String textDescription) {
        this.sourceFile.setDescription(textDescription);
    }

    public boolean exists() throws Exception {

        try {
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
        return sourceFile.getLibrary();
    }

    public String getFileName() {
        return sourceFile.getName();
    }

    public String getDescription() {
        return sourceFile.getDescription();
    }

    public int getRecordLength() throws Exception {
        try {
            return sourceFile.getRecordFormat(null).getLength();
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

        return executeCommand(command.toString());
    }
}
