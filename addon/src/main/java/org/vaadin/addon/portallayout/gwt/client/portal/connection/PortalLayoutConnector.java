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

import org.vaadin.addon.portallayout.gwt.client.dnd.PortalDropController;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalHeightRedistributionStrategy;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalLayoutUtil;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalView;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalViewImpl;
import org.vaadin.addon.portallayout.gwt.client.portal.StackHeightRedistributionStrategy;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletChrome;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletConnector;
import org.vaadin.addon.portallayout.gwt.shared.portal.PortalLayoutState;
import org.vaadin.addon.portallayout.gwt.shared.portal.rpc.PortalServerRpc;
import org.vaadin.addon.portallayout.portal.PortalLayout;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ComponentConstants;
import com.vaadin.shared.communication.URLReference;
import com.vaadin.shared.ui.Connect;

/**
 * PortalWithExtensionConnector.
 */
@Connect(PortalLayout.class)
public class PortalLayoutConnector extends AbstractLayoutConnector implements PortalView.Presenter {

    /**
     * PortalPickupDragController.
     */
    private static final class PortalPickupDragController extends PickupDragController {
        String moveablePanelStyleHack = null;

        private PortalPickupDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
            super(boundaryPanel, allowDroppingOnBoundaryPanel);
        }

        protected void setMoveablePanleStyleName(String styleName) {
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

    private final PortalServerRpc rpc = RpcProxy.create(PortalServerRpc.class, this);

    private final static PortalPickupDragController commonDragController = new PortalPickupDragController(
            RootPanel.get(), false);

    private ComponentConnector incomingPortletCandidate;

    private ComponentConnector outcomingPortletCandidate;

    private PortalView view;

    private DropController dropController;
    
    private PortalHeightRedistributionStrategy heightRedistributionStrategy;
    
    private final ElementResizeListener portletResizeListener = new ElementResizeListener() {
        @Override
        public void onElementResize(ElementResizeEvent event) {
            recalculateHeights();
        }
    };

    @Override
    protected void init() {
        super.init();
        this.heightRedistributionStrategy = initHeightRedistributionStrategy();
        getLayoutManager().setNeedsMeasure(this);
    }

    protected PortalHeightRedistributionStrategy initHeightRedistributionStrategy() {
        return new StackHeightRedistributionStrategy();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        if (!commonDragController.isMoveablePanelStyleSet()) {
            String themeUri = getConnection().getThemeUri();
            commonDragController.setMoveablePanleStyleName(themeUri.substring(themeUri.lastIndexOf("/") + 1));
        }
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
                cc.getLayoutManager().addElementResizeListener(portletWidget.getAssociatedSlot().getElement(),
                        portletResizeListener);
                commonDragController.makeDraggable(portletWidget, portletWidget.getHeader().getDraggableArea());
                getView().addPortlet(pc.getWidget());

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
        return new PortalDropController(this);
    }

    protected PortalView initView() {
        return new PortalViewImpl(this);
    }

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
            Widget slot = PortalLayoutUtil.getPortletConnectorForContent(incomingPortletCandidate).getWidget()
                    .getAssociatedSlot();
            rpc.updatePortletPosition(incomingPortletCandidate, view.getWidgetIndex(slot));
            incomingPortletCandidate = null;
        }
    }

    public void updatePortletPositionOnServer(ComponentConnector cc) {
        Widget slot = PortalLayoutUtil.getPortletConnectorForContent(cc).getWidget().getAssociatedSlot();
        int positionInView = view.getWidgetIndex(slot);
        int positionInState = getState().portletConnectors.indexOf(cc);
        if (positionInState != positionInView) {
            rpc.updatePortletPosition(cc, positionInView);
        }
    }

    public List<ComponentConnector> getCurrentChildren() {
        List<ComponentConnector> result = new ArrayList<ComponentConnector>(getChildComponents()) {
            @Override
            public boolean add(ComponentConnector cc) {
                if (cc != null) {
                    return super.add(cc);
                }
                return false;
            };
        };
        result.add(incomingPortletCandidate);
        result.remove(outcomingPortletCandidate);
        return result;
    }

    public void removePortlet(ServerConnector connector) {
        rpc.removePortlet(connector);
    }
    
    public PortalHeightRedistributionStrategy getHeightRedistributionStrategy() {
        return heightRedistributionStrategy;
    }
}
