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
package org.vaadin.addon.portallayout.gwt.client.portal.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import org.vaadin.addon.portallayout.gwt.client.dnd.StackPortalDropController;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalLayoutUtil;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalView;
import org.vaadin.addon.portallayout.gwt.client.portal.strategy.PortalHeightRedistributionStrategy;
import org.vaadin.addon.portallayout.gwt.client.portal.strategy.StackHeightRedistributionStrategy;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletChrome;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletConnector;
import org.vaadin.addon.portallayout.gwt.shared.portal.PortalLayoutState;
import org.vaadin.addon.portallayout.gwt.shared.portal.rpc.PortalServerRpc;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ComponentConstants;
import com.vaadin.shared.communication.URLReference;

/**
 * PortalWithExtensionConnector.
 */
public abstract class PortalLayoutConnector extends AbstractLayoutConnector implements PortalView.Presenter {

    /**
     * PortalPickupDragController.
     */
    private static final class PortalPickupDragController extends PickupDragController {
        String moveablePanelStyleHack = null;

        private PortalPickupDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
            super(boundaryPanel, allowDroppingOnBoundaryPanel);
        }

        protected void setMoveablePanelStyleName(String styleName) {
            this.moveablePanelStyleHack = styleName;
        }

        @Override
        public void dragStart() {
            super.dragStart();
            context.selectedWidgets.get(0).getParent().addStyleName("v-app");
            context.selectedWidgets.get(0).getParent().addStyleName(moveablePanelStyleHack);
        }

        public boolean isMoveablePanelStyleSet() {
            return moveablePanelStyleHack != null;
        }
    }

    private PortalServerRpc rpc;

    private final static PortalPickupDragController commonDragController = new PortalPickupDragController(
            RootPanel.get(), false);

    private ComponentConnector incomingPortletCandidate;

    private ComponentConnector outcomingPortletCandidate;

    private PortalView view;

    private DropController dropController;

    private PortalHeightRedistributionStrategy heightRedistributionStrategy;

    private final List<ComponentConnector> headerConnectors = new ArrayList<ComponentConnector>();

    private final ElementResizeListener slotResizeListener = new ElementResizeListener() {
        @Override
        public void onElementResize(ElementResizeEvent event) {
            /**
             * We defer recalculation of heights so that other listeners first update the size values.
             */
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    recalculateHeights();
                }
            });
        }
    };

    @Override
    protected void init() {
        super.init();
        rpc = initRpc();
        this.heightRedistributionStrategy = initHeightRedistributionStrategy();
        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
            }
        });
    }

    protected abstract PortalServerRpc initRpc();

    protected PortalHeightRedistributionStrategy initHeightRedistributionStrategy() {
        return new StackHeightRedistributionStrategy();
    }

    protected PortalServerRpc getServerRpc() {
        return rpc;
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        if (!commonDragController.isMoveablePanelStyleSet()) {
            String themeUri = getConnection().getThemeUri();
            commonDragController.setMoveablePanelStyleName(themeUri.substring(themeUri.lastIndexOf("/") + 1));
        }

        boolean spacing = getState().spacing;
        getWidget().setStyleName("v-portal-layout-no-spacing", !spacing);
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        if (getState().contentToPortlet.get(connector) != null) {
            final PortletConnector pc = (PortletConnector) getState().contentToPortlet.get(connector);
            pc.setCaption(connector.getState().caption);
            URLReference iconRef = connector.getState().resources.get(ComponentConstants.ICON_RESOURCE);
            pc.setIcon(iconRef != null ? iconRef.getURL() : null);
        }
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        final List<ComponentConnector> children = getChildComponents();
        final List<ComponentConnector> oldChildren = event.getOldChildren();
        oldChildren.removeAll(children);
        headerConnectors.clear();
        for (final ComponentConnector cc : oldChildren) {
            final PortletConnector pc = PortalLayoutUtil.getPortletConnectorForContent(cc);
            if (pc != null) {
                view.removePortlet(pc.getWidget());
            }
        }

        final Iterator<ComponentConnector> it = children.iterator();
        while (it.hasNext()) {
            final ComponentConnector cc = it.next();
            if (getState().contentToPortlet.get(cc) != null) {
                final PortletConnector pc = (PortletConnector) getState().contentToPortlet.get(cc);
                final PortletChrome portletWidget = pc.getWidget();
                cc.getLayoutManager().addElementResizeListener(portletWidget.getAssociatedSlot().getElement(), slotResizeListener);
                getView().addPortlet(pc.getWidget());
            } else {
                headerConnectors.add(cc);
            }
        }
    }

    public void setIncomingPortletCandidate(PortletChrome portletWidget) {
        assert portletWidget != null;
        ComponentConnector pc = Util.findConnectorFor(portletWidget.getContentWidget());
        if (this.outcomingPortletCandidate == pc) {
            this.outcomingPortletCandidate = null;
        } else if (!getChildComponents().contains(pc)) {
            this.incomingPortletCandidate = pc;
        }
    }

    public void setOutcomingPortletCandidate(PortletChrome portletWidget) {
        assert portletWidget != null;
        ComponentConnector pc = Util.findConnectorFor(portletWidget.getContentWidget());
        if (this.incomingPortletCandidate == pc) {
            this.incomingPortletCandidate = null;
        } else if (getChildComponents().contains(pc)) {
            this.outcomingPortletCandidate = pc;
        }
    }

    @Override
    public Panel getWidget() {
        return (Panel) super.getWidget();
    }

    @Override
    public PortalView getView() {
        return view;
    }

    @Override
    protected Panel createWidget() {
        this.view = initView();
        this.dropController = initDropController();
        commonDragController.registerDropController(dropController);
        return view.asWidget();
    }

    protected DropController initDropController() {
        return new StackPortalDropController(this);
    }

    protected abstract PortalView initView();

    @Override
    public PortalLayoutState getState() {
        return (PortalLayoutState) super.getState();
    }

    @Override
    public void recalculateHeights() {
        getHeightRedistributionStrategy().redistributeHeights(this);
    }

    public void propagateHierarchyChangesToServer() {
        if (outcomingPortletCandidate != null) {
            rpc.removePortlet(outcomingPortletCandidate);
            outcomingPortletCandidate = null;
        }

        if (incomingPortletCandidate != null) {
            updatePortletPositionOnServer(incomingPortletCandidate);
            incomingPortletCandidate = null;
        }
    }

    public abstract void updatePortletPositionOnServer(ComponentConnector cc);

    public List<ComponentConnector> getCurrentChildren() {
        List<ComponentConnector> result = new ArrayList<ComponentConnector>(getChildComponents()) {
            @Override
            public boolean add(ComponentConnector cc) {
                return cc != null && super.add(cc);
            }
        };
        result.removeAll(headerConnectors);
        result.remove(outcomingPortletCandidate);
        result.add(incomingPortletCandidate);
        return result;
    }

    public void removePortlet(ServerConnector connector) {
        rpc.removePortlet(connector);
    }

    public PortalHeightRedistributionStrategy getHeightRedistributionStrategy() {
        return heightRedistributionStrategy;
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        final PickupDragController dragController = getDragController();
        for (final ComponentConnector cc : getCurrentChildren()) {
            PortalLayoutUtil.lockPortlet((PortletConnector) cc);
        }
        dragController.unregisterDropController(dropController);
    }

    public PickupDragController getDragController() {
        return commonDragController;
    }
}
