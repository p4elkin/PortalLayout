package org.vaadin.addon.portallayout.client.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.addon.portallayout.client.dnd.PickupDragController;
import org.vaadin.addon.portallayout.client.dnd.util.DOMUtil;
import org.vaadin.addon.portallayout.client.ui.Portlet.PortletLockState;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.EventId;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderInformation.FloatSize;
import com.vaadin.terminal.gwt.client.RenderInformation.Size;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.StyleConstants;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.LayoutClickEventHandler;
import com.vaadin.terminal.gwt.client.ui.VMarginInfo;
import com.vaadin.terminal.gwt.client.ui.layout.CellBasedLayout.Spacing;

/**
 * Client-side implementation of the portal layout.
 * 
 * @author p4elkin
 */
public class VPortalLayout extends SimplePanel implements Paintable, Container {

    private final static PickupDragController commonDragController = new PickupDragController(RootPanel.get(), false);

    public static final String PORTLET_CLOSED_EVENT_ID = "PORTLET_CLOSED";

    public static final String PORTLET_COLLAPSE_EVENT_ID = "COLLAPSE_STATE_CHANGED";

    protected ApplicationConnection client;

    protected PortalDropController dropController;

    private PickupDragController localDragController = null;

    private final Map<Widget, Portlet> widgetToPortletContainer = new HashMap<Widget, Portlet>();

    private final Map<AnimationType, Boolean> animationModeMap = new EnumMap<AnimationType, Boolean>(
            AnimationType.class);

    private final Map<AnimationType, Integer> animationSpeedMap = new EnumMap<AnimationType, Integer>(
            AnimationType.class);

    private final Element marginWrapper = DOM.createDiv();

    private final FlowPanel portalContent = new FlowPanel() {

        @Override
        public void insert(Widget w, int beforeIndex) {
            super.insert(w, beforeIndex);
            VPortalLayout.this.updateSpacingOnPortletPositionChange((PortalObject) w, true);
            if (!isRendering)
                recalculateLayoutAndPortletSizes();
        };

        @Override
        public boolean remove(Widget w) {
            boolean result = super.remove(w);
            VPortalLayout.this.updateSpacingOnPortletPositionChange((PortalObject) w, false);
            if (!isRendering)
                recalculateLayoutAndPortletSizes();
            return result;
        };
    };

    private Size actualSizeInfo = new Size(0, 0);

    private Size sizeInfoFromUidl = null;

    protected final Spacing computedSpacing = new Spacing(0, 0);

    protected final Spacing activeSpacing = new Spacing(0, 0);

    private float sumRelativeHeight = 0f;

    private int capacity;

    private int consumedHeight;

    private boolean isSpacingEnabled = false;

    private boolean isCommunicative = true;

    private boolean isRendering = false;

    protected String paintableId;

    /**
     * Get the Common PickupDragController that should wire all the portals
     * together.
     * 
     * @return PickupDragController.
     */
    public PickupDragController getDragController() {
        if (isCommunicative)
            return commonDragController;
        if (localDragController == null)
            localDragController = new PickupDragController(RootPanel.get(), false);
        return localDragController;
    }

    private LayoutClickEventHandler clickEventHandler = new LayoutClickEventHandler(this, EventId.LAYOUT_CLICK) {

        @Override
        protected Paintable getChildComponent(Element element) {
            return getComponent(element);
        }

        @Override
        protected <H extends EventHandler> HandlerRegistration registerHandler(H handler, Type<H> type) {
            return addDomHandler(handler, type);
        }
    };

