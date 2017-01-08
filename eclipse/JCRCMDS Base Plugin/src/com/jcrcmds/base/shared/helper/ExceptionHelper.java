/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.shared.helper;

public final class ExceptionHelper {

    public static String getLocalizedMessage(Throwable throwable) {

        if (throwable.getLocalizedMessage() == null || throwable.getLocalizedMessage().length() == 0) {
            return throwable.getClass().getName();
        } else {
            return throwable.getLocalizedMessage();
        }

    }

}
