package org.vaadin.addons.portallayout.event;

import com.vaadin.ui.Component;
import org.vaadin.addons.portallayout.portlet.Portlet;

/**
 *
 */
public class PortletCollapseEvent extends Component.Event {

    public static final java.lang.reflect.Method PORTLET_COLLAPSE_STATE_CHANGED;

    static {
        try {
            PORTLET_COLLAPSE_STATE_CHANGED = Listener.class.getDeclaredMethod("portletCollapseStateChanged",
                            new Class[] { PortletCollapseEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    private final Portlet portlet;

    public PortletCollapseEvent(Component source, final Portlet portlet) {
        super(source);
        this.portlet = portlet;
    }

    public Portlet getPortlet() {
        return portlet;
    }

    public interface Listener {

        void portletCollapseStateChanged(PortletCollapseEvent event);
    }

}
