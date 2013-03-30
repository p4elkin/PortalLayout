package org.vaadin.addon.portallayout;

import com.vaadin.ui.Notification;
import org.vaadin.addon.portallayout.demo.ActionDemoTab;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class PortalLayoutDemoUI extends UI {
    @Override
    protected void init(VaadinRequest request) {
        final Layout layout = new VerticalLayout();
        layout.setSizeFull();
        
        setContent(layout);
        layout.addComponent(new ActionDemoTab());
    }
}
