package org.vaadin.addon.portallayout.gwt.client.portal.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vaadin.addon.portallayout.gwt.client.dnd.OrderedPortalDropController;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalView;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalViewImpl;
import org.vaadin.addon.portallayout.gwt.client.portal.connection.rpc.PortalServerRpc;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletSlot;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletWidget;
import org.vaadin.addon.portallayout.gwt.shared.portal.PortalState;
import org.vaadin.addon.portallayout.portal.Portal;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.client.ui.PostLayoutListener;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

@Connect(Portal.class)
public class PortalLayoutConnector extends AbstractComponentContainerConnector implements PortalView.Presenter,
        PostLayoutListener {

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

    private final static PortalPickupDragController commonDragController = new PortalPickupDragController(
            RootPanel.get(), false);

    private PortalView view;

    private PortletConnector incomingPortletCandidate = null;

    private PortletConnector outcomingPortletCandidate = null;

    private final PortalServerRpc rpc = RpcProxy.create(PortalServerRpc.class, this);

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
        if (connector instanceof PortletConnector) {
            final PortletConnector portletConnector = (PortletConnector) connector;
            portletConnector.updateOwnCaption();
        }
    }

    private final ElementResizeListener portletResizeListener = new ElementResizeListener() {
        @Override
        public void onElementResize(ElementResizeEvent event) {
            recalculateHeights();
        }
    };

    StateChangeHandler portletStateChangeHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent event) {
            recalculateHeights();
        }
    };

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        final List<ComponentConnector> children = getChildComponents();
        final List<ComponentConnector> oldChildren = event.getOldChildren();

        oldChildren.removeAll(children);
        for (final ComponentConnector cc : oldChildren) {
            view.removePortlet(((PortletConnector) cc).getWidget());
        }

        final Iterator<ComponentConnector> it = children.iterator();
        while (it.hasNext()) {
            final ComponentConnector cc = it.next();
            if (cc instanceof PortletConnector) {
                final PortletConnector pc = (PortletConnector) cc;
                final PortletWidget portletWidget = pc.getWidget();
                final PortletSlot slot = portletWidget.getSlot();
                pc.addStateChangeHandler("slotSize", portletStateChangeHandler);
                commonDragController.makeDraggable(portletWidget, portletWidget.getHeader().getDraggableArea());
                getView().addPortlet(pc.getWidget());
                cc.getLayoutManager().addElementResizeListener(slot.getElement(), portletResizeListener);
            }
        }
    }

    @Override
    public void recalculateHeights() {
        Iterator<ComponentConnector> it = getCurrentChildren().iterator();
        List<PortletConnector> relativeHeightPortlets = new ArrayList<PortletConnector>();
        double totalPercentage = 0;
        int totalFixedHeightConsumption = 0;
        while (it.hasNext()) {
            PortletConnector cc = (PortletConnector) it.next();
            if (cc.isSlotRelativeHeight()) {
                totalPercentage += cc.getHeightValue();
                relativeHeightPortlets.add(cc);
            } else {
                totalFixedHeightConsumption += cc.getHeightValue();
            }
        }
        if (totalPercentage > 0) {
            totalPercentage = Math.max(totalPercentage, 100);
            int totalPortalHeight = getLayoutManager().getInnerHeight(getWidget().getElement());
            int reservedForRelativeSize = totalPortalHeight - totalFixedHeightConsumption;
            double ratio = reservedForRelativeSize / (double) totalPortalHeight * 100d;
            for (PortletConnector pc : relativeHeightPortlets) {
                if (!pc.isCollased()) {
                    double slotHeight = (pc.getHeightValue() / totalPercentage * ratio);
                    pc.setSlotHeight(slotHeight + "%");
                }
            }
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
        commonDragController.registerDropController(new OrderedPortalDropController(this));
        return view.asWidget();
    }

    @Override
    public PortalState getState() {
        return (PortalState) super.getState();
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

    public void setIncomingPortletCandidate(PortletWidget portletWidget) {
        assert portletWidget != null;
        ComponentConnector pc = Util.findConnectorFor(portletWidget);
        if (!getChildComponents().contains(pc) || outcomingPortletCandidate == pc) {
            if (this.outcomingPortletCandidate == pc) {
                this.outcomingPortletCandidate = null;
            } else {
                this.incomingPortletCandidate = (PortletConnector) pc;
            }
            recalculateHeights();
        }
    }

    public void setOutcomingPortletCandidate(PortletWidget portletWidget) {
        assert portletWidget != null;
        ComponentConnector pc = Util.findConnectorFor(portletWidget);
        if (getChildComponents().contains(pc) || incomingPortletCandidate == pc) {
            if (this.incomingPortletCandidate == pc) {
                this.incomingPortletCandidate = null;
            } else {
                this.outcomingPortletCandidate = (PortletConnector) pc;
            }
            recalculateHeights();
        }
    }

    public void propagateHierarchyChangesToServer() {
        if (outcomingPortletCandidate != null) {
            rpc.removePortlet(outcomingPortletCandidate);
            outcomingPortletCandidate = null;
        }

        if (incomingPortletCandidate != null) {
            rpc.updatePortletPosition(incomingPortletCandidate, view.getWidgetIndex(incomingPortletCandidate.getSlot()));
            incomingPortletCandidate = null;
        }
    }

    public PortletWidget getIncomingPortlet() {
        return incomingPortletCandidate == null ? null : incomingPortletCandidate.getWidget();
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
    }

    public void updatePortletPositionOnServer(PortletWidget portletWidget) {
        ComponentConnector pc = Util.findConnectorFor(portletWidget);
        int positionInView = view.getWidgetIndex(portletWidget.getSlot());
        int positionInState = getState().portletConnectors.indexOf(portletWidget);
        if (positionInState != positionInView) {
            rpc.updatePortletPosition(pc, positionInView);
        }
    }

    public void removePortlet(PortletConnector portletConnector) {
        rpc.removePortlet(portletConnector);
    }

    @Override
    public void postLayout() {
        recalculateHeights();
    }
}
