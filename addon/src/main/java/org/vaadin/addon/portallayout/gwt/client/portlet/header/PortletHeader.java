package org.vaadin.addon.portallayout.gwt.client.portlet.header;

import org.vaadin.addon.portallayout.gwt.client.portlet.PortletChrome;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEventGwt;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEventGwt.HasPortletCloseEventHandlers;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEventGwt;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEventGwt.Handler;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEventGwt.HasPortletCollapseEventHandlers;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasTouchStartHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
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
import com.vaadin.client.LayoutManager;
import com.vaadin.client.Util;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;

public class PortletHeader extends ComplexPanel implements HasPortletCollapseEventHandlers, HasPortletCloseEventHandlers, HasMouseDownHandlers, HasTouchStartHandlers {

    public static final String CLASSNAME = "-header";

    private static final String BUTTON_CLOSE = "close";

    private static final String BUTTON_COLLAPSE = "collapse";

    private static final String BUTTON_EXPAND = "expand";

    private static final String BUTTON = "-button";

    private static final String BUTTONBAR = "-buttonbar";

    private static final String WIDGET_SLOT = "-widget-slot";

    private final Element container = DOM.createDiv();

    private final Element captionContainer = DOM.createSpan();

    private final Element buttonContainer = DOM.createDiv();

    private final Element toolbarContainer = DOM.createDiv();

    private final Button closeButton = new Button();

    private final Button collapseButton = new Button();

    private final Image icon = new Image();

    private Widget toolbarWidget;

    private LayoutManager lm;

    private ElementResizeListener toolbarResizeListener;

    private final ClickHandler closeButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            fireEvent(new PortletCloseEventGwt(getParent()));
        }
    };


    private final ClickHandler collapseButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            fireEvent(new PortletCollapseEventGwt(getParent()));
        }
    };

    public PortletHeader() {
        super();
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                final NativeEvent nativeEvent = event.getNativeEvent();
                final Element target = nativeEvent.getEventTarget().cast();
                if (getElement().isOrHasChild(target)) {
                    if (event.getTypeInt() == Event.ONMOUSEDOWN) {
                        getParent().blur();
                        final Widget w = Util.findWidget(target, null);
                        if (!(w instanceof HasWidgets)) {
                            nativeEvent.stopPropagation();
                        }
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

        closeButton.setStyleName(getClassName() + BUTTON);
        closeButton.addStyleDependentName(BUTTON_CLOSE);

        collapseButton.setStyleName(getClassName() + BUTTON);
        collapseButton.addStyleDependentName(BUTTON_COLLAPSE);

        buttonContainer.setClassName(getClassName() + BUTTONBAR);
        toolbarContainer.setClassName(getClassName() + WIDGET_SLOT);

        container.appendChild(buttonContainer);
        container.appendChild(toolbarContainer);
    }

    public void setLayoutManager(final LayoutManager lm) {
        this.lm = lm;
        lm.addElementResizeListener(buttonContainer, new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                updateToolbarRightPos();
            }
        });

        lm.addElementResizeListener(getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                updateCaptionWidth();
            }
        });

        lm.addElementResizeListener(captionContainer, new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {}
        });
    }

    private void updateCaptionWidth() {
        int buttonbarWidth = buttonContainer.getOffsetWidth();
        int totalWidth = lm.getOuterWidth(getElement());
        int toolbarWidth = toolbarWidget == null ? 0 : lm.getOuterWidth(toolbarWidget.getElement());
        int captionWidth = Math.max(0, totalWidth - buttonbarWidth - toolbarWidth - lm.getPaddingLeft(captionContainer));
        captionContainer.getStyle().setWidth(captionWidth, Style.Unit.PX);
    }

    private void updateToolbarRightPos() {
        int buttonContainerWidth = lm.getOuterWidth(buttonContainer);
        if (toolbarWidget != null) {
            toolbarWidget.getElement().getStyle().setRight(buttonContainerWidth, Style.Unit.PX);
        }
    }

    public void setIcon(String iconUri) {
        icon.setVisible(iconUri != null);
        if (iconUri != null) {
            iconUri = iconUri == null ? "" : iconUri;
            icon.setUrl(iconUri);
            icon.getElement().getStyle().setDisplay(iconUri.isEmpty() ? Display.NONE : Display.INLINE);
        }
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

    public void toggleCollapseStyles(boolean isCollapsed) {
        collapseButton.removeStyleDependentName(isCollapsed ? BUTTON_COLLAPSE : BUTTON_EXPAND);
        collapseButton.addStyleDependentName(isCollapsed ? BUTTON_EXPAND : BUTTON_COLLAPSE);
    }

    @Override
    public PortletChrome getParent() {
        return (PortletChrome) super.getParent();
    }

    @Override
    public HandlerRegistration addPortletCollapseEventHandler(Handler handler) {
        return addHandler(handler, PortletCollapseEventGwt.TYPE);
    }

    @Override
    public HandlerRegistration addPortletCloseEventHandler(PortletCloseEventGwt.Handler handler) {
        return addHandler(handler, PortletCloseEventGwt.TYPE);
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

    @Override
    public HandlerRegistration addTouchStartHandler(TouchStartHandler handler) {
        return addDomHandler(handler, TouchStartEvent.getType());
    }

    public void setToolbar(Widget toolbar) {
        if (this.toolbarWidget != null) {
            if (toolbarResizeListener != null) {
                lm.removeElementResizeListener(toolbarWidget.getElement(), toolbarResizeListener);
            }
            remove(toolbarWidget);
        }
        this.toolbarWidget = toolbar;
        if (toolbar != null) {
            add(toolbar, toolbarContainer);
        }

        if (toolbarWidget != null) {
            lm.addElementResizeListener(toolbarWidget.getElement(), new ElementResizeListener() {
                @Override
                public void onElementResize(ElementResizeEvent e) {
                    updateToolbarRightPos();
                    updateCaptionWidth();
                }
            });
        }
    }
}
