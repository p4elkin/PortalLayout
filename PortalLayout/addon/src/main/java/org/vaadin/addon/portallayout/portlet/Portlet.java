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
package org.vaadin.addon.portallayout.portlet;

import org.vaadin.addon.portallayout.gwt.shared.portlet.PortletState;
import org.vaadin.addon.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;

/**
 * Extends a component on the client-side by providing a chrome with controls, icon and a caption.
 */
public class Portlet extends AbstractExtension {

    /**
     * Constructs an empty {@link Portlet} which is not bound to any component.
     */
    public Portlet() {
        registerRpc(new PortletServerRpc() {
            @Override
            public void setCollapsed(boolean isCollapsed) {
                getState().collapsed = isCollapsed;
            }
        });
    }
    
    /**
     * Constructs a {@link Portlet} bound to a provided component.
     * @param portletContent Portlet Content.
     */
    public Portlet(Component portletContent) {
        this();
        wrap(portletContent);
    }
    
    public void wrap(Component content) {
        extend((AbstractClientConnector)content);
    }
    
    public void setCollapsed(boolean collapsed) {
        getState().collapsed = collapsed;
    }
    
    public void setClosable(boolean closable) {
        getState().closable = closable;
    }
    
    public void setCollapsible(boolean collapsible) {
        getState().collapsible = collapsible;
    }
    
    public void setLocked(boolean locked) {
        getState().locked = locked;
    }
    
    public boolean isLocked() {
        return getState().locked;
    }
    
    public boolean isCollapsed() {
        return getState().collapsed;
    }
    
    public boolean isCollapsible() {
        return getState().collapsible;
    }
    
    public boolean isClosable() {
        return getState().closable;
    }
    
    @Override
    protected PortletState getState() {
        return (PortletState)super.getState();
    }
}
