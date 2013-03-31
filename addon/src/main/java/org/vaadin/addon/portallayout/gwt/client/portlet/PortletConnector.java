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

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.ComponentStateUtil;
import com.vaadin.shared.ui.Connect;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalLayoutUtil;
import org.vaadin.addon.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEvent;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent;
import org.vaadin.addon.portallayout.gwt.shared.portlet.PortletState;
import org.vaadin.addon.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;
import org.vaadin.addon.portallayout.portal.StackPortalLayout;
import org.vaadin.addon.portallayout.portlet.Portlet;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side connector that corresponds to {@link Portlet}.
 */
@Connect(Portlet.class)
public class PortletConnector extends AbstractExtensionConnector implements PortletCollapseEvent.Handler, PortletCloseEvent.Handler {

    /**
     * In case portlet has an unspecified height - it could resize if the contents shrink/expand.
     * Slot should shrink/extend as well.
     */
    private final class UndefinedHeightResizeListener implements ElementResizeListener {
        @Override
        public void onElementResize(ElementResizeEvent e) {
            if (isHeightUndefined) {
                portletChrome.getAssociatedSlot().setHeight(layoutManager.getOuterHeight(e.getElement()) + "px");
            }
        }
    }

    /**
     * Triggered when header component is changed from the server-side.
     */
    private final class HeaderToolbarStateHandler implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent e) {
            Widget toolbar = getState().headerComponent == null ? null : ((ComponentConnector)getState().headerComponent).getWidget(); 
            portletChrome.setHeaderToolbar(toolbar);
        }
    }

    /**
     * Triggered when height is changed from the server-side.
     */
    private final class HeightStateChangeListener implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent event) {
            isHeightRelative = ComponentStateUtil.isRelativeHeight(getState());
            isHeightUndefined = ComponentStateUtil.isUndefinedHeight(getState());

            portletChrome.updateContentStructure(isHeightUndefined);
            portletChrome.getAssociatedSlot().setHeight(getState().height);
        }
    }

    /**
     * Triggered when width is changed from the server-side.
     */
    public class WidthStateChangeHandler implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent event) {
            portletChrome.getAssociatedSlot().setWidth(getState().width);   
        }
    }
    
    /**
     * Triggered when collapse state is changed from the server-side.
     */
    private final class CollapseStateChangeListener implements StateChangeHandler {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            boolean isCollapsed = portletChrome.isCollapsed();
            if (getState().collapsed != isCollapsed) {

                String slotHeight;
                if (getState().collapsed)  {
                    slotHeight = portletChrome.getHeader().getOffsetHeight() + "px";
                } else {
                    if (isHeightUndefined) {
                        slotHeight = layoutManager.getOuterHeight(portletChrome.getElement()) + "px";
                    } else {
                        slotHeight = getState().height;
                    }
                }

                final String portletHeight = getState().collapsed ?
                        layoutManager.getOuterHeight(portletChrome.getElement()) + "px" :
                        getParent().getState().height;

                portletChrome.setStyleName("collapsed", getState().collapsed);
                portletChrome.getAssociatedSlot().setHeight(slotHeight);
                portletChrome.setHeight(portletHeight);

                /**
                 * In case portlet is expanded - it might need to occupy a different area than before,
                 * so we cause a layout phase.
                 */
                if (!getState().collapsed) {
                    layoutManager.setNeedsMeasure((ComponentConnector) portletChrome.getParent());
                    layoutManager.layoutLater();
                }
            }
        }
    }

    /**
     * In case a {@link PortletChrome} is dragged from a portal with no width
     * restriction to the one that has width restrictions (like
     * {@link StackPortalLayout}) and sets the width to a {@link PortletChrome},
     * we save the new width and send it to server.
     */
    private final class SlotSizeChangeListener implements ElementResizeListener {
        @Override
        public void onElementResize(final ElementResizeEvent e) {
            if (!isCollapsed()) {
                rpc.updatePixelWidth(layoutManager.getOuterWidth(e.getElement()));
                /**
                 * In case slot was re-sized, state will still contain relative height
                 * whereas slot already has pixels. This is why we check the actual state of slot here.
                 */
                if (Util.parseRelativeSize(portletChrome.getAssociatedSlot().getHeight()) < 0 && !isHeightUndefined) {
                    isHeightRelative = false;
                    rpc.updatePixelHeight(layoutManager.getOuterHeight(e.getElement()));
                }
            }
        }
    }
    
    private final Map<String, StateChangeHandler> stateChangeHandlers = new HashMap<String, StateChangeHandler>();

    private final PortletServerRpc rpc = RpcProxy.create(PortletServerRpc.class, this);

    private final PortletChrome portletChrome = new PortletChrome();

    private boolean isHeightRelative = false;
    
    public boolean isHeightUndefined = false;

    private LayoutManager layoutManager;
    
    @Override
    protected void init() {
        super.init();
        portletChrome.getHeader().addPortletCollapseEventHandler(this);
        portletChrome.getHeader().addPortletCloseEventHandler(this);
        stateChangeHandlers.put("collapsed", new CollapseStateChangeListener());
        stateChangeHandlers.put("headerComponent", new HeaderToolbarStateHandler());
        stateChangeHandlers.put("collapsible", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent e) {
                portletChrome.getHeader().setCollapsible(getState().collapsible);
            }
        });
        stateChangeHandlers.put("closable", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent e) {
                portletChrome.getHeader().setClosable(getState().closable);
            }
        });
        stateChangeHandlers.put("locked", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent e) {
                boolean locked = getState().locked;
                if (!locked) {
                    PortalLayoutUtil.unlockPortlet(PortletConnector.this);
                } else {
                    PortalLayoutUtil.lockPortlet(PortletConnector.this);
                }
            }
        });
        
        for (final Map.Entry<String, StateChangeHandler> entry : stateChangeHandlers.entrySet()) {
            addStateChangeHandler(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected void extend(ServerConnector target) {
        ComponentConnector cc = (ComponentConnector) target;
        Widget w = cc.getWidget();
        portletChrome.setContentWidget(w);
        addStateChangeHandler("height", new HeightStateChangeListener());
        addStateChangeHandler("width", new WidthStateChangeHandler());
        PortletSlot slot = portletChrome.getAssociatedSlot();
        layoutManager = cc.getLayoutManager();
        layoutManager.addElementResizeListener(slot.getElement(), new SlotSizeChangeListener());
        layoutManager.addElementResizeListener(portletChrome.getElement(), new UndefinedHeightResizeListener());
    }

    public PortletChrome getWidget() {
        return portletChrome;
    }

    public boolean isCollapsed() {
        return portletChrome.isCollapsed();
    }

    @Override
    public PortletState getState() {
        return (PortletState) super.getState();
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
        portletChrome.getAssociatedSlot().removeFromParent();
        portletChrome.removeFromParent();
        super.onUnregister();
    }

    @Override
    public ComponentConnector getParent() {
        return super.getParent() == null ? null : (ComponentConnector) super.getParent();
    }

    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public boolean hasRelativeHeight() {
        return isHeightRelative;
    }
}
