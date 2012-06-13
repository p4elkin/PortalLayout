package org.vaadin.addon.portallayout.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.addon.portallayout.client.dnd.PickupDragController;
import org.vaadin.addon.portallayout.client.dnd.util.DOMUtil;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalDropPositioner;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalObject;
import org.vaadin.addon.portallayout.client.ui.portlet.VPortlet;
import org.vaadin.csstools.client.CSSRule;
import org.vaadin.csstools.client.ComputedStyle;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderInformation.FloatSize;
import com.vaadin.terminal.gwt.client.RenderInformation.Size;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

/**
 * Client-side implementation of the portal layout.
 * 
 * @author p4elkin
 */
public class VPortalLayout extends ComplexPanel implements Paintable, Container, InsertPanel.ForIsWidget {

    private final static PickupDragController commonDragController = new PickupDragController(RootPanel.get(), false);

    public static final String PORTLET_CLOSED_EVENT_ID = "PORTLET_CLOSED";

    public static final String PORTLET_COLLAPSE_EVENT_ID = "COLLAPSE_STATE_CHANGED";

    protected ApplicationConnection client;

    protected PortalDropController dropController;

    private CSSRule spacingRule = null;

    private final List<VPortlet> portlets = new ArrayList<VPortlet>();

    private final Element marginWrapper = DOM.createDiv();

    private final Element contentEl = DOM.createDiv();

    private Size actualSizeInfo = new Size(0, 0);

    private Size sizeInfoFromUidl = null;

    private float sumRelativeHeight = 0f;

    private int consumedHeight;

    private boolean isSpacingEnabled = false;

    private boolean isRendering = false;

    protected String paintableId;

    public PickupDragController getDragController() {
        return null;
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        isRendering = true;
        sizeInfoFromUidl = null;
        this.client = client;
        this.paintableId = uidl.getId();
        final FloatSize relaiveSize = Util.parseRelativeSize(uidl);
        if (relaiveSize == null || relaiveSize.getHeight() == -1) {
            sizeInfoFromUidl = new Size(parsePixel(uidl.getStringAttribute("width")) - getHorizontalMargins(),
                    parsePixel(uidl.getStringAttribute("height")) - getVerticalMargins());
        }

        actualSizeInfo.setHeight(DOMUtil.getClientHeight(getElement()));
        actualSizeInfo.setWidth(DOMUtil.getClientWidth(getElement()));

        int pos = 0;
        final Map<VPortlet, UIDL> realtiveSizePortletUIDLS = new HashMap<VPortlet, UIDL>();
        final List<VPortlet> orphanCandidates = new ArrayList<VPortlet>(portlets);
        for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext(); ++pos) {
            final UIDL portletUidl = (UIDL) it.next();
            final Paintable child = client.getPaintable(portletUidl);
            if (child instanceof VPortlet) {
                final VPortlet portlet = (VPortlet) child;
                orphanCandidates.remove(portlet);
                updatePortletInPosition(portlet, pos);

                final Size portletSize = portlet.getContentSizeInfo();
                portletSize.setWidth(actualSizeInfo.getWidth());

                if (!Util.isCached(portletUidl)) {
                    portlet.tryDetectRelativeHeight(portletUidl);
                }

                if (portlet.isHeightRelative())
                    realtiveSizePortletUIDLS.put(portlet, portletUidl);
                else {
                    portlet.updateFromUIDL(portletUidl, client);
                    if (!portlet.isCollapsed()) {
                        portletSize.setHeight(Util.getRequiredHeight(portlet.getElement()));
                    }
                }
            }
        }

        recalculateLayoutAndPortletSizes();

        for (final VPortlet p : realtiveSizePortletUIDLS.keySet()) {
            final UIDL relUidl = realtiveSizePortletUIDLS.get(p);
            p.updateFromUIDL(relUidl, client);
        }

        for (final VPortlet w : orphanCandidates) {
            remove(w);
            portlets.remove(w);
            client.unregisterPaintable((Paintable) w);
        }

