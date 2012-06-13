package org.vaadin.addon.portallayout.client.ui.portlet;

import java.util.Set;

import org.vaadin.addon.portallayout.client.dnd.util.DOMUtil;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VCaption;

/**
 * Portlet header. Contains the controls for the basic operations with portlet
 * like closing, collapsing and pinning.
 * 
 * @author p4elkin
 */
public class PortletHeader extends ComplexPanel implements Container {    
    
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
    
    private final Element buttonContainer = DOM.createDiv();
    
    private final Element uidlContainer = DOM.createDiv();
    
    private final VPortlet parentPortlet;

    private Widget child;
    
    private Button closeButton = new Button();

    private Button collapseButton = new Button();

    private VPortletCaption vcaption;
    
    private ApplicationConnection client;
    
    private boolean closable = true;

    private boolean collapsible = true;
    
    private ClickHandler closeButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            parentPortlet.close();
        }
    };

    
    private ClickHandler collapseButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            parentPortlet.toggleCollapseState();
        }
    };

    private MouseDownHandler blockingDownHandler = new MouseDownHandler() {

        @Override
        public void onMouseDown(MouseDownEvent event) {
            parentPortlet.blur();
            final NativeEvent nativeEvent = event.getNativeEvent();
            final Element target = nativeEvent.getEventTarget().cast();
            final Widget w = Util.findWidget(target, null);
            if (!(w instanceof HasWidgets)) {
                event.stopPropagation();
            }
        }
    };

    public PortletHeader(final VPortlet parent) {
        super();
        
        setElement(container);
        vcaption = new VPortletCaption(null, client);
        container.setClassName(getClassName());
        parentPortlet = parent;

        closeButton.addClickHandler(closeButtonClickHandler);
        collapseButton.addClickHandler(collapseButtonClickHandler);
        vcaption.addMouseDownHandler(blockingDownHandler);
        closeButton.addMouseDownHandler(blockingDownHandler);
        collapseButton.addMouseDownHandler(blockingDownHandler);
       
        
        add(vcaption, container);
        add(collapseButton, buttonContainer);
        add(closeButton, buttonContainer);

        controlContainer.appendChild(buttonContainer);
        controlContainer.appendChild(uidlContainer);


        
        vcaption.getElement().appendChild(controlContainer);
        controlContainer.getStyle().setVerticalAlign(VerticalAlign.TOP);
        closeButton.setStyleName(getClassName() + BUTTON);
        closeButton.addStyleDependentName(BUTTON_CLOSE);
        
        collapseButton.setStyleName(getClassName() + BUTTON);
        collapseButton.addStyleDependentName(BUTTON_COLLAPSE);
        
        controlContainer.setClassName(getClassName() + TOOLBAR);
        buttonContainer.setClassName(getClassName() + BUTTONBAR);
        uidlContainer.setClassName(getClassName() + WIDGET_SLOT);
    }
    
    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                vcaption.updateComponentSlotWidth();
            }
        });
    }
    
    public Widget getDraggableArea() {
        return vcaption;
    }

    public static String getClassName() {
        return VPortlet.getClassName() + CLASSNAME;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
        closeButton.setVisible(closable);
    }

    public void setCollapsible(boolean isCollapsible) {
        this.collapsible = isCollapsible;
        collapseButton.setVisible(isCollapsible);
    }

    public boolean isClosable() {
        return closable;
    }

    public boolean isCollapsible() {
        return collapsible;
    }
    
    public void setHeaderWidget(Widget widget) {
        if (widget != child) {
            replaceChildComponent(child, widget);
        }
    }
    
    public void updateCaption(UIDL uidl) {
        vcaption.updateCaption(uidl);
    }

    public void toggleCollapseStyles(boolean isCollapsed) {
        collapseButton.removeStyleDependentName(isCollapsed ? BUTTON_COLLAPSE : BUTTON_EXPAND);
        collapseButton.addStyleDependentName(isCollapsed ?  BUTTON_EXPAND : BUTTON_COLLAPSE);
    }
    
    private class VPortletCaption extends VCaption {
        
        int iconWidth = 0;
        
        public VPortletCaption(Paintable component, ApplicationConnection client) {
            super(component, client);
            getElement().getStyle().setHeight(100, Unit.PCT);
            getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
            getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
            getElement().getStyle().setProperty("zoom", "1");
        }
        
        public void updateComponentSlotWidth() {
            int offsetWidth = PortletHeader.this.getOffsetWidth() - getTextElement().getScrollWidth() - iconWidth - getHPadding();
            controlContainer.getStyle().setWidth(offsetWidth, Unit.PX);
            uidlContainer.getStyle().setWidth(offsetWidth - buttonContainer.getOffsetWidth(), Unit.PX);
            if (child != null) {
                client.handleComponentRelativeSize(child);
            }
        }
        
        @Override
        public boolean updateCaption(UIDL uidl) {
            boolean result = super.updateCaption(uidl);
            updateComponentSlotWidth();
            return result;
        }
        
        public int getHPadding() {    
            return DOMUtil.getHorizontalMargin(getElement());
        }
        
        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            if (event.getTypeInt() == Event.ONLOAD) {
                iconWidth = super.getRequiredWidth() - getTextElement().getOffsetWidth();
                updateComponentSlotWidth();
            }
        }
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {/*No server side correspondence*/}

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        if (oldComponent == child) {
            if (oldComponent != null && child != newComponent) {
                remove(child);
            }
            child = newComponent;
            add(newComponent, uidlContainer);
            newComponent.addDomHandler(blockingDownHandler, MouseDownEvent.getType());
        }
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return child == component;
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {/*NOP*/}

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (child == this.child) {
            return new RenderSpace(uidlContainer.getOffsetWidth(), uidlContainer.getOffsetHeight());
        }
        return null;
    }

    public void setAppConnection(ApplicationConnection client) {
        this.client = client;
    }

}
