package org.vaadin.addon.portallayout.client.ui.portlet;

public enum AnimationType {
    AT_CLOSE("CLOSE"),
    AT_COLLAPSE("COLLAPSE"),
    AT_ATTACH("ATTACH");
    
    private String name;
    
    private AnimationType(final String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