        isRendering = false;
    }

    private int getVerticalMargins() {
        return DOMUtil.getVerticalMargin(marginWrapper);
    }

    private int getHorizontalMargins() {
        return DOMUtil.getHorizontalMargin(marginWrapper);
    }


    private Iterator<Widget> getPortalContentIterator() {
        return getChildren().iterator();
    }

    private void recalculateLayoutAndPortletSizes() {
        recalculateLayout();
        final Set<PortalObject> objSet = getPortletSet();
        final PortalDropPositioner p = dropController.getDummy();
        if (p != null) {
            objSet.add(p);
        }
        calculatePortletSizes(objSet);
    }

    public Set<PortalObject> getPortletSet() {
        return new HashSet<PortalObject>(portlets);
    }

    public void setContainerHeight(int newHeight) {
        int contentHeight = getOffsetHeight();
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
            final VPortlet corresponingPortlet = p.getPortletRef();
            int currentPortletIndex = getWidgetIndex(corresponingPortlet);
            if (currentPortletIndex != -1 && currentPortletIndex != getWidgetIndex(p))
                continue;
            if (p.isHeightRelative()) {
                sumRelativeHeight += p.getRelativeHeightValue();
            } else {
                consumedHeight += p.getRequiredHeight();
            }

        }
        consumedHeight += (contentsSize - 1) * getVerticalSpacing();
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
            int height = portalObject.isHeightRelative() ? getRelativePortletHeight(portalObject) : portalObject.getContentHeight();
            portalObject.setWidgetSizes(width, height);
        }
        if (client != null)
            client.runDescendentsLayout(this);
    }

    public void updateSpacingOnPortletPositionChange(final PortalObject object, boolean attached) {
        int position = getWidgetIndex(object);
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

    public PortalObject getChildAt(int i) {
        return (PortalObject) getWidget(i);
    }

    public int getChildCount() {
        return getWidgetCount();
    }

    public int getChildPosition(final PortalObject child) {
        return getWidgetIndex(child);
    }

    private float normalizedRealtiveRatio() {
        float result = 0;
        if (sumRelativeHeight != 0f)
            result = (sumRelativeHeight <= 100) ? 1 : 100 / sumRelativeHeight;
        return result;
    }

    public void onPortletClose(final VPortlet portlet) {
        final Paintable child = portlet.getContentAsPaintable();
        if (child != null) {
            portlets.remove(portlet.getContent());
            client.updateVariable(paintableId, PortalConst.PORTLET_CLOSED, child, true);
        }
    }

    public void onPortletCollapseStateChanged(final VPortlet portlet) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PortalConst.PAINTABLE_MAP_PARAM, portlet.getContentAsPaintable());
        params.put(PortalConst.PORTLET_COLLAPSED, portlet.isCollapsed());
        client.updateVariable(paintableId, PortalConst.PORTLET_COLLAPSE_STATE_CHANGED, params, true);
    }

    private void updatePortletInPosition(VPortlet portlet, int i) {
        int currentPosition = getChildPosition(portlet);
        if (i != currentPosition) {
            portlet.removeFromParent();
            portlets.add(portlet);
            addToRootElement(portlet, i);
            //portlet.setPortal(this);
        }
    }

    @Override
    public void insert(Widget w, Element el, int beforeIndex) {
        super.insert(w, el, beforeIndex, true);
        VPortalLayout.this.updateSpacingOnPortletPositionChange((PortalObject) w, true);
        if (!isRendering) {
            recalculateLayoutAndPortletSizes();
        }
    };

    @Override
    public boolean remove(Widget w) {
        boolean result = super.remove(w);
        VPortalLayout.this.updateSpacingOnPortletPositionChange((PortalObject) w, false);
        portlets.remove(w);
        if (!isRendering) {
            recalculateLayoutAndPortletSizes();
        }
        return result;
    };

    public void addToRootElement(final PortalObject widget, int position) {
        insert((Widget) widget, contentEl, position);
        /**
         * TODO: remove.
         */
        if (widget instanceof VPortlet) {
            //setLock((VPortlet) widget, false);
        }
    }

    public void onPortletMovedOut(VPortlet portlet) {
        final Paintable child = portlet.getContentAsPaintable();
        if (child != null) {
            portlets.remove(portlet.getContent());
            client.updateVariable(paintableId, PortalConst.PORTLET_REMOVED, child, true);
        }
    }

    public int getPortletCount() {
        return portlets.size();
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
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return portlets.contains(component);
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        recalculateLayoutAndPortletSizes();
        return true;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (child instanceof VPortlet) {
            final VPortlet portlet = (VPortlet) child;
            final Size sizeInfo = portlet.getContentSizeInfo();
            int height = sizeInfo.getHeight();
            if (portlet.isHeightRelative()) {
                height = (int) (((float) height) * 100 / portlet.getRelativeHeightValue());
            }
            return new RenderSpace(sizeInfo.getWidth(), height);
        }
        return null;
    }

    public int getVerticalSpacing() {
        if (isSpacingEnabled) {
            return ComputedStyle.parseInt(spacingRule.getProperty("width"));
        }
        return 0;
    }

    public static int parsePixel(String value) {
        final Integer result = ComputedStyle.parseInt(value);
        return result == null ? 0 : result;
    }

    public void onPortletEntered(PortalDropPositioner dummy, int dummyIndex) {

    }

    @Override
    public void insert(Widget w, int beforeIndex) {
        insert(w, contentEl, beforeIndex);
    }

    @Override
    public void insert(IsWidget w, int beforeIndex) {
        insert(asWidgetOrNull(w), beforeIndex);
    }
}