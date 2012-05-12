package org.vaadin.addon.portallayout;

import org.vaadin.addon.portallayout.event.Context;

import com.vaadin.terminal.ThemeResource;

/**
 * Action class that represents a toolbutton on the client side that
 * performs an action.
 * 
 * @author p4elkin
 */
public abstract class ToolbarAction {

    private final ThemeResource icon;
    
    public ToolbarAction(final ThemeResource icon) {
        this.icon = icon;
    }
    
    public ThemeResource getIcon() {
        return icon;
    }
    
    public abstract void execute(final Context context);
}