    public VPortalLayout() {
        super();
        getElement().appendChild(marginWrapper);
        setStyleName(PortalConst.CLASSNAME);
        setWidget(portalContent);

        getElement().getStyle().setProperty("overflow", "hidden");
        marginWrapper.getStyle().setProperty("overflow", "hidden");

        dropController = new PortalDropController(this);
        getDragController().registerDropController(dropController);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        isRendering = true;

        if (client.updateComponent(this, uidl, true)) {
            isRendering = false;
            return;
        }

        sizeInfoFromUidl = null;
        this.client = client;
        paintableId = uidl.getId();
        updateSpacingInfoFromUidl(uidl);
        updateMarginsFromUidl(uidl);
        updateAnimationsFromUidl(uidl);
        clickEventHandler.handleEventHandlerRegistration(client);
        final FloatSize relaiveSize = Util.parseRelativeSize(uidl);
        if (relaiveSize == null || relaiveSize.getHeight() == -1)
            sizeInfoFromUidl = new Size(parsePixel(uidl.getStringAttribute("width")) - getHorizontalMargins(),
                    parsePixel(uidl.getStringAttribute("height")) - getVerticalMargins());

        actualSizeInfo.setHeight(DOMUtil.getClientHeight(getElement()));
        actualSizeInfo.setWidth(DOMUtil.getClientWidth(getElement()));

        int pos = 0;
        final Map<Portlet, UIDL> realtiveSizePortletUIDLS = new HashMap<Portlet, UIDL>();
        final Map<Widget, Portlet> oldMap = new HashMap<Widget, Portlet>(widgetToPortletContainer);
        final Map<Paintable, UIDL> headerPaintablesMap = new HashMap<Paintable, UIDL>();
        for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext(); ++pos) {
            final UIDL itUidl = (UIDL) it.next();
            if (itUidl.getTag().equals("portlet")) {

                final UIDL bodyUidl = itUidl.getChildByTagName("body");

                final Boolean isClosable = bodyUidl.getBooleanAttribute(PortalConst.PORTLET_CLOSABLE);
                final Boolean isCollapsible = bodyUidl.getBooleanAttribute(PortalConst.PORTLET_COLLAPSIBLE);
                final Boolean isLocked = bodyUidl.getBooleanAttribute(PortalConst.PORTLET_LOCKED);
                final Boolean isCollapsed = bodyUidl.getBooleanAttribute(PortalConst.PORTLET_COLLAPSED);

                final UIDL childUidl = (UIDL) bodyUidl.getChildUIDL(0);
                final Paintable child = client.getPaintable(childUidl);
                final Widget widget = (Widget) child;

                if (oldMap.containsKey(widget))
                    oldMap.remove(widget);

                final Portlet portlet = findOrCreatePortlet(widget);

                final String[] styles = bodyUidl.getStringArrayAttribute("styles");
                portlet.updateStyles(Arrays.asList(styles));

                updatePortletInPosition(portlet, pos);
                setLock(portlet, isLocked);
                portlet.setClosable(isClosable);
                portlet.setCollapsible(isCollapsible);

                if (bodyUidl.hasAttribute(PortalConst.PORTLET_ACTION_IDS)) {
                    final String[] actions = bodyUidl.getStringArrayAttribute(PortalConst.PORTLET_ACTION_IDS);
                    final String[] icons = bodyUidl.getStringArrayAttribute(PortalConst.PORTLET_ACTION_ICONS);
                    assert icons.length == actions.length;
                    final Map<String, String> idToIconUrl = new LinkedHashMap<String, String>();
                    for (int i = 0; i < actions.length; ++i) {
                        idToIconUrl.put(actions[i], client.translateVaadinUri(icons[i]));
                    }
                    portlet.updateActions(idToIconUrl);
                }

                if (!isCollapsed.equals(portlet.isCollapsed()))
                    portlet.toggleCollapseState();

                final Size portletSize = portlet.getContentSizeInfo();
                portletSize.setWidth(actualSizeInfo.getWidth());

                if (!Util.isCached(childUidl)) {
                    portlet.tryDetectRelativeHeight(childUidl);
                }

                if (portlet.isHeightRelative())
                    realtiveSizePortletUIDLS.put(portlet, childUidl);
                else {
                    portlet.renderContent(childUidl);
                    if (!portlet.isCollapsed()) {
                        portletSize.setHeight(Util.getRequiredHeight(widget.getElement()));
                    }
                }

                final UIDL headerUidl = itUidl.getChildByTagName("header");
                if (headerUidl != null) {
                    Paintable p = client.getPaintable(headerUidl.getChildUIDL(0));
                    if (p instanceof Widget) {
                        portlet.setHeaderWidget((Widget) p);
                        headerPaintablesMap.put(p, headerUidl.getChildUIDL(0));
                    }
                }
            }
        }

