package org.vaadin.addon.portallayout.gwt.client.portlet;

import org.vaadin.addon.portallayout.gwt.client.portlet.header.PortletHeader;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class PortletChrome extends FlowPanel {

    private final PortletSlot associatedSlot = new PortletSlot(this);

    private final PortletHeader header = new PortletHeader();

    private final Element contentEl = DOM.createDiv();

    private Widget contentWidget;

    public PortletChrome() {
        super();
        setStyleName("v-portlet");
        header.setStyleName("v-portlet-header");
        getElement().getStyle().setPosition(Position.RELATIVE);
        super.add(header);
        contentEl.setClassName("v-portlet-content");

        getElement().appendChild(contentEl);
        updateContentStructure(false);
    }

    public PortletSlot getAssociatedSlot() {
        return associatedSlot;
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
        getAssociatedSlot().removeFromParent();
    }

    public boolean isCollapsed() {
        return getStyleName().contains("collapsed");
    }

    public void setHeaderToolbar(Widget toolbar) {
        header.setToolbar(toolbar);
    }

    public void updateContentStructure(boolean isHeightUndefined) {
        if (isHeightUndefined) {
            contentEl.getStyle().setTop(0, Style.Unit.PX);
        } else {
            contentEl.getStyle().clearTop();
        }
        contentEl.getStyle().setPosition(isHeightUndefined ? Position.RELATIVE : Position.ABSOLUTE);
        setStyleName("v-portlet-undefined-height", isHeightUndefined);
        setHeight(isHeightUndefined ? "" : "100%");
    }
}
