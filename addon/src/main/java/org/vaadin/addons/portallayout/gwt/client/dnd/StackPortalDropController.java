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
package org.vaadin.addons.portallayout.gwt.client.dnd;

import org.vaadin.addons.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addons.portallayout.gwt.client.portlet.PortletChrome;
import org.vaadin.addons.portallayout.gwt.client.portlet.PortletSlot;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbstractPositioningDropController;
import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.LocationWidgetComparator;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.Util;

/**
 * StackPortalDropController.
 */
public class StackPortalDropController extends AbstractPositioningDropController {

    private PortletSlot positionerSlot;

    private final InsertPanel panel;

    private final PortalLayoutConnector portalConnector;

    public StackPortalDropController(PortalLayoutConnector portal) {
        super(portal.getView().asWidget());
        this.panel = portal.getView();
        this.portalConnector = portal;
    }

    protected LocationWidgetComparator getLocationWidgetComparator() {
        return LocationWidgetComparator.BOTTOM_HALF_COMPARATOR;
    }

    @Override
    public void onEnter(DragContext context) {
        super.onEnter(context);
        PortletChrome portletWidget = (PortletChrome) context.selectedWidgets.get(0);
        PortletSlot slot = portletWidget.getAssociatedSlot();
        if (panel != slot.getParent()) {
            PortalLayoutConnector originalConnector = ((PortalLayoutConnector) Util.findConnectorFor(slot.getParent()));
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
        PortletChrome portletWidget = (PortletChrome) context.selectedWidgets.get(0);
        if (positionerSlot != null) {
            positionerSlot.setWidget(portletWidget);
        }
        Widget contentWidget = portletWidget.getContentWidget();
        ComponentConnector contentConnector = Util.findConnectorFor(contentWidget);
        PortalLayoutConnector originalConnector = (PortalLayoutConnector) contentConnector.getParent();
        if (originalConnector != portalConnector) {
            portalConnector.propagateHierarchyChangesToServer();
            originalConnector.propagateHierarchyChangesToServer();
        } else {
            portalConnector.updatePortletPositionOnServer(contentConnector);
        }
    }

    @Override
    public void onMove(DragContext context) {
        super.onMove(context);
        int targetIndex = DOMUtil.findIntersect(panel,
                new CoordinateLocation(context.mouseX, context.mouseY),
                getLocationWidgetComparator());

        // check that positioner not already in the correct location
        int positionerIndex = panel.getWidgetIndex(positionerSlot);
        if (positionerIndex != targetIndex && (positionerIndex != targetIndex - 1 || targetIndex == 0)) {
            if (positionerIndex == 0 && panel.getWidgetCount() == 1) {
            } else if (targetIndex == -1) {
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
        positionerSlot.addStyleName("v-portallayout-positioner");
    }
}
