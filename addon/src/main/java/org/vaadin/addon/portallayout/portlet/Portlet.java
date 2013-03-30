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

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import org.vaadin.addon.portallayout.gwt.shared.portlet.PortletState;
import org.vaadin.addon.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;
import org.vaadin.addon.portallayout.portal.PortalBase;

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
                getPortalLayout().firePortletCollapseEvent(Portlet.this);
            }

            @Override
            public void updatePreferredPixelWidth(int widthPixels) {
                getState().fixedContentWidth = widthPixels + "px";
            }

            @Override
            public void updatePixelHeight(int heightPixels) {
                getState().height = heightPixels + "px";
            }
        });
    }

    private PortalBase getPortalLayout() {
        return getContent().getParent() == null ? null : (PortalBase) getContent().getParent();
    }

    /**
     * Constructs a {@link Portlet} bound to a provided component.
     * @param portletContent Portlet Content.
     */
    public Portlet(Component portletContent) {
        this();
        wrap(portletContent);
    }
    
    @Override
    public void remove() {
        super.remove();
        markAsDirty();
    }
    
    public void wrap(Component content) {
        extend((AbstractClientConnector)content);
    }
    
    public void setHeaderComponent(Component header) {
        if (getState().headerComponent != null) {
            ((Component)getState().headerComponent).setParent(null);
        }
        getState().headerComponent = header;
        header.setParent(getParent().getParent());
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

    public void setPreferredFixedContentWidth(String width) {
        getState().fixedContentWidth = width;
    }

    public String getPreferredFixedContentWidth() {
        return getState().fixedContentWidth;
    }

    public void setCaption(String string) {
        getState().caption = string;
    }

    public Component getContent() {
        return getParent() == null ? null : (Component)getParent();
    }

    public Component getHeaderComponent() {
        return getState().headerComponent == null ? null : (Component)getState().headerComponent;
    }
    
    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        final Component c = (Component)getParent();
        String width = String.format("%d%s", (int)c.getWidth(), c.getWidthUnits().getSymbol());
        
        if (c.getWidth() >= 0 && !"100%".equals(width)) {
            c.setWidth("100%");
            getState().width = width;
        }
        
        String height = String.format("%d%s", (int)c.getHeight(), c.getHeightUnits().getSymbol());
        if (c.getHeight() >= 0 && !"100%".equals(height)) {
            c.setHeight("100%");
            getState().height = height;   
        }
        
        c.beforeClientResponse(initial);
    }
    
}
