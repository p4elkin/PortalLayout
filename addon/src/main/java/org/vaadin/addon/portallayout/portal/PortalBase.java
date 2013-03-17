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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.vaadin.addon.portallayout.gwt.shared.portal.PortalLayoutState;
import org.vaadin.addon.portallayout.gwt.shared.portal.rpc.PortalServerRpc;
import org.vaadin.addon.portallayout.portlet.Portlet;

import com.vaadin.server.Extension;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Layout.MarginHandler;

/**
 * PortalBase.
 */
public abstract class PortalBase extends AbstractComponent implements MarginHandler, HasComponents {


    /**
     * Constructs a {@link PortalLayout}.
     */
    public PortalBase() {
        setStyleName("v-portal-layout");
        registerRpc(new PortalServerRpc() {

            @Override
            public void removePortlet(Connector portlet) {
                PortalBase.this.removePortlet((Component) portlet);
            }

            @Override
            public void updatePortletPosition(Connector portlet, int index) {
                if (index >= 0) {
                    addComponentAt((Component) portlet, index);
                }
            }
        });
    }

    /**
     * 
     * @param c
     *            Component to be wrapped into a {@link Portlet}.
     * @return created {@link Portlet}.
     */
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

    /**
     * Finds the correspondent {@link Portlet} and removes it.
     * 
     * @param portletContent
     *            Content Component.
     */
    public void removePortlet(Component portletContent) {
        removePortlet((Portlet) getState().contentToPortlet.remove(portletContent));

    }

    /**
     * Removes a {@link Portlet} from current layout.
     * 
     * @param portletContent
     *            {@link Portlet} to be removed.
     */
    public void removePortlet(Portlet portlet) {
        Component portletContent = (Component) portlet.getParent();
        if (portlet != null) {
            getState().portletConnectors.remove(portlet);
            if (portletContent.getParent() == this) {
                portletContent.setParent(null);
            }
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

    private void addComponentAt(Component c, int index) {
        Portlet portlet = getPortletForComponent(c);
        LinkedList<Connector> portlets = (LinkedList<Connector>) getState().portletConnectors;
        if (portlets.contains(portlet)) {
            portlets.remove(portlet);
        }
        portlets.add(index, portlet);
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
    public Iterator<Component> iterator() {
        CombinedIterator<Component> cIt = new CombinedIterator<Component>();
        cIt.addIterator(portletContentIterator());
        cIt.addIterator(portletHeaderIterator());
        return cIt;
    }

    public Portlet getPortlet(Component c) {
        return (Portlet) getState().contentToPortlet.get(c);
    }
    

    protected Iterator<Component> portletContentIterator() {
        return new PortletContentIterator();
    }

    protected Iterator<Component> portletHeaderIterator() {
        return new PortletHeaderIterator();
    }
    
    private static final class CombinedIterator<T> implements Iterator<T>, Serializable {

        private final Collection<Iterator<? extends T>> iterators = new ArrayList<Iterator<? extends T>>();
        
        public void addIterator(Iterator<? extends T> iterator) {
            iterators.add(iterator);
        }

        @Override
        public boolean hasNext() {
            for (Iterator<? extends T> i : iterators) {
                if (i.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public T next() {
            for (Iterator<? extends T> i : iterators) {
                if (i.hasNext()) {
                    return i.next();
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * IteratorImplementation.
     */
    private final class PortletContentIterator implements Iterator<Component> {
        private final Iterator<Connector> wrappedIt = getState().portletConnectors.iterator();
        
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
    }
    
    private final class PortletHeaderIterator implements Iterator<Component> {
        
        private final Iterator<Component> headersIt;
        
        public PortletHeaderIterator() {
            List<Component> headers = new ArrayList<Component>();
            for (Connector portletConnector : getState().portletConnectors) {
                Portlet p = (Portlet) portletConnector;
                if (p.getHeaderComponent() != null) {
                    headers.add(p.getHeaderComponent());
                }
            }
            this.headersIt = headers.iterator();
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Component next() {
            return headersIt.next();
        }
        
        @Override
        public boolean hasNext() {
            return headersIt.hasNext();
        }
    }

}
