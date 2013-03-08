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

import org.vaadin.addon.portallayout.gwt.client.dnd.PortalWithExDropController;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalView;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalViewImpl;
import org.vaadin.addon.portallayout.gwt.client.portal.connection.rpc.PortalServerRpc;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletExConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletWidget;
import org.vaadin.addon.portallayout.gwt.shared.portal.PortalWithExState;
import org.vaadin.addon.portallayout.portal.PortalWithExtension;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.PostLayoutListener;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ComponentConstants;
import com.vaadin.shared.communication.URLReference;
import com.vaadin.shared.ui.ComponentStateUtil;
import com.vaadin.shared.ui.Connect;

/**
 * PortalWithExtensionConnector.
 */
@Connect(PortalWithExtension.class)
public class PortalWithExtensionConnector extends AbstractLayoutConnector implements PortalView.Presenter, PostLayoutListener {

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
    
    private final static PortalPickupDragController commonDragController = new PortalPickupDragController(RootPanel.get(), false);
    
    private ComponentConnector incomingPortletCandidate;

    private ComponentConnector outcomingPortletCandidate;
    
    private PortalView view;
    
    private final ElementResizeListener portletResizeListener = new ElementResizeListener() {
        @Override
        public void onElementResize(ElementResizeEvent event) {
            recalculateHeights();
        }
    };
    
    @Override
    protected void init() {
        super.init();
        getLayoutManager().setNeedsMeasure(this);
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
            final PortletExConnector pc = (PortletExConnector)getState().contentToPortlet.get(connector);
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
            view.removePortlet(((PortletExConnector)getState().contentToPortlet.get(cc)).getWidget());
        }

        final Iterator<ComponentConnector> it = children.iterator();
        while (it.hasNext()) {
            final ComponentConnector cc = it.next();
            if (getState().contentToPortlet.get(cc) != null) {
                final PortletExConnector pc = (PortletExConnector)getState().contentToPortlet.get(cc);
                final PortletWidget portletWidget = pc.getWidget();
                cc.getLayoutManager().addElementResizeListener(cc.getWidget().getElement(), portletResizeListener);
                commonDragController.makeDraggable(portletWidget, portletWidget.getHeader().getDraggableArea());
                getView().addPortlet(pc.getWidget());

            }
        }
    }
    
    public void setIncomingPortletCandidate(PortletWidget portletWidget) {
        assert portletWidget != null;
        ComponentConnector pc = Util.findConnectorFor(portletWidget.getContentWidget());
        if (!getChildComponents().contains(pc) || outcomingPortletCandidate == pc) {
            if (this.outcomingPortletCandidate == pc) {
                this.outcomingPortletCandidate = null;
            } else {
                this.incomingPortletCandidate = pc;
            }
            recalculateHeights();
        }
    }

    public void setOutcomingPortletCandidate(PortletWidget portletWidget) {
        assert portletWidget != null;
        ComponentConnector pc = Util.findConnectorFor(portletWidget.getContentWidget());
        if (getChildComponents().contains(pc) || incomingPortletCandidate == pc) {
            if (this.incomingPortletCandidate == pc) {
                this.incomingPortletCandidate = null;
            } else {
                this.outcomingPortletCandidate = pc;
            }
            recalculateHeights();
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
        this.view = new PortalViewImpl(this);
        commonDragController.registerDropController(new PortalWithExDropController(this));
        return view.asWidget();
    }

    @Override
    public PortalWithExState getState() {
        return (PortalWithExState) super.getState();
    }

    @Override
    public void postLayout() {
        recalculateHeights();
    }

    @Override
    public void recalculateHeights() {
        Iterator<ComponentConnector> it = getCurrentChildren().iterator();
        List<ComponentConnector> relativeHeightPortlets = new ArrayList<ComponentConnector>();
        double totalPercentage = 0;
        int totalFixedHeightConsumption = 0;
        while (it.hasNext()) {
            ComponentConnector cc = it.next();
            if (ComponentStateUtil.isRelativeHeight(cc.getState())) {
                totalPercentage += Util.parseRelativeSize(cc.getState().height);
                relativeHeightPortlets.add(cc);
            } else {
                totalFixedHeightConsumption += cc.getLayoutManager().getOuterHeight(cc.getWidget().getElement());
            }
        }
        if (totalPercentage > 0) {
            totalPercentage = Math.max(totalPercentage, 100);
            int totalPortalHeight = getLayoutManager().getInnerHeight(getWidget().getElement());
            int reservedForRelativeSize = totalPortalHeight - totalFixedHeightConsumption;
            double ratio = reservedForRelativeSize / (double) totalPortalHeight * 100d;
            for (ComponentConnector cc : relativeHeightPortlets) {
                PortletExConnector pc = (PortletExConnector)getState().contentToPortlet.get(cc);
                if (!pc.isCollased()) {
                    float height = Util.parseRelativeSize(cc.getState().height);
                    double slotHeight = (height / totalPercentage * ratio);
                    pc.setSlotHeight(slotHeight + "%");
                }
            }
        }
    }
    
    protected List<ComponentConnector> getCurrentChildren() {
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
}
