package org.vaadin.addon.portallayout.gwt.shared.portlet;

import java.io.Serializable;

import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.AbstractLayoutState;

public class PortletState extends AbstractLayoutState {

    public boolean isLocked = false;

    public boolean isCollapsed = false;

    public boolean isClosable = true;

    public boolean isCollapsible = true;
    
    public SlotSize slotSize = new SlotSize();
    
    public Connector content;
    
    public Connector headerToolbar;
    
    public String getFormattedWidth() {
        return slotSize.width < 0 ? "" : slotSize.width + slotSize.widthUnit;            
    }
    
    public String getFormattedHeight() {
        return slotSize.height < 0 ? "" : slotSize.height + slotSize.heightUnit;
    }
    
    public static class SlotSize implements Serializable {
        
        public float width = -1;
        
        public float height = -1;
        
        public String widthUnit = null;
       
        public String heightUnit = null;
    }
}
