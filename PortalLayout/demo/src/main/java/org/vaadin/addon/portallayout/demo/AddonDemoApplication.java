package org.vaadin.addon.portallayout.demo;

import com.vaadin.Application;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class AddonDemoApplication extends Application {
    private Window window;

    @Override
    public void init() {
        window = new Window("My Vaadin Application");
        setMainWindow(window);
    }
}
