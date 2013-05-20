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
package org.vaadin.addons.portallayout.portlet;

import org.vaadin.addons.portallayout.gwt.shared.portlet.PortletState;
import org.vaadin.addons.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;
import org.vaadin.addons.portallayout.portal.PortalBase;

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
                getPortal().firePortletCollapseEvent(Portlet.this);
            }

            @Override
            public void updatePixelWidth(int widthPixels) {
                getState().width = widthPixels + "px";
            }

            @Override
            public void updatePixelHeight(int heightPixels) {
                getState().height = heightPixels + "px";
            }

            @Override
            public void close() {
                getPortal().closePortlet(Portlet.this);
            }
        });
    }

    /**
     * Get instance of {@link PortalBase} that holds the current {@link Portlet}.
     * @return parent portal.
     */
    private PortalBase getPortal() {
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

    /**
     * Extend a {@link Component} with portlet chrome.
     * @param content component to be extended.
     */
    public void wrap(Component content) {
        extend((AbstractClientConnector)content);
    }

    /**
     * Adds a component to the header of the current {@link Portlet}.
     * @param header Component to be displayed in the header.
     */
    public void setHeaderComponent(Component header) {
        if (getState().headerComponent != null) {
            ((Component)getState().headerComponent).setParent(null);
        }
        getState().headerComponent = header;
        header.setParent(getParent().getParent());
    }


    /**
     * Get {@link Component} wrapped in the current {@link Portlet}.
     * @return wrapped component.
     */
    public Component getContent() {
        return getParent() == null ? null : getParent();
    }

    /**
     * Get {@link Component} displayed in the header.
     * @return
     */
    public Component getHeaderComponent() {
        return getState().headerComponent == null ? null : (Component)getState().headerComponent;
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

    @Override
    protected PortletState getState(boolean markAsDirty) {
        return (PortletState) super.getState(markAsDirty);
    }

    public void setContentPixelWidth(String width) {
        getState().contentPixelWidth = width;
    }

    public String getPreferredFixedContentWidth() {
        return getState().contentPixelWidth;
    }

    public void setCaption(String string) {
        getState().caption = string;
    }


    @Override
    public void remove() {
        getParent().setHeight(getState().height);
        getParent().setWidth(getState().width);
        super.remove();
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        delegateSizeManagement(initial);
    }

    @Override
    public Component getParent() {
        return (Component) super.getParent();
    }

    private void delegateSizeManagement(boolean initial) {
        final Component c = getParent();
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
        getParent().beforeClientResponse(initial);
    }

}
