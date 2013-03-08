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

import org.vaadin.addon.portallayout.gwt.client.portal.connection.PortalWithExtensionConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEvent;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent;
import org.vaadin.addon.portallayout.gwt.shared.portlet.PortletExState;
import org.vaadin.addon.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;
import org.vaadin.addon.portallayout.portlet.PortletEx;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
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
@Connect(PortletEx.class)
public class PortletExConnector extends AbstractExtensionConnector implements PortletCollapseEvent.Handler, PortletCloseEvent.Handler {

    private final PortletServerRpc rpc = RpcProxy.create(PortletServerRpc.class, this);
    
    private final PortletWidget widget = new PortletWidget();
    
    private boolean isHeightRelative = false;
    
    @Override
    protected void init() {
        super.init();
        widget.getHeader().addPortletCollapseEventHandler(this);
        widget.getHeader().addPortletCloseEventHandler(this);
        addStateChangeHandler("isCollapsed", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                boolean isCollapsed = widget.isCollapsed();
                if (isCollapsed != getState().isCollapsed) {
                    widget.getSlot().setStyleName("collapsed", getState().isCollapsed);
                    if (getState().isCollapsed) {
                        widget.getSlot().setHeight(widget.getHeader().getOffsetHeight() + "px");   
                    } else {
                        widget.getSlot().getElement().getStyle().clearHeight();
                    }   
                }
            }
        });
    }
    
    @Override
    protected void extend(ServerConnector target) {
        ComponentConnector cc = (ComponentConnector)target;
        Widget w = cc.getWidget();
        widget.setContentWidget(w);
        cc.addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                ComponentConnector cc = (ComponentConnector)event.getConnector();
                isHeightRelative = (ComponentStateUtil.isRelativeHeight(cc.getState()));
                widget.getContentElement().getStyle().setPosition(isHeightRelative ? Position.ABSOLUTE : Position.STATIC);
            }
        });
        
        cc.getLayoutManager().addElementResizeListener(widget.getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                if (!isHeightRelative) {
                    int staticHeight = e.getLayoutManager().getOuterHeight(e.getElement());
                    setSlotHeight(staticHeight + "px");
                }
            }
        });
    }
    
    public PortletWidget getWidget() {
        return widget;
    }

    public boolean isCollased() {
        return false;
    }
    
    @Override
    public PortletExState getState() {
        return (PortletExState)super.getState();
    }

    public void setSlotHeight(String height) { 
        widget.getSlot().setHeight(height);
    }

    public void setCaption(String caption) {
        widget.getHeader().setCaptionText(caption);
    }

    public void setIcon(String url) {
        widget.getHeader().setIcon(url);
    }

    @Override
    public void onPortletClose(PortletCloseEvent e) {
        ((PortalWithExtensionConnector)getParent().getParent()).removePortlet(getParent());
    }

    @Override
    public void onPortletCollapse(PortletCollapseEvent e) {
        rpc.setCollapsed(!getState().isCollapsed);
    }
    
    @Override
    public void onUnregister() {
        super.onUnregister();
        widget.removeFromParent();
    }
}
