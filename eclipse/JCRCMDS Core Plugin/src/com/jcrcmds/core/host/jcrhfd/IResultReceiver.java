/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.host.jcrhfd;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IResultReceiver {

    public void setResult(String[] sourceLines, IProgressMonitor monitor);

}
