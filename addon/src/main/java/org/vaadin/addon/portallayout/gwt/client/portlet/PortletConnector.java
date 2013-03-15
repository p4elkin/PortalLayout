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
package org.vaadin.addon.portallayout.gwt.client.portlet;

import org.vaadin.addon.portallayout.gwt.client.portal.PortalLayoutUtil;
import org.vaadin.addon.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEvent;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent;
import org.vaadin.addon.portallayout.gwt.shared.portlet.PortletState;
import org.vaadin.addon.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;
import org.vaadin.addon.portallayout.portlet.Portlet;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.ComponentStateUtil;
import com.vaadin.shared.ui.Connect;

/**
 * PortletExConnector.
 */
@Connect(Portlet.class)
public class PortletConnector extends AbstractExtensionConnector implements PortletCollapseEvent.Handler,
        PortletCloseEvent.Handler {

    private LayoutManager layoutManager;

    /**
     * heightStateChangeListener.
     */
    private final class HeightStateChangeListener implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent event) {
            ComponentConnector cc = (ComponentConnector) event.getConnector();
            isHeightRelative = (ComponentStateUtil.isRelativeHeight(cc.getState()));
            portletChrome.updateContentStructure(isHeightRelative);
        }
    }

    /**
     * CollapseStateChangeListener.
     */
    private final class CollapseStateChangeListener implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            boolean isCollapsed = portletChrome.isCollapsed();
            if (isCollapsed != getState().collapsed) {
                portletChrome.setStyleName("collapsed", getState().collapsed);
                if (getState().collapsed) {
                    portletChrome.getAssociatedSlot().setHeight(portletChrome.getHeader().getOffsetHeight() + "px");
                } else {
                    portletChrome.getAssociatedSlot().getElement().getStyle().clearHeight();
                    PortalLayoutConnector pc = (PortalLayoutConnector)((ComponentConnector)getParent().getParent());
                    PortalLayoutUtil.recalculatePortletHeights(pc);
                }
            }
        }
    }

    /**
     * FixedHeightPortletResizeListener.
     */
    private final class FixedHeightPortletResizeListener implements ElementResizeListener {
        @Override
        public void onElementResize(ElementResizeEvent e) {
            if (!isHeightRelative) {
                int staticHeight = e.getLayoutManager().getOuterHeight(e.getElement());
                setSlotHeight(staticHeight + "px");
            }
        }
    }

    /**
     * ContentAreaSizeChangeListener.
     */
    private final class ContentAreaSizeChangeListener implements ElementResizeListener {
        @Override
        public void onElementResize(ElementResizeEvent e) {
            portletChrome.resizeContent(e.getLayoutManager().getInnerHeight(e.getElement()));
        }
    }

    private final PortletServerRpc rpc = RpcProxy.create(PortletServerRpc.class, this);

    private final PortletChrome portletChrome = new PortletChrome();

    private boolean isHeightRelative = false;

    @Override
    protected void init() {
        super.init();
        portletChrome.getHeader().addPortletCollapseEventHandler(this);
        portletChrome.getHeader().addPortletCloseEventHandler(this);
        addStateChangeHandler("collapsed", new CollapseStateChangeListener());
        addStateChangeHandler("collapsible", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent e) {
                portletChrome.getHeader().setCollapsible(getState().collapsible);
            }
        });
        
        addStateChangeHandler("closable", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent e) {
                portletChrome.getHeader().setClosable(getState().closable);
                if (!((PortletState)e.getConnector().getState()).closable) {
                    System.out.println();
                }
            }
        });
        
        addStateChangeHandler("locked", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent e) {
                
            }
        });
        
        addStateChangeHandler("headerToolbar", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent e) {
                Widget toolbar = getState().headerToolbar == null ? null : ((ComponentConnector)getState().headerToolbar).getWidget(); 
                portletChrome.setHeaderToolbar(toolbar);
            }
        });
        
        
    }

    @Override
    protected void extend(ServerConnector target) {
        ComponentConnector cc = (ComponentConnector) target;
        Widget w = cc.getWidget();
        portletChrome.setContentWidget(w);
        cc.addStateChangeHandler("height", new HeightStateChangeListener());
        
        layoutManager = cc.getLayoutManager();
        layoutManager.addElementResizeListener(portletChrome.getElementWrapper(), new ContentAreaSizeChangeListener());
        layoutManager.addElementResizeListener(portletChrome.getElement(), new FixedHeightPortletResizeListener());
    }

    public PortletChrome getWidget() {
        return portletChrome;
    }

    public boolean isCollased() {
        return portletChrome.isCollapsed();
    }

    @Override
    public PortletState getState() {
        return (PortletState) super.getState();
    }

    public void setSlotHeight(String slotHeight) {
        portletChrome.getAssociatedSlot().setHeight(slotHeight);
    }

    public void setCaption(String caption) {
        portletChrome.getHeader().setCaptionText(caption);
    }

    public void setIcon(String url) {
        portletChrome.getHeader().setIcon(url);
    }

    @Override
    public void onPortletClose(PortletCloseEvent e) {
        ((PortalLayoutConnector) getParent().getParent()).removePortlet(getParent());
    }

    @Override
    public void onPortletCollapse(PortletCollapseEvent e) {
        portletChrome.getHeader().toggleCollapseStyles(!getState().collapsed);
        rpc.setCollapsed(!getState().collapsed);
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        portletChrome.getAssociatedSlot().removeFromParent();
        portletChrome.removeFromParent();
    }

    public void setSlotHeight(String percentSlotSize, double pixelSlotSize) {
        setSlotHeight(percentSlotSize);
        portletChrome.resizeContent((int) (pixelSlotSize - portletChrome.getHeaderHeight()));
    }
}