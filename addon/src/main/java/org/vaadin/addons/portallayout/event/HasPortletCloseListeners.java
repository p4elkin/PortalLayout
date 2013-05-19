package org.vaadin.addons.portallayout.event;

/**
 *
 */
public interface HasPortletCloseListeners {

    void addPortletCloseListener(PortletCloseEvent.Listener listener);

    void removePortletCloseListener(PortletCloseEvent.Listener listener);

}
