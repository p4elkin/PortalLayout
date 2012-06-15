package org.vaadin.addon.portallayout.client.ui.portlet;

import org.vaadin.addon.portallayout.client.dnd.util.DOMUtil;

import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Dummy wire frame widget that is displayed when the portlet is dragged within
 * the portal.
 * 
 * @author p4elkin
 */

public class PortalDropPositioner extends SimplePanel implements PortalObject {
    
    private static final String CLASS_NAME = "v-portallayout-positioner";

    private final SimplePanel internalContent = new SimplePanel();

    private final VPortlet portlet;
    
    public PortalDropPositioner(final VPortlet portlet) {
        super();
        setStyleName(CLASS_NAME);
        this.portlet = portlet;
        int width = portlet.getContentSizeInfo().getWidth();
        int height = portlet.getRequiredHeight();
        setWidgetSizes(width, height);
        setWidget(internalContent);
    }
    
    @Override
    public boolean isHeightRelative() {
        return portlet.isHeightRelative();
    }

    @Override
    public float getRelativeHeightValue() {
        return portlet.getRelativeHeightValue();
    }

    @Override
    public int getRequiredHeight() {
        return portlet.getRequiredHeight();
    }

    @Override
    public int getContentHeight() {
        return portlet.getContentHeight();
    }
    
    
    public void setWidgetSizes(int width, int height) {
        int innerWidth = width - DOMUtil.getHorizontalBorders(this);
        int innerHeight = height - DOMUtil.getVerticalBorders(this) + 
                (portlet.isHeightRelative() ? 0 : portlet.getDraggableArea().getOffsetHeight());
        internalContent.setPixelSize(innerWidth, innerHeight);
    }

    @Override
    public void setSpacingValue(int spacing) {
        getElement().getStyle().setPropertyPx("marginTop", spacing);
    }

    @Override
    public VPortlet getPortletRef() {
        return portlet;
    }
}
