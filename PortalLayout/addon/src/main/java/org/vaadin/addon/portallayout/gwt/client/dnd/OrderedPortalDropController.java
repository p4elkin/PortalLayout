package org.vaadin.addon.portallayout.gwt.client.dnd;

import org.vaadin.addon.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletSlot;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletWidget;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbstractPositioningDropController;
import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.allen_sauer.gwt.dnd.client.util.LocationWidgetComparator;
import com.google.gwt.user.client.ui.InsertPanel;
import com.vaadin.client.Util;

public class OrderedPortalDropController extends AbstractPositioningDropController {

    private PortletSlot positionerSlot;

    private final InsertPanel panel;

    private final PortalLayoutConnector portalConnector;

    public OrderedPortalDropController(PortalLayoutConnector portal) {
        super(portal.getView().asWidget());
        this.panel = portal.getView();
        this.portalConnector = portal;
    }

    protected LocationWidgetComparator getLocationWidgetComparator() {
        return LocationWidgetComparator.BOTTOM_HALF_COMPARATOR;
    }

    @Override
    public void onEnter(DragContext context) {
        PortletWidget portletWidget = (PortletWidget) context.selectedWidgets.get(0);
        PortletSlot slot = portletWidget.getSlot();
        if (panel != slot.getParent()) {
            PortalLayoutConnector originalConnector = ((PortalLayoutConnector) Util.findConnectorFor(portletWidget)
                    .getParent());
            originalConnector.setOutcomingPortletCandidate(portletWidget);
            portalConnector.setIncomingPortletCandidate(portletWidget);
        }
        configurePostionerSlot(context, slot);
    }

    @Override
    public void onPreviewDrop(DragContext context) throws VetoDragException {
        super.onPreviewDrop(context);
    }

    @Override
    public void onDrop(DragContext context) {
        super.onDrop(context);
        PortletWidget portletWidget = (PortletWidget) context.selectedWidgets.get(0);
        if (positionerSlot != null) {
            positionerSlot.setWidget(portletWidget);
        }

        PortalLayoutConnector originalConnector = ((PortalLayoutConnector) 
                Util.findConnectorFor(portletWidget).getParent());
        if (originalConnector != portalConnector) {
            portalConnector.propagateHierarchyChangesToServer();
            originalConnector.propagateHierarchyChangesToServer();
        } else {
            portalConnector.updatePortletPositionOnServer(portletWidget);
        }
    }

    @Override
    public void onMove(DragContext context) {
        super.onMove(context);
        super.onMove(context);
        int targetIndex = DOMUtil.findIntersect(panel,
                new CoordinateLocation(context.mouseX, context.mouseY),
                getLocationWidgetComparator());

        // check that positioner not already in the correct location
        int positionerIndex = panel.getWidgetIndex(positionerSlot);

        if (positionerIndex != targetIndex && (positionerIndex != targetIndex - 1 || targetIndex == 0)) {
            if (positionerIndex == 0 && panel.getWidgetCount() == 1) {
                // do nothing, the positioner is the only widget
            } else if (targetIndex == -1) {
                /**
                 * Outside drop target, so normally - remove positioner to
                 * indicate a drop will not happen. In our case the portlet slot
                 * acts as a positioner, so we do not remove it.
                 */
            } else {
                panel.insert(positionerSlot, targetIndex);
            }
        }
    }
    
    protected void configurePostionerSlot(DragContext context, PortletSlot slot) {
        positionerSlot = slot;
        int targetIndex = DOMUtil.findIntersect(panel,
                new CoordinateLocation(context.mouseX, context.mouseY),
                getLocationWidgetComparator());
        panel.insert(positionerSlot, targetIndex);
        positionerSlot.addStyleName(DragClientBundle.INSTANCE.css().positioner());
    }

}
