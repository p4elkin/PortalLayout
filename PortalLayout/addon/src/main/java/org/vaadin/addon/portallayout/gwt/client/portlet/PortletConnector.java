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
public class PortletConnector extends AbstractExtensionConnector implements PortletCollapseEvent.Handler, PortletCloseEvent.Handler {

    private LayoutManager layoutManager;
    
    /**
     * heightStateChangeListener.
     */
    private final class HeightStateChangeListener implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent event) {
            ComponentConnector cc = (ComponentConnector)event.getConnector();
            isHeightRelative = (ComponentStateUtil.isRelativeHeight(cc.getState()));
            widget.updateContentStructure(isHeightRelative);
        }
    }

    /**
     * CollapseStateChangeListener.
     */
    private final class CollapseStateChangeListener implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            boolean isCollapsed = widget.isCollapsed();
            if (isCollapsed != getState().isCollapsed) {
                widget.setStyleName("collapsed", getState().isCollapsed);
                if (getState().isCollapsed) {
                    widget.getSlot().setHeight(widget.getHeader().getOffsetHeight() + "px");   
                } else {
                    widget.getSlot().getElement().getStyle().clearHeight();
                }   
                layoutManager.setNeedsMeasure((ComponentConnector)getParent());
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
            widget.resizeContent(e.getLayoutManager().getInnerHeight(e.getElement()));
        }
    }

    private final PortletServerRpc rpc = RpcProxy.create(PortletServerRpc.class, this);
    
    private final PortletWidget widget = new PortletWidget();
    
    private boolean isHeightRelative = false;
    
    @Override
    protected void init() {
        super.init();
        widget.getHeader().addPortletCollapseEventHandler(this);
        widget.getHeader().addPortletCloseEventHandler(this);
        addStateChangeHandler("isCollapsed", new CollapseStateChangeListener());
    }
    
    @Override
    protected void extend(ServerConnector target) {
        ComponentConnector cc = (ComponentConnector)target;
        Widget w = cc.getWidget();
        widget.setContentWidget(w);
        cc.addStateChangeHandler("height", new HeightStateChangeListener());
        
        layoutManager = cc.getLayoutManager(); 
        layoutManager.addElementResizeListener(widget.getElementWrapper(), new ContentAreaSizeChangeListener());
        layoutManager.addElementResizeListener(widget.getElement(), new FixedHeightPortletResizeListener());
    }
    
    public PortletWidget getWidget() {
        return widget;
    }

    public boolean isCollased() {
        return false;
    }
    
    @Override
    public PortletState getState() {
        return (PortletState)super.getState();
    }

    public void setSlotHeight(String slotHeight) { 
        widget.getSlot().setHeight(slotHeight);
    }

    public void setCaption(String caption) {
        widget.getHeader().setCaptionText(caption);
    }

    public void setIcon(String url) {
        widget.getHeader().setIcon(url);
    }

    @Override
    public void onPortletClose(PortletCloseEvent e) {
        ((PortalLayoutConnector)getParent().getParent()).removePortlet(getParent());
    }

    @Override
    public void onPortletCollapse(PortletCollapseEvent e) {
        rpc.setCollapsed(!getState().isCollapsed);
    }
    
    @Override
    public void onUnregister() {
        super.onUnregister();
        widget.getSlot().removeFromParent();
        widget.removeFromParent();
    }

    public void setSlotHeight(String percentSlotSize, double pixelSlotSize) {
        setSlotHeight(percentSlotSize);
        widget.resizeContent((int)(pixelSlotSize - widget.getHeaderHeight()));
    }
}
