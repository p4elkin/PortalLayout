package org.vaadin.addon.portallayout.client.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.addon.portallayout.client.dnd.util.DOMUtil;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderInformation.FloatSize;
import com.vaadin.terminal.gwt.client.RenderInformation.Size;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

/**
 * The class representing the portlet in the portal. Basically has the header
 * with portlet controls and caption and the widget which plays the role of the
 * portlet contents.
 * 
 * @author p4elkin
 */
public class Portlet extends ComplexPanel implements PortalObject {

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

    private ApplicationConnection client;

    private boolean isCollapsed = false;

    private boolean isHeightRelative = false;

    private PortletLockState isLocked = PortletLockState.PLS_NOT_SET;

    private List<String> appliedStyles = new ArrayList<String>();

    private int vBorders = -1;

    private int hBorders = -1;

    public Portlet(Widget widget, final ApplicationConnection client, VPortalLayout parent) {
        super();

        this.client = client;
        this.animation = new ContentCollapseAnimation();
        this.fadeAnimation = new FadeAnimation();
        parentPortal = parent;
        content = widget;

        containerElement = DOM.createDiv();

        header = new PortletHeader(this, client);
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

        add(content, contentDiv);
    }

    public void renderContent(UIDL uidl) {
        if (content != null && (content instanceof Paintable)) {
            ((Paintable) content).updateFromUIDL(uidl, client);
        }
    }

    public void updateStyles(final List<String> newStyles) {
        final Iterator<String> styleIt = appliedStyles.iterator();
        while (styleIt.hasNext()) {
            removeStyleName(styleIt.next());
            styleIt.remove();
        }
        for (final String style : newStyles) {
            appliedStyles.add(style);
            addStyleName(style);
        }
    }

    public int getHeaderHeight() {
        return header.getOffsetHeight();
    }

    @Override
    public void addStyleName(String style) {
        super.addStyleName(style);
        vBorders = DOMUtil.getVerticalBorders(contentDiv);
        hBorders = DOMUtil.getHorizontalBorders(contentDiv);
    }

    @Override
    public void setWidgetSizes(int width, int height) {
        setPortletWidth(width);
        setPortletHeight(height);
    }

    public void setPortletHeight(int height) {
        int contentHeight = isHeightRelative ? height - header.getOffsetHeight() : height;
        int containerHeight = isHeightRelative ? height : height + header.getOffsetHeight();
        contentHeight = (contentHeight >= getVBorders()) ? contentHeight - getVBorders() : 0;
        contentSizeInfo.setHeight(contentHeight);
        contentDiv.getStyle().setHeight(contentSizeInfo.getHeight(), Unit.PX);
        containerElement.getStyle().setHeight(containerHeight, Unit.PX);
    }

    public void setPortletWidth(int width) {
        contentSizeInfo.setWidth(width >= getHBorders() ? width - getHBorders() : 0);
        contentDiv.getStyle().setWidth(contentSizeInfo.getWidth(), Unit.PX);
        containerElement.getStyle().setWidth(width, Unit.PX);
        header.setWidth(width + "px");
    }

    public int getVBorders() {
        vBorders = DOMUtil.getVerticalBorders(contentDiv);
        return isCollapsed ? 0 : vBorders;
    }

    public int getHBorders() {
        hBorders = DOMUtil.getHorizontalBorders(contentDiv);
        return hBorders;
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

    public VPortalLayout getParentPortal() {
        return parentPortal;
    }

    public void setParentPortal(VPortalLayout portal) {
        this.parentPortal = portal;
    }

    public int getPosition() {
        return parentPortal == null ? -1 : parentPortal.getChildPosition(this);
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

    public int getTotalHeight() {
        return header.getOffsetHeight() + getVBorders() + contentSizeInfo.getHeight();
    }

    @Override
    public int getContentHeight() {
        return getTotalHeight() - header.getOffsetHeight();
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
    public Portlet getPortletRef() {
        return this;
    }

    public void updateActions(final Map<String, String> actions) {
        header.updateActions(actions);
    }

    public void onActionTriggered(final String actionId) {
        parentPortal.onActionTriggered(this, actionId);
    }

    public void updateCaption(final UIDL uidl) {
        header.updateCaption(uidl);
    }

    public void blur() {
        content.getElement().blur();
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
                height = parentPortal.getRelativePortletHeight(Portlet.this) - getVBorders();
                setCollapsed(!isCollapsed);
            } else {
                setCollapsed(!isCollapsed);
                if (!isCollapsed) {
                    contentDiv.getStyle().setVisibility(Visibility.VISIBLE);
                    setPortletWidth(getOffsetWidth());
                    // client.handleComponentRelativeSize(content);
                }
                parentPortal.recalculateLayout();
                final Set<PortalObject> portletSet = parentPortal.getPortletSet();
                if (isHeightRelative) {
                    parentPortal.calculatePortletSizes(portletSet);
                    height = parentPortal.getRelativePortletHeight(Portlet.this) - getVBorders();
                } else {
                    portletSet.remove(Portlet.this);
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
            setPortletHeight(getContentHeight());
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            header.toggleCollapseStyles(isCollapsed);
            parentPortal.onPortletCollapseStateChanged(Portlet.this);
            Util.notifyParentOfSizeChange(parentPortal, false);
            if (isCollapsed)
                contentDiv.getStyle().setVisibility(Visibility.HIDDEN);
        }

    }

    @Override
    protected void onAttach() {
        super.onAttach();
        int position = getPosition();
        if (position != -1) {
            if (parentPortal.shouldAnimate(AnimationType.AT_ATTACH))
                fadeAnimation.start(true, parentPortal.getAnimationSpeed(AnimationType.AT_ATTACH));
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
                parentPortal.onPortletClose(Portlet.this);
            }
        }
    }

    public void setHeaderWidget(Widget widget) {
        header.setHeaderWidget(widget);
    }

    public PortletHeader getHeader() {
        return header;
    }
}
