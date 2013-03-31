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
package org.vaadin.addon.portallayout.gwt.client.portlet;

import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import org.vaadin.addon.portallayout.gwt.shared.portlet.AbsolutePositionPortletState;
import org.vaadin.addon.portallayout.portlet.AbsolutePositionPortlet;

/**
 * Client-side connector that corresponds to {@link AbsolutePositionPortlet}.
 */
@Connect(AbsolutePositionPortlet.class)
public class AbsolutePortletConnector extends AbstractExtensionConnector {

    private Element footer = DOM.createDiv();

    private PortletConnector parentPortlet;

    private PortletChrome portletChrome;

    private final HTML resizeDrag = new HTML() {

        {
            addStyleName("v-portlet-resize-drag");
            sinkEvents(Event.MOUSEEVENTS);
        }

        @Override
        public void onBrowserEvent(Event event) {
            int type = event.getTypeInt();
            if (type == Event.ONMOUSEDOWN) {
                handleMouseDown(event);
            }
            if (type == Event.ONMOUSEMOVE) {
                handleMouseMove(event);
            }
            if (type == Event.ONMOUSEUP) {
                handleMouseUp(event);
            }
            if (type == Event.ONCLICK) {
                Window.alert("CLICK");
            }
            super.onBrowserEvent(event);
        }
    };
    private int initialWidth;

    private int initialHeight;

    @Override
    protected void init() {
        super.init();
        footer.setClassName("v-portlet-footer");
    }

    private void handleMouseUp(Event event) {
        initialX = -1;
        initialY = -1;
        isResizing = false;
        DOM.releaseCapture(resizeDrag.getElement());
        cancelDocumentSelection();
    }

    private void handleMouseMove(Event event) {
        if (isResizing) {
            cancelDocumentSelection();
            int currentX = Util.getTouchOrMouseClientX(event);
            int currentY = Util.getTouchOrMouseClientY(event);
            int deltaX = currentX - initialX;
            int deltaY = currentY - initialY;
            LayoutManager lm = parentPortlet.getLayoutManager();
            String width = (initialWidth + deltaX) + "px";
            String height = (initialHeight + deltaY) + "px";

            portletChrome.getAssociatedSlot().setWidth(width);
            portletChrome.getAssociatedSlot().setHeight(height);

            lm.setNeedsMeasure((ComponentConnector)getParent().getParent());
            lm.layoutLater();
        }
    }

    private void handleMouseDown(Event event) {
        LayoutManager lm = parentPortlet.getLayoutManager();
        DOM.setCapture(resizeDrag.getElement());
        initialX = Util.getTouchOrMouseClientX(event);
        initialY = Util.getTouchOrMouseClientY(event);
        initialWidth = lm.getOuterWidth(portletChrome.getElement());
        initialHeight = lm.getOuterHeight(portletChrome.getElement());
        cancelDocumentSelection();
        isResizing = true;
    }

    private void cancelDocumentSelection() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                DOMUtil.cancelAllDocumentSelections();
            }
        });
    }

    private int initialX = -1;
    
    private int initialY = -1;
    
    private boolean isResizing = false;
    
    @Override
    protected void extend(ServerConnector target) {
        assert target instanceof PortletConnector;
        this.parentPortlet = getParent();
        this.portletChrome = parentPortlet.getWidget();
        PortletConnector portlet = (PortletConnector)target;
        PortletChrome widget = portlet.getWidget();
        widget.addStyleName("v-portlet-resizable");
        widget.insert(resizeDrag, widget.getWidgetCount() - 1);
        widget.getElement().appendChild(footer);
    }
    
    @Override
    public PortletConnector getParent() {
        return (PortletConnector)super.getParent();
    }
    
    @Override
    public AbsolutePositionPortletState getState() {
        return (AbsolutePositionPortletState)super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent event) {
        super.onStateChanged(event);
        Style style = getParent().getWidget().getElement().getStyle();
        style.setTop(getState().y, Style.Unit.PX);
        style.setLeft(getState().x, Style.Unit.PX);
    }

    @Override
    public void onUnregister() {
        portletChrome.getElement().removeChild(footer);
        portletChrome.removeStyleName("v-portlet-resizable");
        resizeDrag.removeFromParent();
        super.onUnregister();
    }
    
}
