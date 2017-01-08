/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.rse.shared.connection;

import org.eclipse.core.resources.IFile;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import com.ibm.as400.access.AS400;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;
import com.ibm.etools.systems.editor.IRemoteResourceProperties;
import com.ibm.etools.systems.editor.RemoteResourcePropertiesFactoryManager;

public final class ConnectionHelper {

    /**
     * Returns the connection name of a given editor.
     * 
     * @param editor - that shows a remote file
     * @return name of the connection the file has been loaded from
     */
    public static String getConnectionName(IEditorPart editor) {

        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)editorInput).getFile();
            IRemoteResourceProperties properties = RemoteResourcePropertiesFactoryManager.getInstance().getRemoteResourceProperties(file);
            String subsystemStr = properties.getRemoteFileSubSystem();
            if (subsystemStr != null) {
                ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
                if (registry != null) {
                    ISubSystem subsystem = registry.getSubSystem(subsystemStr);
                    if (subsystem != null) {
                        String connectionName = subsystem.getHost().getAliasName();
                        return connectionName;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Internal method that returns a connection for a given profile and
     * connection name. The profile might be null.
     * 
     * @parm profile - Profile that is searched for the connection
     * @parm connectionName - Name of the connection a system is returned for
     * @return IBMiConnection
     */
    private static IBMiConnection getConnection(String profile, String connectionName) {

        if (profile == null) {
            return IBMiConnection.getConnection(connectionName);
        }

        return IBMiConnection.getConnection(profile, connectionName);
    }

    /**
     * Returns a system for a given connection name.
     * 
     * @parm connectionName - Name of the connection a system is returned for
     * @return AS400
     */
    public static AS400 getSystem(String connectionName) {

        return getSystem(null, connectionName);
    }

    /**
     * Returns a system for a given profile and connection name.
     * 
     * @parm profile - Profile that is searched for the connection
     * @parm connectionName - Name of the connection a system is returned for
     * @return AS400
     */
    private static AS400 getSystem(String profile, String connectionName) {

        IBMiConnection connection = getConnection(profile, connectionName);
        if (connection == null) {
            return null;
        }

        try {
            return connection.getAS400ToolboxObject();
        } catch (Throwable e) {
            return null;
        }
    }

}
