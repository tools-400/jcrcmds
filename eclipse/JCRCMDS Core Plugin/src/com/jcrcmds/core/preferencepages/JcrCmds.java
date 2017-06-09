/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.preferencepages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.jcrcmds.base.shared.helper.IntHelper;
import com.jcrcmds.core.JcrCmdsCorePlugin;
import com.jcrcmds.core.Messages;
import com.jcrcmds.core.preferences.Preferences;
import com.jcrcmds.core.swt.widgets.WidgetFactory;

public class JcrCmds extends PreferencePage implements IWorkbenchPreferencePage {

    private static final int MAXIMUM_NAME_LENGTH = 9;
    private static final int MAXIMUM_RECORD_LENGTH = 32766;

    private Text textProductLibrary;
    private Text textWorkLibrary;
    private Text textWorkFile;
    private Text textWorkMember;
    private Text textWorkFileRecordLength;
    private Text textJcrHfdCmd;

    public JcrCmds() {
        super();
        setPreferenceStore(JcrCmdsCorePlugin.getDefault().getPreferenceStore());
        getPreferenceStore();
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    public Control createContents(Composite parent) {

        Composite main = new Composite(parent, SWT.NONE);
        main.setLayout(new GridLayout(2, false));
        main.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createSectionCommon(main);

        setScreenToValues();

        return main;
    }

    private void createSectionCommon(Composite parent) {

        textProductLibrary = createNameTextField(parent, Messages.Label_product_library_name, Messages.Tooltip_product_library_name);
        textProductLibrary.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (validateProductLibraryName()) {
                    validateAll();
                }
                setControlsEnablement();
            }
        });

        textWorkLibrary = createNameTextField(parent, Messages.Label_work_library_name, Messages.Tooltip_work_library_name);
        textWorkLibrary.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (validateWorkLibraryName()) {
                    validateAll();
                }
                setControlsEnablement();
            }
        });

        textWorkFile = createNameTextField(parent, Messages.Label_work_file_name, Messages.Tooltip_work_file_name);
        textWorkFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (validateWorkFileName()) {
                    validateAll();
                }
                setControlsEnablement();
            }
        });

        textWorkMember = createNameTextField(parent, Messages.Label_work_member_name, Messages.Tooltip_work_member_name);
        textWorkMember.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (validateWorkMemberName()) {
                    validateAll();
                }
                setControlsEnablement();
            }
        });

        textWorkFileRecordLength = createIntegerField(parent, Messages.Label_work_file_record_length, Messages.Tooltip_work_file_record_length);
        textWorkFileRecordLength.setTextLimit(5);
        textWorkFileRecordLength.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (validateWorkFileRecordLength()) {
                    validateAll();
                }
                setControlsEnablement();
            }
        });

        textJcrHfdCmd = createTextBox(parent, Messages.Label_JCRHFD_command, Messages.Tooltip_JCRHFD_command);
        textJcrHfdCmd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (validateJCRHFDCommand()) {
                    validateAll();
                }
                setControlsEnablement();
            }
        });
    }

    private Text createTextBox(Composite parent, String label, String tooltip) {

        createFieldLabel(parent, label, tooltip);

        Text textBox = WidgetFactory.createMultilineText(parent, true, true);
        textBox.setToolTipText(tooltip);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        gd.widthHint = GridData.FILL;
        gd.heightHint = 100;
        gd.grabExcessHorizontalSpace = true;
        textBox.setLayoutData(gd);

        return textBox;
    }

    private Text createNameTextField(Composite parent, String label, String tooltip) {

        createFieldLabel(parent, label, tooltip);

        Text textField = WidgetFactory.createNameText(parent, true);
        textField.setToolTipText(tooltip);

        return textField;
    }

    private Text createIntegerField(Composite parent, String label, String tooltip) {

        createFieldLabel(parent, label, tooltip);

        Text textField = WidgetFactory.createIntegerText(parent);
        textField.setToolTipText(tooltip);

        GridData gd = new GridData();
        gd.widthHint = 40;
        textField.setLayoutData(gd);

        return textField;
    }

    private void createFieldLabel(Composite parent, String label, String tooltip) {

        Label textFieldLabel = new Label(parent, SWT.NONE);
        textFieldLabel.setText(label);
        textFieldLabel.setToolTipText(tooltip);
        textFieldLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
    }

    @Override
    protected void performApply() {
        setStoreToValues();
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        setScreenToDefaultValues();
        super.performDefaults();
    }

    @Override
    public boolean performOk() {

        if (!validateAll()) {
            return false;
        }

        setStoreToValues();
        return super.performOk();
    }

    protected void setStoreToValues() {

        Preferences preferences = Preferences.getInstance();

        preferences.setProductLibrary(textProductLibrary.getText());
        preferences.setWorkLibrary(textWorkLibrary.getText());
        preferences.setWorkFile(textWorkFile.getText());
        preferences.setWorkMember(textWorkMember.getText());
        preferences.setWorkFileRecordLength(IntHelper.tryParseInt(textWorkFileRecordLength.getText(), preferences.getInitialWorkFileRecordLength()));
        preferences.setJcrHfdCommand(textJcrHfdCmd.getText());
    }

    protected void setScreenToValues() {

        JcrCmdsCorePlugin.getDefault();

        Preferences preferences = Preferences.getInstance();

        textProductLibrary.setText(preferences.getProductLibrary());
        textWorkLibrary.setText(preferences.getWorkLibrary());
        textWorkFile.setText(preferences.getWorkFile());
        textWorkMember.setText(preferences.getWorkMember());
        textWorkFileRecordLength.setText(Integer.toString(preferences.getWorkFileRecordLength()));
        textJcrHfdCmd.setText(preferences.getJcrHfdCommand());

        validateAll();
        setControlsEnablement();
    }

    protected void setScreenToDefaultValues() {

        Preferences preferences = Preferences.getInstance();

        textProductLibrary.setText(preferences.getInitialProductLibrary());
        textWorkLibrary.setText(preferences.getInitialWorkLibrary());
        textWorkFile.setText(preferences.getInitialWorkFile());
        textWorkMember.setText(preferences.getInitialWorkMember());
        textWorkFileRecordLength.setText(Integer.toString(preferences.getInitialWorkFileRecordLength()));
        textJcrHfdCmd.setText(preferences.getInitialJcrHfdCommand());

        validateAll();
        setControlsEnablement();
    }

    private boolean validateProductLibraryName() {

        return true;
    }

    private boolean validateWorkLibraryName() {

        return true;
    }

    private boolean validateWorkFileName() {

        return true;
    }

    private boolean validateWorkMemberName() {

        if (textWorkMember.getText().length() > MAXIMUM_NAME_LENGTH) {
            setErrorMessage(Messages.bind(Messages.Name_must_not_exceed_A_characters_because_suffixes_are_added, MAXIMUM_NAME_LENGTH));
            textWorkMember.setFocus();
            return false;
        }

        return true;
    }

    private boolean validateWorkFileRecordLength() {

        int recordLength = IntHelper.tryParseInt(textWorkFileRecordLength.getText(), -1);
        if (recordLength < 0) {
            setErrorMessage(Messages.Invalid_numeric_data);
            textWorkFileRecordLength.setFocus();
            return false;
        }

        if (recordLength > MAXIMUM_RECORD_LENGTH) {
            setErrorMessage(Messages.bind(Messages.Value_must_not_exceed_A, MAXIMUM_RECORD_LENGTH));
            textWorkFileRecordLength.setFocus();
            return false;
        }

        return true;
    }

    private boolean validateJCRHFDCommand() {

        return true;
    }

    private boolean validateAll() {

        if (!validateProductLibraryName()) {
            return false;
        }

        if (!validateWorkLibraryName()) {
            return false;
        }

        if (!validateWorkFileName()) {
            return false;
        }

        if (!validateWorkMemberName()) {
            return false;
        }

        if (!validateWorkFileRecordLength()) {
            return false;
        }

        if (!validateJCRHFDCommand()) {
            return false;
        }

        return clearError();
    }

    private void setControlsEnablement() {
    }

    private boolean clearError() {
        setErrorMessage(null);
        setValid(true);
        return true;
    }
}
