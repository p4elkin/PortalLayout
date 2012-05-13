package org.vaadin.addon.portallayout.client.ui;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for the objects that might be stored in the portal. Exists mostly
 * for making easier the size calculations of Portlets and
 * PortalDropPositioners.
 * 
 * @author p4elkin
 * 
 */
public interface PortalObject extends IsWidget {

    /**
     * Check if object has relative height.
     * 
     * @return true is height is relative, false - otherwise.
     */
    public boolean isHeightRelative();

    /**
     * Get the percentage of the relative height.
     * 
     * @return The relative height value.
     */
    public float getRelativeHeightValue();

    /**
     * Get pixel amount needed by the component to display its fixed part. If
     * the component has relative height then only size of the header will be
     * considered.
     * 
     * @return Value in pixels.
     */
    public int getRequiredHeight();

    /**
     * 
     */
    public int getContentHeight();
    
    /**
     * Set pixel size of the portal object (Portlet or PortalDropPositioners).
     * 
     * @param width
     *            New width value.
     * @param height
     *            New height value.
     */
    public void setWidgetSizes(int width, int height);

    /**
     * Set the pixel value reserved for the spacing.
     * 
     * @param spacing
     *            New spacing value.
     */
    public void setSpacingValue(int spacing);

    /**
     * Get reference to the Portlet.
     * 
     * @return Reference to the Portlet.
     */
    public VPortlet getPortletRef();

}
