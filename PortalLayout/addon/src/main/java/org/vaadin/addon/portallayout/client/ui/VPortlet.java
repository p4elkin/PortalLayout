package org.vaadin.addon.portallayout.client.ui;

import java.util.Set;

import org.vaadin.csstools.client.ComputedStyle;
import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderInformation.FloatSize;
import com.vaadin.terminal.gwt.client.RenderInformation.Size;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * The class representing the portlet in the portal. Basically has the header
 * with portlet controls and caption and the widget which plays the role of the
 * portlet contents.
 * 
 * @author p4elkin
 */
@SuppressWarnings("serial")
public class VPortlet extends ComplexPanel implements PortalObject, Container, ClientSideHandler {

    public enum PortletLockState {
        PLS_NOT_SET, PLS_LOCKED, PLS_NOT_LOCKED;
    }

    private final static String CLASSNAME = "v-portlet";

    private static final String WRAPPER_CLASSNAME = "-wrapper";

    private static final String CONTENT_CLASSNAME = "-content";

    private static final String DRAGGABLE_SUFFIX = "draggable";

    private Size contentSizeInfo = new Size(0, 0);

    private PortletHeader header;

    private Widget content;

    private Element containerElement;

    private Element contentDiv;

    private VPortalLayout parentPortal = null;

    private final ContentCollapseAnimation animation;

    private final FadeAnimation fadeAnimation;

    private FloatSize relativeSize;

    private boolean isCollapsed = false;

    private boolean isHeightRelative = false;

    private PortletLockState isLocked = PortletLockState.PLS_NOT_SET;

