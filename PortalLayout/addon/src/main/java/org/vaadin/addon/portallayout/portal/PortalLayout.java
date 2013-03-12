/*
 * Copyright 2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.addon.portallayout.portal;

import java.util.Iterator;
import java.util.LinkedList;

import org.vaadin.addon.portallayout.gwt.shared.portal.PortalLayoutState;
import org.vaadin.addon.portallayout.gwt.shared.portal.rpc.PortalServerRpc;
import org.vaadin.addon.portallayout.portlet.Portlet;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.Extension;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Layout.MarginHandler;

/**
 * PortalWithExtension.
 */
@StyleSheet("portallayout_styles.css")
public class PortalLayout extends AbstractComponent implements MarginHandler, HasComponents {

    public PortalLayout() {
        setStyleName("v-portal-layout");
        registerRpc(new PortalServerRpc() {

            @Override
            public void removePortlet(Connector portlet) {
                PortalLayout.this.removePortlet((Component) portlet);
            }

            @Override
            public void updatePortletPosition(Connector portlet, int index) {
                if (index >= 0) {
                    addComponentAt((Component) portlet, index);
                }
            }
        });
    }

    private void addComponentAt(Component c, int index) {
        Portlet portlet = getPortletForComponent(c);
        LinkedList<Connector> portlets = (LinkedList<Connector>) getState().portletConnectors;
        if (portlets.contains(portlet)) {
            portlets.remove(portlet);
        }
        portlets.add(index, portlet);
    }

    public Portlet wrapInPortlet(Component c) {
        Portlet result = getPortletForComponent(c);
        return result;
    }

    private Portlet getPortletForComponent(Component c) {
        Portlet result = (Portlet) getState().contentToPortlet.get(c);
        if (result != null) {
            return result;
        } else {
            for (Extension extension : c.getExtensions()) {
                if (extension instanceof Portlet) {
                    addPortletMapping(c, (Portlet) extension);
                    return (Portlet) extension;
                }
            }
        }
        result = new Portlet(c);
        addPortletMapping(c, result);
        return result;
    }

    public void removePortlet(Component portletContent) {
        Portlet portlet = (Portlet) getState().contentToPortlet.remove(portletContent);
        if (portlet != null) {
            getState().portletConnectors.remove(portlet);
            if (portletContent.getParent() == this) {
                portletContent.setParent(null);
            }
        } else {
            throw new IllegalArgumentException("Portal does not contain portlet with content "
                    + portletContent.getConnectorId());   
        }
    }

    public void removePortlet(Portlet portlet) {
        removePortlet((Component) portlet.getParent());
    }

    private void addPortletMapping(Component c, Portlet result) {
        getState().contentToPortlet.put(c, result);
        getState().portletConnectors.add(result);

        if (c instanceof ComponentContainer) {
            for (Component parent = this; parent != null; parent = parent.getParent()) {
                if (parent == c) {
                    throw new IllegalArgumentException("Component cannot be added inside it's own content");
                }
            }
        }
        c.setParent(null);
        c.setParent(this);
    }

    @Override
    protected PortalLayoutState getState() {
        return (PortalLayoutState) super.getState();
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
    
    @Override
    public Iterator<Component> iterator() {
        return new Iterator<Component>() {
            final Iterator<Connector> wrappedIt = getState().portletConnectors.iterator();

            @Override
            public void remove() {
                wrappedIt.remove();
            }

            @Override
            public Component next() {
                return (Component) wrappedIt.next().getParent();
            }

            @Override
            public boolean hasNext() {
                return wrappedIt.hasNext();
            }
        };
    }

    public Portlet getPortlet(Component c) {
        return (Portlet) getState().contentToPortlet.get(c);
    }
}
