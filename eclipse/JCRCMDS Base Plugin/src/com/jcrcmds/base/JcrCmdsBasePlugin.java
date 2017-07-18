package com.jcrcmds.base;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JcrCmdsBasePlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.jcrcmds.base"; //$NON-NLS-1$

    // The shared instance
    private static JcrCmdsBasePlugin plugin;

    /**
     * The constructor
     */
    public JcrCmdsBasePlugin() {
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
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static JcrCmdsBasePlugin getDefault() {
        return plugin;
    }

    /**
     * Convenience method to log error messages to the application log.
     * 
     * @param message Message
     * @param e The exception that has produced the error
     */
    public static void logError(String message, Throwable e) {
        if (plugin == null) {
            System.err.println(message);
            if (e != null) {
                e.printStackTrace();
            }
            return;
        }
        plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, Status.ERROR, message, e));
    }

    /**
     * Convenience method to log infomational messages to the application log.
     * 
     * @param message Message
     * @param e The exception that has produced the error
     */
    public static void logInfo(String message) {
        if (plugin == null) {
            System.err.println(message);
            return;
        }
        plugin.getLog().log(new Status(Status.INFO, PLUGIN_ID, message));
    }

}