    private ComputedStyle contentStyle = null;
    
    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("setClosable", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setClosable((Boolean)params[0]);
                }
            });
            
            register("setCollapsibe", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setCollapsible((Boolean)params[0]);
                }
            });
            
            register("setLocked", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setLocked((Boolean)params[0]);
                }
            });
            
            register("setCollapsed", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    setCollapsed((Boolean)params[0]);
                }
            });
            
        }
    };
    
    protected ApplicationConnection client;

    protected String paintableId;
    
    public VPortlet() {
        super();

        this.animation = new ContentCollapseAnimation();
        this.fadeAnimation = new FadeAnimation();

        containerElement = DOM.createDiv();

        header = new PortletHeader(this);
        header.getElement().getStyle().setFloat(Style.Float.LEFT);
        add(header, containerElement);

        contentDiv = DOM.createDiv();
        contentDiv.addClassName(CLASSNAME + CONTENT_CLASSNAME);
        contentDiv.getStyle().setFloat(Style.Float.LEFT);
        contentDiv.getStyle().setOverflow(Overflow.HIDDEN);

        containerElement.appendChild(contentDiv);
        setElement(containerElement);
        setStyleName(CLASSNAME);
        addStyleDependentName(DRAGGABLE_SUFFIX);
        containerElement.addClassName(CLASSNAME + WRAPPER_CLASSNAME);
    }

    public int getHeaderHeight() {
        return header.getOffsetHeight();
    }

    @Override
    public void setWidgetSizes(int width, int height) {}

    public int getVBorders() {
        return isCollapsed ? 0 : contentStyle.getBorder()[0] + contentStyle.getBorder()[2];
    }

    public int getHBorders() {
        return contentStyle.getBorder()[1] + contentStyle.getBorder()[3];
    }

    public Paintable getContentAsPaintable() {
        return (content == null || !(content instanceof Paintable)) ? null : (Paintable) content;
    }

    public Widget getContent() {
        return content;
    }

    public void setContent(Widget content) {
        this.content = content;
    }

    public Widget getDraggableArea() {
        return header.getDraggableArea();
    }

    public boolean tryDetectRelativeHeight(final UIDL uidl) {
        relativeSize = Util.parseRelativeSize(uidl);
        isHeightRelative = relativeSize != null && relativeSize.getHeight() > 0;
        return isHeightRelative;
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
    }

    public void setLocked(boolean isLocked) {
        boolean oldState = this.isLocked();
        this.isLocked = isLocked ? PortletLockState.PLS_LOCKED : PortletLockState.PLS_NOT_LOCKED;
        if (isLocked != oldState) {
            if (!isLocked) {
                addStyleDependentName(DRAGGABLE_SUFFIX);
            } else
                removeStyleDependentName(DRAGGABLE_SUFFIX);
        }
    }

    public boolean isLocked() {
        return isLocked == PortletLockState.PLS_LOCKED;
    }

    public PortletLockState getLockState() {
        return isLocked;
    }

    public void close() {
        fadeAnimation.start(
                false,
                parentPortal.shouldAnimate(AnimationType.AT_CLOSE) ? parentPortal
                        .getAnimationSpeed(AnimationType.AT_CLOSE) : 0);
    }

    public void toggleCollapseState() {
        animation.start(parentPortal.shouldAnimate(AnimationType.AT_COLLAPSE) ? parentPortal
                .getAnimationSpeed(AnimationType.AT_COLLAPSE) : 0);
    }

    public Size getContentSizeInfo() {
        return contentSizeInfo;
    }

    public static String getClassName() {
        return CLASSNAME;
    }

    public int getSpacing() {
        return parentPortal.getVerticalSpacing();
    }

    public void setClosable(boolean closable) {
        header.setClosable(closable);
    }

    public void setCollapsible(Boolean isCollapsible) {
        header.setCollapsible(isCollapsible);
    }

    public boolean isClosable() {
        return header.isClosable();
    }

    public boolean isCollapsible() {
        return header.isCollapsible();
    }

    @Override
    public int getRequiredHeight() {
        int result = header.getOffsetHeight() + contentDiv.getOffsetHeight();
        return result;
    }

    @Override
    public int getContentHeight() {
        return content.getOffsetHeight();
    }

    @Override
    public float getRelativeHeightValue() {
        if (relativeSize != null && !isCollapsed)
            return relativeSize.getHeight();
        return 0f;
    }

    @Override
    public boolean isHeightRelative() {
        return isHeightRelative;
    }

    @Override
    public void setSpacingValue(int spacing) {
        containerElement.getStyle().setPropertyPx("marginTop", spacing);
    }

    @Override
    public VPortlet getPortletRef() {
        return this;
    }

    public void blur() {
        content.getElement().blur();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        header.setWidth("100%");
        contentStyle = new ComputedStyle(contentDiv);
        int position = parentPortal == null ? -1 : parentPortal.getChildPosition(this);
        if (position != -1) {
            if (parentPortal.shouldAnimate(AnimationType.AT_ATTACH))
                fadeAnimation.start(true, parentPortal.getAnimationSpeed(AnimationType.AT_ATTACH));
        }
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        this.header.setAppConnection(client);
        
        proxy.update(this, uidl, client);
        
        updateHeader(uidl);
        updateContent(uidl);
    }

    private void updateContent(final UIDL uidl) {
        final UIDL contentUidl = uidl.getChildByTagName("content");
        if (contentUidl != null) {
            final UIDL paintableUidl = contentUidl.getChildUIDL(0);
            final Paintable p = client.getPaintable(paintableUidl);
            final Widget content = (Widget)p;
            if (!hasChildComponent(content)) {
                replaceChildComponent(this.content, content);
                this.content = content;
            }            
            p.updateFromUIDL(paintableUidl, client);
        }
    }

    private void updateHeader(final UIDL uidl) {
        final UIDL headerUidl = uidl.getChildByTagName("header");
        if (headerUidl != null) {
            final UIDL paintableUidl = headerUidl.getChildUIDL(0);
            final Paintable p = client.getPaintable(paintableUidl);
            final Widget header = (Widget)p;
            this.header.setHeaderWidget(header);
            p.updateFromUIDL(paintableUidl, client);
        }
    }

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unknown server call: " + method);
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        if (oldComponent != null) {
            remove(oldComponent);
            if (oldComponent instanceof Paintable) {
                client.unregisterPaintable((Paintable)oldComponent);   
            }
        }
        add(newComponent, contentDiv);
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return getChildren().contains(component);
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        header.updateCaption(uidl);
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            int width = getOffsetWidth() - getHBorders();
            int height = getOffsetHeight() - getVBorders() - header.getOffsetHeight();
            return new RenderSpace(width, height);
        }
        return null;
    }
    
    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        header.setWidth(width);
    }
    
    private class ContentCollapseAnimation extends Animation {

        private int height;

        @Override
        protected void onStart() {
            super.onStart();
        }

        public void start(int speed) {
            cancel();
            double duration = 0;
            if (!isCollapsed && isHeightRelative) {
                height = parentPortal.getRelativePortletHeight(VPortlet.this) - getVBorders();
                setCollapsed(!isCollapsed);
            } else {
                setCollapsed(!isCollapsed);
                if (!isCollapsed) {
                    contentDiv.getStyle().setVisibility(Visibility.VISIBLE);
                    // client.handleComponentRelativeSize(content);
                }
                parentPortal.recalculateLayout();
                final Set<PortalObject> portletSet = parentPortal.getPortletSet();
                if (isHeightRelative) {
                    parentPortal.calculatePortletSizes(portletSet);
                    height = parentPortal.getRelativePortletHeight(VPortlet.this) - getVBorders();
                } else {
                    portletSet.remove(VPortlet.this);
                    parentPortal.calculatePortletSizes(portletSet);
                    height = content.getOffsetHeight();
                }
            }
            if (speed > 0) {
                duration = (double) height / (double) speed * 1000d;
            }
            run((int) duration);
        }

        @Override
        protected void onUpdate(double progress) {
            int heightValue = (int) (isCollapsed ? (1 - progress) * height : progress * height);
            contentSizeInfo.setHeight(heightValue);
            //setPortletHeight(getContentHeight());
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            header.toggleCollapseStyles(isCollapsed);
            parentPortal.onPortletCollapseStateChanged(VPortlet.this);
            Util.notifyParentOfSizeChange(parentPortal, false);
            if (isCollapsed)
                contentDiv.getStyle().setVisibility(Visibility.HIDDEN);
        }

    }

    
    protected class FadeAnimation extends Animation {

        private boolean fadeIn;

        public void start(boolean isOpening, int speed) {
            this.fadeIn = isOpening;
            if (fadeIn) {
                getElement().getStyle().setOpacity(0);
            }
            run(speed);
        }

        @Override
        protected void onUpdate(double progress) {
            final String msOpacityPrpertyValue = "progid:DXImageTransform.Microsoft.Alpha(Opacity=";
            if (fadeIn) {
                if (BrowserInfo.get().isIE8()) {
                    getElement().getStyle().setProperty("filter", msOpacityPrpertyValue + (int) (progress * 100) + ")");
                } else {
                    getElement().getStyle().setOpacity(progress);
                }
            } else {
                if (BrowserInfo.get().isIE8()) {
                    getElement().getStyle().setProperty("filter",
                            msOpacityPrpertyValue + (int) ((1 - progress) * 100) + ")");
                } else {
                    getElement().getStyle().setOpacity(1 - progress);
                }
            }
        }

        @Override
        public void onComplete() {
            super.onComplete();
            getElement().getStyle().clearOpacity();
            if (!fadeIn) {
                removeFromParent();
                parentPortal.onPortletClose(VPortlet.this);
            }
        }
    }


    public void setPortal(final VPortalLayout portal) {
        this.parentPortal = portal;
    }
    
    public VPortalLayout getPortal() {
        return parentPortal;
    }
}
