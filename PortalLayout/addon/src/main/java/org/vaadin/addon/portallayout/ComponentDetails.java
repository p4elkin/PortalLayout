package org.vaadin.addon.portallayout;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.vaadin.ui.Component;

/**
 * Helper class that holds Portlet information about the object.
 * 
 * @author p4elkin
 */
@SuppressWarnings("serial")
final class ComponentDetails implements Serializable {

    private boolean isLocked = false;

    private boolean isCollapsed = false;

    private boolean isClosable = true;

    private boolean isCollapsible = true;

    private Map<String, ToolbarAction> actions;

    private List<String> styles = new LinkedList<String>();

    private Component headerComponent;

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
    }

    public boolean isClosable() {
        return isClosable;
    }

    public void setClosable(boolean isClosable) {
        this.isClosable = isClosable;
    }

    public boolean isCollapsible() {
        return isCollapsible;
    }

    public void setCollapsible(boolean isCollapsible) {
        this.isCollapsible = isCollapsible;
    }

    public String addAction(final ToolbarAction action) {
        if (actions == null)
            actions = new LinkedHashMap<String, ToolbarAction>();
        final String randomId = "TB_ACTION" + Math.random();
        actions.put(randomId, action);
        return randomId;
    }

    public Map<String, ToolbarAction> getActions() {
        return actions;
    }

    public ToolbarAction getActionById(final String id) {
        return actions.get(id);
    }

    public void removeAction(final String actionId) {
        actions.remove(actionId);
    }

    public void addStyle(final String style) {
        styles.add(style);
    }

    public void setStyles(final List<String> styles) {
        this.styles.clear();
        this.styles.addAll(styles);
    }
    
    public void removeStyle(final String style) {
        styles.remove(style);
    }
    
    public List<String> getStyles() {
        return Collections.unmodifiableList(styles);
    }

    public Component getHeaderComponent() {
        return headerComponent;
    }

    public void setHeaderComponent(Component headerComponent) {
        this.headerComponent = headerComponent;
    }
}
