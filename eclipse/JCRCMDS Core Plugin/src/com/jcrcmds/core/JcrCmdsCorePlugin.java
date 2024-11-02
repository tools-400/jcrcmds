package com.jcrcmds.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.jcrcmds.core.lpex.MenuExtension;

/**
 * The activator class controls the plug-in life cycle
 */
public class JcrCmdsCorePlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.jcrcmds.core"; //$NON-NLS-1$

    // The shared instance
    private static JcrCmdsCorePlugin plugin;

    // The Lpex menu extension
    private MenuExtension menuExtension;

    /**
     * The constructor
     */
    public JcrCmdsCorePlugin() {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {

        if (menuExtension != null) {
            menuExtension.uninstall();
        }

        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static JcrCmdsCorePlugin getDefault() {
        return plugin;
    }

    public void setLpexMenuExtension(MenuExtension menuExtension) {
        this.menuExtension = menuExtension;
    }

}
