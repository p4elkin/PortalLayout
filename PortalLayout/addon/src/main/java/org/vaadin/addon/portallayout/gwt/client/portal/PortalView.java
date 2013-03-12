package org.vaadin.addon.portallayout.gwt.client.portal;

import org.vaadin.addon.portallayout.gwt.client.portlet.PortletChrome;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;

public interface PortalView extends IsWidget, HasWidgets, IndexedPanel, InsertPanel {
    
    void addPortlet(PortletChrome pc);
    
    void removePortlet(PortletChrome widget);
    
    @Override
    Panel asWidget();
    
    interface Presenter {
        
        void recalculateHeights();
        
        PortalView getView();

    }
}
