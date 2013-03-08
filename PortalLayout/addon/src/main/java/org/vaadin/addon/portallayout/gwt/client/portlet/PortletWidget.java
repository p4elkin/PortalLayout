package org.vaadin.addon.portallayout.gwt.client.portlet;

import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent.Handler;
import org.vaadin.addon.portallayout.gwt.client.portlet.header.PortletHeader;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class PortletWidget extends FlowPanel {
    
    private final PortletSlot slot = new PortletSlot(this);
    
    private final PortletHeader header = new PortletHeader();

    private final Element contentEl = DOM.createDiv();

    private Widget contentWidget;
    
    public PortletWidget() {
        super();
        setStyleName("v-portlet");
        header.setStyleName("v-portlet-header");
        getElement().getStyle().setPosition(Position.RELATIVE);
        super.add(header);

        contentEl.setClassName("v-portlet-content");
        getElement().appendChild(contentEl);
        getElement().getStyle().setColor("white");
        
        header.addPortletCollapseEventHandler(new Handler() {
            @Override
            public void onPortletCollapse(PortletCollapseEvent e) {
                
            }
        });
    }
    
    public PortletSlot getSlot() {
        return slot;
    }
    
    public Widget getContentWidget() {
        return contentWidget;
    }
    
    public void setContentWidget(Widget content) {
        this.contentWidget = content;
        add(content);
    }

    public PortletHeader getHeader() {
        return header;
    }
    
    @Override
    public void add(Widget child) {
        super.add(child, contentEl);
    }

    public void blur() {
        if (contentWidget != null) {
            contentWidget.getElement().blur();
        }
    }

    public void close() {
        getSlot().removeFromParent();
    }

    public boolean isCollapsed() {
        return getSlot().getStyleName().contains("collapsed");
    }

    public Element getContentElement() {
        return contentEl;
    }

    public void setHeaderToolbar(Widget toolbar) {
        header.setToolbar(toolbar);
    }
}
