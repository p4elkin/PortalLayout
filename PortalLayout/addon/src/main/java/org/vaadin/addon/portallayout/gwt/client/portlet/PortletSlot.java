package org.vaadin.addon.portallayout.gwt.client.portlet;

import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class PortletSlot extends SimplePanel {

    public PortletSlot(PortletWidget portlet) {
        setStyleName("v-portlet-slot");
        setWidget(portlet);
    }
    
    @Override
    public PortletWidget getWidget() {
        return (PortletWidget)super.getWidget();
    }
    
    @Override
    public void setWidget(Widget w) {
        super.setWidget(w);
        removeStyleName(DragClientBundle.INSTANCE.css().positioner());
    }
    

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
    }
   
}
