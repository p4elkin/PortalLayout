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
package org.vaadin.addon.portallayout.container;

import java.util.Iterator;
import java.util.LinkedList;

import org.vaadin.addon.portallayout.gwt.shared.container.PortalColumnsState;
import org.vaadin.addon.portallayout.portal.PortalBase;
import org.vaadin.addon.portallayout.portal.StackPortalLayout;

import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

/**
 * PortalColumns.
 * @deprecated This class is not for production use - it is very experimental and has a super unstable API as well as lacks 
 * most of functionality. 
 */
@Deprecated
public class PortalColumns extends AbstractComponent implements HasComponents {

    public PortalColumns() {
    }
    
    public PortalColumns(int size) {
        for (int i = 0; i < size; ++i) {
            appendPortal();
        }
    }
    
    public void resize(int size) {
        boolean shrinking = getState().portalLayouts.size() > size;
        while (getState().portalLayouts.size() != size) {
            if (shrinking) {
                removeLastPortal();
            } else {
                appendPortal();
            }
        }
    }
    
    public void removeLastPortal() {
        PortalBase lastPortal = (PortalBase)((LinkedList<Connector>)getState().portalLayouts).pollLast();
        if (lastPortal != null) {
            lastPortal.setParent(null);
        }
    }

    public void appendPortal() {
        appendPortal(new StackPortalLayout());
    }
    
    public void appendPortal(PortalBase portalLayout) {
        if (getState().portalLayouts.contains(portalLayout)) {
            throw new IllegalArgumentException("Already contains this portal.");
        }
        getState().portalLayouts.add(portalLayout);
        portalLayout.setParent(this);
    }
    
    public PortalBase getPortalLayout(int position) {
        if (getState().portalLayouts.size() <= position) {
            throw new IllegalArgumentException();
        }
        return (StackPortalLayout)getState().portalLayouts.get(position);
    }
    
    @Override
    protected PortalColumnsState getState() {
        return (PortalColumnsState)super.getState();
    }
    
    @Override
    public Iterator<Component> iterator() {
        return new PortalLayoutIterator();
    }

    private final class PortalLayoutIterator implements Iterator<Component> {
        
        private final Iterator<Connector> wrappedIt = getState().portalLayouts.iterator();
        
        @Override
        public void remove() {
            wrappedIt.remove();
        }
        
        @Override
        public Component next() {
            return (Component) wrappedIt.next();
        }
        
        @Override
        public boolean hasNext() {
            return wrappedIt.hasNext();
        }
    }
}
