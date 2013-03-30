package org.vaadin.addon.portallayout.event;

import com.vaadin.ui.Component;
import org.vaadin.addon.portallayout.portlet.Portlet;

/**
 *
 */
public class PortletCloseEvent extends Component.Event {

    public static final java.lang.reflect.Method PORTLET_CLOSED;

    static {
        try {
            PORTLET_CLOSED = Listener.class.getDeclaredMethod("portletClosed",new Class[] { PortletCloseEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    private final Portlet portlet;

    public PortletCloseEvent(Component source, final Portlet portlet) {
        super(source);
        this.portlet = portlet;
    }

    public Portlet getPortlet() {
        return portlet;
    }

    public interface Listener {

        void portletClosed(PortletCollapseEvent event);
    }

}
