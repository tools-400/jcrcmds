/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.base.rse.shared.host;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;
import com.jcrcmds.base.rse.internal.AbstractRemoteObject;
import com.jcrcmds.base.shared.logger.Logger;

public abstract class AbstractRemoteSourceMember extends AbstractRemoteObject {

    /** Record field */
    public static final int SRCSEQ_INDEX = 0;
    /** Record field */
    public static final int SRCDAT_INDEX = 1;
    /** Record field */
    public static final int SRCDTA_INDEX = 2;

    private String type;
    private String description;

    public AbstractRemoteSourceMember(String connectionName, String library, String file, String member) {
        super(connectionName, new QSYSObjectPathName(library, file, member, "MBR")); //$NON-NLS-1$

        this.type = ""; //$NON-NLS-1$
        this.description = ""; //$NON-NLS-1$
    }

    public String setTypeAndDescription(String type, String description) throws Exception {

        this.type = type;
        this.description = description;

        StringBuilder command = new StringBuilder();
        command.append("CHGPFM FILE("); //$NON-NLS-1$
        command.append(getLibraryName());
        command.append("/"); //$NON-NLS-1$
        command.append(getFileName());
        command.append(") MBR("); //$NON-NLS-1$
        command.append(getMemberName());
        command.append(") TEXT('"); //$NON-NLS-1$
        command.append(getDescription().replaceAll("'", "''")); //$NON-NLS-1$ //$NON-NLS-2$
        command.append("') SRCTYPE("); //$NON-NLS-1$
        command.append(getType());
        command.append(")"); //$NON-NLS-1$

        return executeCommand(command.toString());
    }

    public void upload(String[] sourceLines) throws Exception {

        SequentialFile sourceFile = new SequentialFile(getSystem(), getObjectPathName().getPath());
        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(getSystem(), getObjectPathName().getPath());

        try {

            RecordFormat[] format = recordDescription.retrieveRecordFormat();

            sourceFile.setRecordFormat(format[0]);
            sourceFile.open(AS400File.WRITE_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

            BigDecimal srcSeq = new BigDecimal(0);
            BigDecimal srcInc = new BigDecimal(1);
            Record[] records = new Record[sourceLines.length];

            for (int i = 0; i < sourceLines.length; i++) {
                srcSeq.add(srcInc);
                Record record = new Record(format[0]);
                record.setField(SRCSEQ_INDEX, srcSeq);
                record.setField(SRCDTA_INDEX, sourceLines[i]);
                records[i] = record;
            }

            sourceFile.write(records);

            sourceFile.close();

        } finally {
            try {
                sourceFile.close();
            } catch (Exception e) {
                Logger.logError("*** Could not close file ***", e); //$NON-NLS-1$
            }
        }
    }

    public String[] download() throws Exception {

        List<String> sourceLines = new LinkedList<String>();

        SequentialFile sourceFile = new SequentialFile(getSystem(), getObjectPathName().getPath());
        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(getSystem(), getObjectPathName().getPath());

        try {

            RecordFormat[] format = recordDescription.retrieveRecordFormat();

            sourceFile.setRecordFormat(format[0]);
            sourceFile.open(AS400File.READ_ONLY, 1000, AS400File.COMMIT_LOCK_LEVEL_NONE);

            Record record = sourceFile.readNext();
            while (record != null) {
                String sourceData = (String)record.getField(SRCDTA_INDEX);
                sourceLines.add(sourceData);
                record = sourceFile.readNext();
            }

            sourceFile.close();

        } finally {
            try {
                sourceFile.close();
            } catch (Exception e) {
                Logger.logError("*** Could not close file ***", e); //$NON-NLS-1$
            }
        }

        return sourceLines.toArray(new String[sourceLines.size()]);
    }

    public boolean exists() throws Exception {

        if (getConnection().getMember(getLibraryName(), getFileName(), getMemberName(), null) != null) {
            return true;
        }

        return false;
    }

    public String clear() throws Exception {

        StringBuilder command = new StringBuilder();
        command.append("CLRPFM FILE("); //$NON-NLS-1$
        command.append(getLibraryName());
        command.append("/"); //$NON-NLS-1$
        command.append(getFileName());
        command.append(") MBR("); //$NON-NLS-1$
        command.append(getMemberName());
        command.append(")"); //$NON-NLS-1$

        return executeCommand(command.toString());
    }

    public String getLibraryName() {
        return getObjectPathName().getLibraryName();
    }

    public String getFileName() {
        return getObjectPathName().getObjectName();
    }

    public String getMemberName() {
        return getObjectPathName().getMemberName();
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String create() throws Exception {

        StringBuilder command = new StringBuilder();
        command.append("ADDPFM FILE("); //$NON-NLS-1$
        command.append(getLibraryName());
        command.append("/"); //$NON-NLS-1$
        command.append(getFileName());
        command.append(") MBR("); //$NON-NLS-1$
        command.append(getMemberName());
        command.append(") TEXT('"); //$NON-NLS-1$
        command.append(getDescription().replaceAll("'", "''")); //$NON-NLS-1$ //$NON-NLS-2$
        command.append("') SRCTYPE("); //$NON-NLS-1$
        command.append(getType());
        command.append(")"); //$NON-NLS-1$

        return executeCommand(command.toString());
    }
}
