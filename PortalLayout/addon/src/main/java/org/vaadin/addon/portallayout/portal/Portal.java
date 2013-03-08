package org.vaadin.addon.portallayout.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.vaadin.addon.portallayout.gwt.client.portal.connection.rpc.PortalServerRpc;
import org.vaadin.addon.portallayout.gwt.shared.portal.PortalState;
import org.vaadin.addon.portallayout.portlet.Portlet;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Layout.MarginHandler;

@StyleSheet("test.css")
public class Portal extends AbstractComponent implements MarginHandler, HasComponents {

    private final Map<Component, Portlet> contentToPortlet = new HashMap<Component, Portlet>();
    
	public Portal() {
	    setStyleName("v-portal-layout");
	    registerRpc(new PortalServerRpc() {
            
            @Override
            public void removePortlet(Connector portlet) {
                Portal.this.removePortlet((Portlet)portlet);
            }
            
            @Override
            public void updatePortletPosition(Connector portlet, int index) {
                if (index >= 0) {
                    addComponentAt((Portlet)portlet, index);   
                }
            }
        });
    }
	
    public void removePortlet(Portlet portlet) {
        getState().portletConnectors.remove(portlet);
        contentToPortlet.remove(portlet);
        if (portlet.getParent() == this) {
            portlet.setParent(null);
        }
    }

    public Portlet wrapInPortlet(Component c ) {
        Portlet result = castOrWrapInPortlet(c);
        addPortlet(result);
        return result;
    }
    
    public void addPortlet(Portlet portlet) {
        doAddPortlet(portlet);
        getState().portletConnectors.add(portlet);
    }
    
    public Portlet getPortlet(final Component c) {
        if (contentToPortlet.containsKey(c)) {
            return contentToPortlet.get(c);   
        } else {
            throw new IllegalArgumentException("Layout doesn't contain the component: " + c.getConnectorId());
        }
    }
	
    @Override
    public void setMargin(boolean enabled) {
        setMargin(new MarginInfo(enabled));
    }

    @Override
    public MarginInfo getMargin() {
        return new MarginInfo(getState().marginsBitmask);
    }

    @Override
    public void setMargin(MarginInfo marginInfo) {
        getState().marginsBitmask = marginInfo.getBitMask();
    }
    
    protected void addComponentAt(Portlet portlet, int index) {
        doAddPortlet(portlet);
        LinkedList<Connector> portlets = (LinkedList<Connector>)getState().portletConnectors; 
        if (portlets.contains(portlet)) {
            portlets.remove(portlet);
        }
        portlets.add(index, portlet);
    }
    
    protected boolean doAddPortlet(Portlet portlet) {
        if (portlet != null && !getState().portletConnectors.contains(portlet)) {
            portlet.setWidth("100%");
            contentToPortlet.put(portlet.getContent(), portlet);
            if (portlet.getParent() != null && (portlet.getParent() instanceof Portal)) {
                ((Portal)portlet.getParent()).removePortlet(portlet);
            }
            portlet.setParent(this);
            return true;
        } else if (getState().portletConnectors.contains(portlet)) {
            return false;
        } else throw new IllegalArgumentException("Portlet must not be null.");   
    }

	
	protected void replaceComponent(Component oldComponent, Component newComponent) {
	    final ArrayList<Connector> portlets = (ArrayList<Connector>)getState().portletConnectors; 
		int idx = portlets.indexOf(oldComponent);
		if (idx >= 0 && newComponent != null) {
			Portlet portlet = castOrWrapInPortlet(newComponent);			
			portlets.set(idx, portlet);
			removePortlet((Portlet)oldComponent);
			fireEvent(new ComponentAttachEvent(this, newComponent));
			fireEvent(new ComponentDetachEvent(this, oldComponent));
			markAsDirty();
		}
	}
	
	private Portlet castOrWrapInPortlet(Component c) {
		return c instanceof Portlet ? (Portlet)c : new Portlet(c);
	}

	@Override
	protected PortalState getState() {
	    return (PortalState)super.getState();
	}
	
	@Override
	protected PortalState createState() {
	    return new PortalState();
	}
    
    @Override
    public Iterator<Component> iterator() {
        return new Iterator<Component>() {
            private final Iterator<Connector> portletIterator = getState().portletConnectors.iterator(); 
            
            @Override
            public void remove() {
                portletIterator.remove();
            }
            
            @Override
            public Component next() {
                return (Portlet)portletIterator.next();
            }
            
            @Override
            public boolean hasNext() {
                return portletIterator.hasNext();
            }
        };
    }
}
