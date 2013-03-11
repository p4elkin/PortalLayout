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
 * PortletEx.
 */
public class Portlet extends AbstractExtension {

    public Portlet() {
        registerRpc(new PortletServerRpc() {
            @Override
            public void setCollapsed(boolean isCollapsed) {
                getState().isCollapsed = isCollapsed;
            }
        });
    }
    
    public Portlet(Component c) {
        this();
        wrap(c);
    }
    
    public void wrap(Component c) {
        extend((AbstractClientConnector)c);
    }
    
    public void setCollapsed(boolean collapsed) {
        getState().isCollapsed = collapsed;
    }
    
    public void setClosable(boolean closable) {
        getState().isClosable = closable;
    }
    
    public void setCollapsible(boolean collapsible) {
        getState().isCollapsible = collapsible;
    }
    
    public void setLocked(boolean locked) {
        getState().isLocked = locked;
    }
    
    public boolean isLocked() {
        return getState().isLocked;
    }
    
    public boolean isCollapsed() {
        return getState().isCollapsed;
    }
    
    public boolean isCollapsible() {
        return getState().isCollapsible;
    }
    
    public boolean isClosable() {
        return getState().isClosable;
    }
    
    @Override
    protected PortletState getState() {
        return (PortletState)super.getState();
    }
}
