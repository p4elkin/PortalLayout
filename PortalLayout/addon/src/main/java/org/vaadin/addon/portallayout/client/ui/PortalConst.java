package org.vaadin.addon.portallayout.client.ui;

public class PortalConst {
    /**
     * Parameter sent to server in case a potlet is added from other portal.
     */
    public static final String PORTLET_POSITION_UPDATED = "COMPONENT_ADDED";

    /**
     * Parameter sent to server in case a potlet is moved to other portal.
     */
    public static final String PORTLET_REMOVED = "COMPONENT_REMOVED";

    /**
     * PID sent to server.
     */
    public static final String PAINTABLE_MAP_PARAM = "PAINTABLE";

    /**
     * Client-server parameter indicating that the portal is collapsed.
     */
    public static final String PORTLET_COLLAPSED = "PORTLET_COLLAPSED";

    /**
     * Client-server parameter indicating that the portal was
     * collapsed/expanded.
     */
    public static final String PORTLET_COLLAPSE_STATE_CHANGED = "PORTLET_COLLAPSE_STATE_CHANGE";

    /**
     * Parameter received from server, true if portlet is closable.
     */
    public static final String PORTLET_CLOSABLE = "PORTLET_CLOSABLE";

    /**
     * Parameter received from server, true if portlet is callapsible.
     */
    public static final String PORTLET_COLLAPSIBLE = "PORTLET_COLLAPSIBLE";

    /**
     * Parameter received from server, true if portlet is locked (cannot be
     * dragged).
     */
    public static final String PORTLET_LOCKED = "PORTLET_LOCKED";

    /**
     * Parameter received from server, portlet position.
     */
    public static final String PORTLET_POSITION = "PORTLET_POSITION";

    /**
     * 
     */
    public static final String PORTLET_ACTION_IDS = "PORTLET_ACTION_IDS";
    
    /**
     * 
     */
    public static final String PORTLET_ACTION_ICONS = "PORTLET_ACTION_ICONS";
    
    /**
     *  
     */
    public static final String PORTLET_ACTION_TRIGGERED = "PORTLET_ACTION_TRIGGERED";
    
    /**
     * 
     */
    public static final String PORTLET_ENTERED = "PORTLET_ENTERED";
    
    /**
     * 
     */
    public static final String PORTLET_LEFT = "PORTLET_LEFT";
    
    /**
     * 
     */
    public static final String PORTLET_ACTION_ID = "PORTLET_ACTION_ID";
    
    /**
     * Client-server parameter that sets this portals' ability to share
     * portlets.
     */
    public static final String PORTAL_COMMUNICATIVE = "PORTAL_COMMUNCATIVE";

    /**
     * 
     */
    public static final String PORTLET_CLOSED = "PORTLET_CLOSED";
    
    /**
     * Basic style name.
     */
    public static final String CLASSNAME = "v-portallayout";

    /**
     * Spacing style prefix.
     */
    public static final String STYLENAME_SPACING = CLASSNAME + "-spacing";
    
    public static final int DEFAULT_SPEED = 700;
   
}
