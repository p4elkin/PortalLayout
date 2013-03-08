package org.vaadin.addon.portallayout.gwt.shared.portal;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.AbstractLayoutState;

public class PortalState extends AbstractLayoutState {

    public int marginsBitmask = 0;
    
    public List<Connector> portletConnectors = new LinkedList<Connector>();
}
