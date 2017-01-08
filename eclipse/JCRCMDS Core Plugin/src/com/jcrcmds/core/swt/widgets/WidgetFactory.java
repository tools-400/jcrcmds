/*******************************************************************************
 * Copyright (c) 2016-2017 JCRCMDS Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package com.jcrcmds.core.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Factory for creating SWT widgets.
 * 
 * @author Thomas Raddatz
 */
public final class WidgetFactory {

    private static final int NAME_FIELD_WIDTH_HINT = 90;

    /**
     * The instance of this Singleton class.
     */
    private static WidgetFactory instance;

    /**
     * Private constructor to ensure the Singleton pattern.
     */
    private WidgetFactory() {
    }

    /**
     * Thread-safe method that returns the instance of this Singleton class.
     */
    public synchronized static WidgetFactory getInstance() {
        if (instance == null) {
            instance = new WidgetFactory();
        }
        return instance;
    }

    /**
     * Produces a single line text field with a border.
     * 
     * @param parent - parent composite
     * @return text field
     */
    public static Text createText(Composite parent) {
        return WidgetFactory.getInstance().produceText(parent, SWT.NONE, true);
    }

    /**
     * Produces a 'name' text field. The field is upper-case only and limited to
     * 10 characters.
     * 
     * @param parent - parent composite
     * @return text field
     */
    public static Text createNameText(Composite parent) {

        return createNameText(parent, true);
    }

    /**
     * Produces a 'name' text field. The field is upper-case only and limited to
     * 10 characters.
     * 
     * @param parent - parent composite
     * @param widthHint - set default text width
     * @return text field
     */
    public static Text createNameText(Composite parent, boolean widthHint) {

        Text text = createUpperCaseText(parent);
        text.setTextLimit(10);

        if (widthHint) {
            GridData gd = new GridData();
            gd.widthHint = NAME_FIELD_WIDTH_HINT;
            text.setLayoutData(gd);
        }

        return text;
    }

    /**
     * Produces a single line text field with a border. Input is upper-case
     * only.
     * 
     * @param parent - parent composite
     * @return text field
     */
    public static Text createUpperCaseText(Composite parent) {

        Text text = WidgetFactory.getInstance().produceText(parent, SWT.NONE, true);
        text.addVerifyListener(new UpperCaseOnlyVerifier());

        return text;
    }

    /**
     * Produces an integer text field with a border. Only the character 0-9 are
     * allowed to be entered.
     * 
     * @param parent - parent composite
     * @return integer text field
     */
    public static Text createIntegerText(Composite parent) {
        return createIntegerText(parent, false);
    }

    /**
     * Produces an integer text field with a border. Only the character 0-9 are
     * allowed to be entered.
     * 
     * @param parent - parent composite
     * @param hasSign - specifies whether to enable the signs '+' and '-'.
     * @return integer text field
     */
    public static Text createIntegerText(Composite parent, boolean hasSign) {

        Text text = WidgetFactory.createText(parent);
        text.addVerifyListener(new NumericOnlyVerifyListener(false, hasSign));

        return text;
    }

    /**
     * Produces a multi-line line text field with a border. If the text does not
     * fit into the field, a vertical scroll bar is displayed.
     * 
     * @param parent - parent composite
     * @return multi-line text field
     */
    public static Text createMultilineText(Composite parent) {
        return WidgetFactory.getInstance().produceMultilineText(parent, SWT.NONE, false);
    }

    /**
     * Produces a multi-line line text field with a border. If the text does not
     * fit into the field, a vertical scroll bar is displayed.
     * 
     * @param parent - parent composite
     * @param wordWrap - <code>true</code>, to enable word wrap
     * @param autoSelect - <code>true</code>, to select the field content when
     *        entering the field wit the cursor
     * @return multi-line text field
     */
    public static Text createMultilineText(Composite parent, boolean wordWrap, boolean autoSelect) {
        int style;
        if (wordWrap) {
            style = SWT.WRAP;
        } else {
            style = SWT.H_SCROLL;
        }
        return WidgetFactory.getInstance().produceMultilineText(parent, style, autoSelect);
    }

    /*
     * Private worker procedures, doing the actual work.
     */
    private Text produceText(Composite parent, int style, boolean autoSelect) {

        Text text = new Text(parent, style | SWT.BORDER);
        if (autoSelect) {
            text.addFocusListener(new SelectAllFocusListener());
        }

        return text;
    }

    private Text produceMultilineText(Composite parent, int style, boolean autoSelect) {

        Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | style);

        Listener scrollBarListener = new AutoScrollbarsListener();
        text.addListener(SWT.Resize, scrollBarListener);
        text.addListener(SWT.Modify, scrollBarListener);

        if (autoSelect) {
            text.addFocusListener(new SelectAllFocusListener());
        }

        return text;
    }

    /**
     * Thread-safe method that disposes the instance of this Singleton class.
     * <p>
     * This method is intended to be call from
     * {@link org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)}
     * to free the reference to itself.
     */
    public static void dispose() {
        if (instance != null) {
            instance = null;
        }
    }
}
