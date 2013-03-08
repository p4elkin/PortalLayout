package org.vaadin.addon.portallayout.gwt.client.portlet.header;

import org.vaadin.addon.portallayout.gwt.client.portlet.PortletWidget;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEvent;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEvent.HasPortletCloseEventHandlers;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent.Handler;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent.HasPortletCollapseEventHandlers;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.Util;

public class PortletHeader extends ComplexPanel implements HasPortletCollapseEventHandlers, HasPortletCloseEventHandlers, HasMouseDownHandlers {    
    
    public static final String CLASSNAME = "-header";

    private static final String BUTTON_CLOSE = "close";

    private static final String BUTTON_COLLAPSE = "collapse";

    private static final String BUTTON_EXPAND = "expand";
    
    private static final String BUTTON = "-button";

    private static final String BUTTONBAR = "-buttonbar";

    private static final String TOOLBAR = "-toolbar";
    
    private static final String WIDGET_SLOT = "-widget-slot";

    private final Element container = DOM.createDiv();
    
    private final Element controlContainer = DOM.createSpan();
    
    private final Element captionContainer = DOM.createSpan();
    
    private final Element buttonContainer = DOM.createDiv();
    
    private final Element uidlContainer = DOM.createDiv();
    
    private final Button closeButton = new Button();

    private final Button collapseButton = new Button();
    
    private Widget toolbarWidget;
    
    private final Image icon = new Image();
    
    private final ClickHandler closeButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            fireEvent(new PortletCloseEvent(getParent()));
        }
    };

    
    private final ClickHandler collapseButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            fireEvent(new PortletCollapseEvent(getParent()));
        }
    };

    public PortletHeader() {
        super();
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                final NativeEvent nativeEvent = event.getNativeEvent();
                if (event.getTypeInt() == Event.ONMOUSEDOWN) {
                    getParent().blur();
                    final Element target = nativeEvent.getEventTarget().cast();
                    final Widget w = Util.findWidget(target, null);
                    if (!(w instanceof HasWidgets)) {
                        nativeEvent.stopPropagation();
                    }   
                }
            }
        });
        
        setElement(container);
        container.addClassName(getClassName());
        container.addClassName("v-caption");
        
        captionContainer.addClassName("v-captiontext");
        icon.addStyleName("v-icon");
        add(icon, container);
        container.appendChild(captionContainer);
        
        closeButton.addClickHandler(closeButtonClickHandler);
        collapseButton.addClickHandler(collapseButtonClickHandler);
        add(collapseButton, buttonContainer);
        add(closeButton, buttonContainer);
        
        controlContainer.getStyle().setVerticalAlign(VerticalAlign.TOP);
        closeButton.setStyleName(getClassName() + BUTTON);
        closeButton.addStyleDependentName(BUTTON_CLOSE);
        
        collapseButton.setStyleName(getClassName() + BUTTON);
        collapseButton.addStyleDependentName(BUTTON_COLLAPSE);
        
        controlContainer.setClassName(getClassName() + TOOLBAR);
        buttonContainer.setClassName(getClassName() + BUTTONBAR);
        uidlContainer.setClassName(getClassName() + WIDGET_SLOT);
        
        container.appendChild(buttonContainer);
        container.appendChild(uidlContainer);
    }
    
    public void setIcon(String iconUri) {
        icon.setVisible(iconUri != null);
        if (iconUri != null) {
            iconUri = iconUri == null ? "" : iconUri;
            icon.setUrl(iconUri);
            icon.getElement().getStyle().setDisplay(iconUri == "" ? Display.NONE : Display.INLINE);   
        }
    }
    
    public Widget getDraggableArea() {
        return this;
    }

    public static String getClassName() {
        return "v-portlet" + CLASSNAME;
    }

    public void setCaptionText(String text) {
        captionContainer.setInnerHTML(text);
    }
    
    public void setClosable(boolean closable) {
        closeButton.setVisible(closable);
    }

    public void setCollapsible(boolean isCollapsible) {
        collapseButton.setVisible(isCollapsible);
    }
    
    public Widget getToolbarWidget() {
        return toolbarWidget;
    }

    public void toggleCollapseStyles(boolean isCollapsed) {
        collapseButton.removeStyleDependentName(isCollapsed ? BUTTON_COLLAPSE : BUTTON_EXPAND);
        collapseButton.addStyleDependentName(isCollapsed ?  BUTTON_EXPAND : BUTTON_COLLAPSE);
    }
    
    @Override
    public PortletWidget getParent() {
        return (PortletWidget)super.getParent();
    }
    
    @Override
    public HandlerRegistration addPortletCollapseEventHandler(Handler handler) {
        return addHandler(handler, PortletCollapseEvent.TYPE);
    }

    @Override
    public HandlerRegistration addPortletCloseEventHandler(PortletCloseEvent.Handler handler) {
        return addHandler(handler, PortletCloseEvent.TYPE);
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

    public void setToolbar(Widget toolbar) {
        this.toolbarWidget = toolbar;
        add(toolbar, uidlContainer);
    }
}
