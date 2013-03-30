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
import java.util.List;
import java.util.NoSuchElementException;

import org.vaadin.addon.portallayout.gwt.shared.portal.PortalLayoutState;
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
 * Base class for Portal Layouts.
 */
public abstract class PortalBase extends AbstractComponent implements MarginHandler, HasComponents {


    /**
     * Constructs a {@link StackPortalLayout}.
     */
    public PortalBase() {
        setStyleName("v-portal-layout");
    }

    /**
     * Wraps the provided component into a {@link Portlet} instance.
     * @param c Component to be wrapped into a {@link Portlet}.
     * @return created {@link Portlet}.
     */
    public Portlet portletFor(Component c) {
        return getOrCreatePortletForComponent(c);
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
     * @param portlet
     *            {@link Portlet} to be removed.
     */
    public void removePortlet(Portlet portlet) {
        Component portletContent = (Component) portlet.getParent();
        getState().portlets().remove(portlet);
        if (portletContent.getParent() == this) {
            portletContent.setParent(null);
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
    
    @Override
    public Iterator<Component> iterator() {
        CombinedIterator<Component> cIt = new CombinedIterator<Component>();
        cIt.addIterator(portletContentIterator());
        cIt.addIterator(portletHeaderIterator());
        return cIt;
    }

    protected Portlet getPortlet(Component c) {
        return (Portlet) getState().contentToPortlet.get(c);
    }
    
    /**
     * Finds or creates a Portlet based on the Component. In case there is no
     * Portlet existing for the given Component - a new Portlet is created, if
     * there is a Portlet but it belongs to a different PortalLayout, current
     * Portal steals the Component from it.
     * 
     * @param c
     *            content Component.
     * @return either Portlet instance from own mapping, other portlets mapping
     *         or a newly created instance.
     */
    protected Portlet getOrCreatePortletForComponent(Component c) {
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
        result = createPortlet(c);
        return result;
    }

    protected Portlet createPortlet(Component c) {
        Portlet result = new Portlet(c);
        addPortletMapping(c, result);
        return result;
    }

    private void addPortletMapping(Component c, Portlet result) {
        getState().contentToPortlet.put(c, result);
        getState().portlets().add(result);

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
        private final Iterator<Connector> wrappedIt = getState().portlets().iterator();
        
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
            for (Connector portletConnector : getState().portlets()) {
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