        recalculateLayoutAndPortletSizes();

        for (final Portlet p : realtiveSizePortletUIDLS.keySet()) {
            final UIDL relUidl = realtiveSizePortletUIDLS.get(p);
            p.renderContent(relUidl);
        }

        updateCommunicationAbility(uidl);

        final Iterator<?> it = headerPaintablesMap.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Paintable, UIDL> entry = (Entry<Paintable, UIDL>) it.next();
            entry.getKey().updateFromUIDL(entry.getValue(), client);
        }

        for (final Widget w : oldMap.keySet()) {
            final Portlet p = oldMap.get(w);
            portalContent.remove(p);
            widgetToPortletContainer.remove(w);
            client.unregisterPaintable((Paintable) w);
        }
        isRendering = false;
    }

    private void updateAnimationsFromUidl(final UIDL uidl) {
        for (final AnimationType at : Arrays.asList(AnimationType.values())) {
            if (uidl.hasAttribute(at.toString())) {
                setAnimationMode(at, uidl.getBooleanAttribute(at.toString()));
            }
            if (uidl.hasAttribute(at.toString() + "-SPEED")) {
                setAnimationSpeed(at, uidl.getIntAttribute(at.toString() + "-SPEED"));
            }
        }
    }

    private int getVerticalMargins() {
        return DOMUtil.getVerticalMargin(marginWrapper);
    }

    private int getHorizontalMargins() {
        return DOMUtil.getHorizontalMargin(marginWrapper);
    }

    private void updateMarginsFromUidl(UIDL uidl) {
        VMarginInfo marginInfo = new VMarginInfo(uidl.getIntAttribute("margins"));
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_TOP, marginInfo.hasTop());
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_RIGHT, marginInfo.hasRight());
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_BOTTOM, marginInfo.hasBottom());
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_LEFT, marginInfo.hasLeft());
    }

    @Override
    protected Element getContainerElement() {
        return marginWrapper;
    }

    private Iterator<Widget> getPortalContentIterator() {
        return portalContent.iterator();
    }

    private void updateCommunicationAbility(final UIDL uidl) {
        Boolean canCommunicate = uidl.getBooleanAttribute(PortalConst.PORTAL_COMMUNICATIVE);
        final PickupDragController currentController = getDragController();
        if (canCommunicate != isCommunicative) {
            currentController.unregisterDropController(dropController);
            isCommunicative = canCommunicate;
            final PickupDragController newController = getDragController();
            newController.registerDropController(dropController);
            for (final Portlet portlet : widgetToPortletContainer.values())
                if (!portlet.isLocked()) {
                    currentController.makeNotDraggable(portlet);
                    newController.makeDraggable(portlet, portlet.getDraggableArea());
                }
        }
    }

    private void setLock(final Portlet portlet, boolean isLocked) {
        PortletLockState formerLockState = portlet.getLockState();
        portlet.setLocked(isLocked);
        if (!isLocked && formerLockState != PortletLockState.PLS_NOT_LOCKED)
            getDragController().makeDraggable(portlet, portlet.getDraggableArea());
        else if (isLocked && formerLockState == PortletLockState.PLS_NOT_LOCKED)
            getDragController().makeNotDraggable(portlet);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        final PickupDragController dragController = getDragController();
        for (final Portlet p : widgetToPortletContainer.values()) {
            dragController.makeNotDraggable(p);
        }
        dragController.unregisterDropController(dropController);
    }

    private void updateSpacingInfoFromUidl(final UIDL uidl) {
        boolean newSpacingEnabledState = uidl.getBooleanAttribute("spacing");
        if (isSpacingEnabled != newSpacingEnabledState) {
            isSpacingEnabled = newSpacingEnabledState;
            activeSpacing.vSpacing = isSpacingEnabled ? computedSpacing.vSpacing : 0;
        }
    }

    /**
     * Calculate height consumed by the fixed sized portlets and distribute the
     * remaining height between the relative sized portlets. When the relative
     * height comes to consideration - if the total sum of percentages overflows
     * 100, that value is normalized, so every relative height portlet would get
     * its piece of space.
     */
    private void recalculateLayoutAndPortletSizes() {
        recalculateLayout();
        final Set<PortalObject> objSet = getPortletSet();
        final PortalDropPositioner p = dropController.getDummy();
        if (p != null)
            objSet.add(p);
        calculatePortletSizes(objSet);
    }

    public Set<PortalObject> getPortletSet() {
        final Collection<Portlet> portlets = widgetToPortletContainer.values();
        final Set<PortalObject> objSet = new HashSet<PortalObject>();
        objSet.addAll(portlets);
        return objSet;
    }

    public void setContainerHeight(int newHeight) {
        int contentHeight = portalContent.getOffsetHeight();
        setDOMHeight(newHeight);
        if (newHeight != contentHeight && getPortletCount() > 0 && !isRendering) {
            Util.notifyParentOfSizeChange(this, false);
        }
    }

    public int recalculateLayout() {
        consumedHeight = 0;
        sumRelativeHeight = 0;
        int contentsSize = getChildCount();
        int newHeight = 0;
        final Iterator<Widget> it = getPortalContentIterator();
        while (it.hasNext()) {
            final PortalObject p = (PortalObject) it.next();
            final Portlet corresponingPortlet = p.getPortletRef();
            int currentPortletIndex = portalContent.getWidgetIndex(corresponingPortlet);
            if (currentPortletIndex != -1 && currentPortletIndex != portalContent.getWidgetIndex(p))
                continue;
            if (p.isHeightRelative()) {
                sumRelativeHeight += p.getRelativeHeightValue();
            } else {
                consumedHeight += p.getRequiredHeight();
            }

        }
        consumedHeight += (contentsSize - 1) * activeSpacing.vSpacing;
        if (sizeInfoFromUidl != null && consumedHeight < sizeInfoFromUidl.getHeight())
            newHeight = sizeInfoFromUidl.getHeight();
        else
            newHeight = Math.max(actualSizeInfo.getHeight(), consumedHeight);
        setContainerHeight(newHeight);
        return newHeight;
    }

    public void calculatePortletSizes(final Set<PortalObject> objSet) {
        int width = DOMUtil.getClientWidth(marginWrapper);
        final Iterator<PortalObject> it = objSet.iterator();
        while (it.hasNext()) {
            final PortalObject portalObject = (PortalObject) it.next();
            int height = portalObject.isHeightRelative() ? getRelativePortletHeight(portalObject) : portalObject
                    .getContentHeight();
            portalObject.setWidgetSizes(width, height);
        }
        if (client != null)
            client.runDescendentsLayout(this);
    }

    public void updateSpacingOnPortletPositionChange(final PortalObject object, boolean attached) {
        int position = portalContent.getWidgetIndex(object);
        if (attached) {
            object.setSpacingValue(position == 0 ? 0 : getVerticalSpacing());
            if (position == 0 && getChildCount() > 1) {
                getChildAt(1).setSpacingValue(getVerticalSpacing());
            }
        } else {
            object.setSpacingValue(0);
            if (getChildCount() > 0) {
                getChildAt(0).setSpacingValue(0);
            }
        }
    }

    public int getResidualHeight() {
        return DOMUtil.getClientHeight(marginWrapper) - consumedHeight - getVerticalMargins();
    }

    public int getRelativePortletHeight(final PortalObject portalObject) {
        int headerHeight = portalObject.getPortletRef().getHeaderHeight();
        float newRealtiveHeight = normalizedRealtiveRatio() * portalObject.getRelativeHeightValue();
        return Math.max((int) (getResidualHeight() * newRealtiveHeight / 100), headerHeight);
    }

    private void setDOMHeight(int height) {
        getElement().getStyle().setPropertyPx("height", height + getVerticalMargins());
        marginWrapper.getStyle().setPropertyPx("height", height);
        portalContent.setHeight(height + "px");
    }

    public PortalObject getChildAt(int i) {
        return (PortalObject) portalContent.getWidget(i);
    }

    public int getChildCount() {
        return portalContent.getWidgetCount();
    }

    public int getChildPosition(final PortalObject child) {
        return portalContent.getWidgetIndex(child);
    }

    public FlowPanel getContentPanel() {
        return portalContent;
    }

    private float normalizedRealtiveRatio() {
        float result = 0;
        if (sumRelativeHeight != 0f)
            result = (sumRelativeHeight <= 100) ? 1 : 100 / sumRelativeHeight;
        return result;
    }

    private Portlet findOrCreatePortlet(Widget widget) {
        Portlet result = widgetToPortletContainer.get(widget);
        if (result == null)
            result = createPortlet(widget);
        return result;
    }

    private final Portlet createPortlet(Widget widget) {
        final Portlet result = new Portlet(widget, client, this);
        widgetToPortletContainer.put(widget, result);
        return result;
    }

    public void onPortletClose(final Portlet portlet) {
        final Paintable child = portlet.getContentAsPaintable();
        if (child != null) {
            widgetToPortletContainer.remove(portlet.getContent());
            client.updateVariable(paintableId, PortalConst.PORTLET_CLOSED, child, true);
        }
    }

    public void onActionTriggered(final Portlet portlet, String key) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PortalConst.PAINTABLE_MAP_PARAM, portlet.getContentAsPaintable());
        params.put(PortalConst.PORTLET_ACTION_ID, key);
        client.updateVariable(paintableId, PortalConst.PORTLET_ACTION_TRIGGERED, params, true);
    }

    public void onPortletCollapseStateChanged(final Portlet portlet) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PortalConst.PAINTABLE_MAP_PARAM, portlet.getContentAsPaintable());
        params.put(PortalConst.PORTLET_COLLAPSED, portlet.isCollapsed());
        client.updateVariable(paintableId, PortalConst.PORTLET_COLLAPSE_STATE_CHANGED, params, true);
    }

    private void updatePortletInPosition(Portlet portlet, int i) {
        int currentPosition = getChildPosition(portlet);
        if (i != currentPosition) {
            portlet.removeFromParent();
            addToRootElement(portlet, i);
        }
    }

    public void addToRootElement(final PortalObject widget, int position) {
        portalContent.insert(widget, position);
    }

    public void onPortletMovedOut(Portlet portlet) {
        final Paintable child = portlet.getContentAsPaintable();
        if (child != null) {
            widgetToPortletContainer.remove(portlet.getContent());
            client.updateVariable(paintableId, PortalConst.PORTLET_REMOVED, child, true);
        }
    }

    public void onPortletPositionUpdated(Portlet portlet, int newPosition) {
        final Paintable child = portlet.getContentAsPaintable();
        if (child != null) {
            portlet.setParentPortal(this);
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put(PortalConst.PAINTABLE_MAP_PARAM, child);
            params.put(PortalConst.PORTLET_POSITION, newPosition);
            client.updateVariable(paintableId, PortalConst.PORTLET_POSITION_UPDATED, params, true);
            widgetToPortletContainer.put(portlet.getContent(), portlet);
        }
    }

    public int getPortletCount() {
        return widgetToPortletContainer.size();
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        if (width != null && !width.isEmpty()) {
            int widthPx = parsePixel(width);
            actualSizeInfo.setWidth(widthPx - getHorizontalMargins());
            if (!isRendering)
                recalculateLayoutAndPortletSizes();
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (height != null && !height.isEmpty()) {
            int heightPx = parsePixel(height);
            actualSizeInfo.setHeight(heightPx - getVerticalMargins());
            if (!isRendering)
                recalculateLayoutAndPortletSizes();
        }
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        final Portlet portlet = widgetToPortletContainer.remove(oldComponent);
        if (portlet != null) {
            portlet.setContent(newComponent);
            client.unregisterPaintable((Paintable) oldComponent);
            widgetToPortletContainer.put(newComponent, portlet);
            recalculateLayoutAndPortletSizes();
        }
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return widgetToPortletContainer.containsKey(component);
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        final Portlet portlet = widgetToPortletContainer.get(component);
        if (portlet != null) {
            portlet.updateCaption(uidl);
        }
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        recalculateLayoutAndPortletSizes();
        /**
         * if needed - notification is already sent, no need to propagate.
         */
        return true;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {

        final Portlet portlet = widgetToPortletContainer.get(child);
        final Size sizeInfo = portlet.getContentSizeInfo();

        int height = sizeInfo.getHeight();
        if (portlet.isHeightRelative())
            height = (int) (((float) height) * 100 / portlet.getRelativeHeightValue());
        return new RenderSpace(sizeInfo.getWidth(), height);
    }

    @Override
    public void setStyleName(String style) {
        super.setStyleName(style);
        measureSpacing();
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getVerticalSpacing() {
        return activeSpacing.vSpacing;
    }

    /**
     * . Takes a String value e.g. "12px" and parses that to int 12
     * 
     * @param String
     *            value with "px" ending
     * @return int the value from the string before "px", converted to int
     */
    public static int parsePixel(String value) {
        if (value == null || value.equals("")) {
            return 0;
        }
        Float ret = Float.parseFloat(value.length() > 2 ? value.substring(0, value.length() - 2) : value);
        return (int) Math.ceil(ret);
    }

    private static DivElement measurement;

    private static DivElement helper;

    static {
        helper = Document.get().createDivElement();
        helper.setInnerHTML("<div style=\"position:absolute;top:0;left:0;height:0;visibility:hidden;overflow:hidden;\">"
                + "<div style=\"width:0;height:0;visibility:hidden;overflow:hidden;\">"
                + "</div></div>"
                + "<div style=\"position:absolute;height:0;overflow:hidden;\"></div>");
        NodeList<Node> childNodes = helper.getChildNodes();
        measurement = (DivElement) childNodes.getItem(1);
    }

    protected boolean measureSpacing() {
        if (!isAttached()) {
            return false;
        }
        measurement.setClassName(PortalConst.STYLENAME_SPACING);
        getElement().appendChild(helper);
        computedSpacing.vSpacing = measurement.getOffsetWidth();
        computedSpacing.hSpacing = measurement.getOffsetWidth();
        getElement().removeChild(helper);
        return true;
    }

    private Paintable getComponent(Element element) {
        return Util.getPaintableForElement(client, this, element);
    }

    public boolean shouldAnimate(final AnimationType animationType) {
        Boolean result = animationModeMap.get(animationType);
        return result == null || result;
    }

    public void setAnimationMode(final AnimationType animationType, boolean animate) {
        animationModeMap.put(animationType, animate);
    }

    public void setAnimationSpeed(final AnimationType animationType, int speed) {
        animationSpeedMap.put(animationType, speed);
    }

    public int getAnimationSpeed(final AnimationType animationType) {
        Integer speed = animationSpeedMap.get(animationType);
        if (speed == null) {
            switch (animationType) {
            case AT_ATTACH:
                return PortalConst.DEFAULT_ATTACH_SPEED;
            case AT_CLOSE:
                return PortalConst.DEFAULT_CLOSE_SPEED;
            case AT_COLLAPSE:
                return PortalConst.DEFAULT_COLLAPSE_SPEED;
            }
        }
        return speed;
    }

    public void onPortletLeft(final PortalDropPositioner dummy, int dummyIndex) {
        addToRootElement(dummy, dummyIndex);
        /**
         * Uncomment if desire to make leave/enter events.
         * client.updateVariable(paintableId, PortalConst.PORTLET_LEFT,
         * dummy.getPortletRef().getContentAsPaintable(), true);
         */
    }

    public void onPortletEntered(final PortalDropPositioner dummy, int dummyIndex) {
        addToRootElement(dummy, dummyIndex);
        /**
         * Uncomment if desire to make leave/enter events.
         * client.updateVariable(paintableId, PortalConst.PORTLET_ENTERED,
         * dummy.getPortletRef().getContentAsPaintable(), true);
         */
    }

}