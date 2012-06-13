package org.vaadin.addon.portallayout.client.ui;

import org.vaadin.addon.portallayout.client.dnd.DragContext;
import org.vaadin.addon.portallayout.client.dnd.VetoDragException;
import org.vaadin.addon.portallayout.client.dnd.drop.AbstractPositioningDropController;
import org.vaadin.addon.portallayout.client.dnd.util.CoordinateLocation;
import org.vaadin.addon.portallayout.client.dnd.util.DOMUtil;
import org.vaadin.addon.portallayout.client.dnd.util.LocationWidgetComparator;
import org.vaadin.addon.portallayout.client.ui.portal.VPortal;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalDropPositioner;
import org.vaadin.addon.portallayout.client.ui.portlet.VPortlet;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Drop process controller for the portlets.
 * 
 * @author p4elkin
 */
public class PortalDropController extends AbstractPositioningDropController {

    private PortalDropPositioner dummy;

    private VPortal portal;

    private int targetDropIndex = -1;

    public PortalDropController(final VPortal portal) {
        super(portal.asPanel());
        this.portal = portal;
    }

    public PortalDropPositioner getDummy() {
        return dummy;
    }
    
    protected LocationWidgetComparator getLocationWidgetComparator() {
        return LocationWidgetComparator.BOTTOM_RIGHT_COMPARATOR;
    }

    private void updatePortletLocationOnDrop(final VPortlet portlet) {
        final VPortal currentParent = portlet.getPortal();

        if (portal.getPortletIndex(portlet) != targetDropIndex) {
            portal.updatePortletPosition(portlet, targetDropIndex);
        }
        
        if (!currentParent.equals(portal)) {
            currentParent.onPortletMovedOut(portlet);
        }
    }

    private int updateDropPosition(final DragContext context) {
        final CoordinateLocation curLocation = new CoordinateLocation(context.mouseX, context.mouseY);
        int targetDropIndex = DOMUtil.findIntersect(portal.asPanel(), curLocation, getLocationWidgetComparator());
        return targetDropIndex;
    }

    private int getDummyIndex() {
        return (dummy == null) ? -1 : portal.getPortletIndex(dummy);
    }

    protected PortalDropPositioner newPositioner(DragContext context) {
        final VPortlet portlet = (VPortlet) context.selectedWidgets.get(0);
        if (portlet != null)
            return new PortalDropPositioner(portlet);
        return null;
    }

    @Override
    public void onDrop(DragContext context) {
        super.onDrop(context);
        assert targetDropIndex != -1 : "Should not happen after onPreviewDrop did not veto";
        final Widget widget = context.selectedWidgets.get(0);
        removeDummy();
        updatePortletLocationOnDrop((VPortlet) widget);
    }

    @Override
    public void onLeave(DragContext context) {
        removeDummy();
    }

    private void removeDummy() {
        if (dummy != null) {
            ((Panel)dummy.getParent()).remove(dummy);
            dummy = null;
        }
    }

    @Override
    public void onMove(final DragContext context) {
        super.onMove(context);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                if (dummy != null) {
                    int targetIndex = updateDropPosition(context);
                    int dummyIndex = getDummyIndex();
                    if (dummyIndex != targetIndex && (dummyIndex != targetIndex - 1 || targetIndex == 0)) {
                        if (dummyIndex == 0 && portal.getPortletCount() == 1) {
                            // Do nothing...
                        } else if (targetIndex == -1) {
                            Panel parent = (Panel)dummy.getParent();
                            parent.remove(dummy);
                        } else {
                            portal.updateDummyPosition(dummy, targetIndex);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onEnter(final DragContext context) {
        dummy = newPositioner(context);
        //int dummyIndex = updateDropPosition(context);
        //portal.onPortletEntered(dummy, dummyIndex);
    }

    @Override
    public void onPreviewDrop(DragContext context) throws VetoDragException {
        super.onPreviewDrop(context);
        targetDropIndex = getDummyIndex();
        if (targetDropIndex == -1)
            throw new VetoDragException();
    }
}
