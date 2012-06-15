package org.vaadin.addon.portallayout.client.ui.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.addon.portallayout.client.dnd.PickupDragController;
import org.vaadin.addon.portallayout.client.dnd.util.DOMUtil;
import org.vaadin.addon.portallayout.client.ui.PortalConst;
import org.vaadin.addon.portallayout.client.ui.PortalDropController;
import org.vaadin.addon.portallayout.client.ui.portlet.AnimationType;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalDropPositioner;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalObject;
import org.vaadin.addon.portallayout.client.ui.portlet.VPortlet;
import org.vaadin.addon.portallayout.client.ui.portlet.VPortlet.PortletLockState;
import org.vaadin.csstools.client.CSSRule;
import org.vaadin.csstools.client.ComputedStyle;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.StyleConstants;
import com.vaadin.terminal.gwt.client.ui.VMarginInfo;


public class VPortalViewImpl extends ComplexPanel implements VPortalView {
    
    private final static PickupDragController commonDragController = new PickupDragController(RootPanel.get(), false);

    private final Map<AnimationType, Boolean> animationModeMap = new EnumMap<AnimationType, Boolean>(AnimationType.class);

    private final Map<AnimationType, Integer> animationSpeedMap = new EnumMap<AnimationType, Integer>(AnimationType.class);
    
    private final List<VPortlet> portlets = new ArrayList<VPortlet>();
    
    private PickupDragController localDragController = null;

    private PortalDropController dropController;
    
    private CSSRule spacingRule = null;
    
    private final Element marginWrapper = DOM.createDiv();

    private final Element contentEl = DOM.createDiv();
    
    private boolean isRendering = false;
    
    private boolean isSpacingEnabled = false;

    private boolean isCommunicative = true;
    
    private float sumRelativeHeight = 0f;

    private int consumedHeight;
    
    public PickupDragController getDragController() {
        if (isCommunicative) {
            return commonDragController;   
        }
        if (localDragController == null) {
            localDragController = new PickupDragController(RootPanel.get(), false);   
        }
        return localDragController;
    }
    
    public VPortalViewImpl() {
        super();
        setElement(marginWrapper);
        marginWrapper.appendChild(contentEl);
        setStyleName(PortalConst.CLASSNAME);
    }
    
    @Override
    public void setDropCotroller(PortalDropController controller) {
        this.dropController = controller;
        getDragController().registerDropController(dropController);
    }
    
    private void recalculateLayoutAndPortletSizes() {
        recalculateLayout();
        final Set<PortalObject> objSet = new HashSet<PortalObject>(portlets);
        final PortalDropPositioner p = dropController.getDummy();
        if (p != null) {
            objSet.add(p);
        }
        calculatePortletSizes(objSet);
    }
    
    public void recalculateLayout() {
        consumedHeight = 0;
        sumRelativeHeight = 0;
        //int newHeight = 0;
        int contentsSize = getWidgetCount();
        final Iterator<Widget> it = iterator();
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
        /*if (sizeInfoFromUidl != null && consumedHeight < sizeInfoFromUidl.getHeight())
            newHeight = sizeInfoFromUidl.getHeight();
        else {
            newHeight = Math.max(actualSizeInfo.getHeight(), consumedHeight);   
        }
        setContainerHeight(newHeight);*/
        //return newHeight;
    }

    public int getRelativePortletHeight(final PortalObject portalObject) {
        int headerHeight = portalObject.getPortletRef().getHeaderHeight();
        float newRealtiveHeight = normalizedRealtiveRatio() * portalObject.getRelativeHeightValue();
        return Math.max((int) (getResidualHeight() * newRealtiveHeight / 100), headerHeight);
    }
    
    private float normalizedRealtiveRatio() {
        float result = 0;
        if (sumRelativeHeight != 0f)
            result = (sumRelativeHeight <= 100) ? 1 : 100 / sumRelativeHeight;
        return result;
    }
    
    public int getResidualHeight() {
        return DOMUtil.getClientHeight(marginWrapper) - consumedHeight - getVerticalMargins();
    }
    
    private int getVerticalMargins() {
        return DOMUtil.getVerticalMargin(marginWrapper);
    }

    private int getHorizontalMargins() {
        return DOMUtil.getHorizontalMargin(marginWrapper);
    }
    
    public void calculatePortletSizes(final Set<PortalObject> objSet) {
        int width = DOMUtil.getClientWidth(marginWrapper);
        final Iterator<PortalObject> it = objSet.iterator();
        while (it.hasNext()) {
            final PortalObject portalObject = (PortalObject) it.next();
            int height = portalObject.isHeightRelative() ? getRelativePortletHeight(portalObject) : portalObject.getContentHeight();
            //portalObject.setWidgetSizes(width, height);
        }
        /*if (client != null)
            client.runDescendentsLayout(this);*/
    }

    @Override
    public List<VPortlet> getPortlets() {
        return Collections.unmodifiableList(portlets);
    }

    @Override
    public void setSpacing(boolean hasSpacing) {
        isSpacingEnabled = hasSpacing;
    }

    @Override
    public void setCommunicative(boolean canCommunicate) {
        final PickupDragController currentController = getDragController();
        if (canCommunicate != isCommunicative) {
            currentController.unregisterDropController(dropController);
            isCommunicative = canCommunicate;
            final PickupDragController newController = getDragController();
            newController.registerDropController(dropController);
            for (final VPortlet portlet : portlets)
                if (!portlet.isLocked()) {
                    currentController.makeNotDraggable(portlet);
                    newController.makeDraggable(portlet, portlet.getDraggableArea());
                }
        }
    }

    @Override
    public void setAnimationEnabled(AnimationType type, boolean isEnabled) {
        animationModeMap.put(type, isEnabled);
    }

    @Override
    public void setAnimationSpeed(AnimationType type, int speed) {
        animationSpeedMap.put(type, speed);
    }
    
    @Override
    public boolean isAnimationEnabled(final AnimationType type) {
        Boolean result = animationModeMap.get(type);
        return result == null || result;
    }
    
    @Override
    public int getAnimationSpeed(final AnimationType animationType) {
        return animationSpeedMap.get(animationType) == null ? PortalConst.DEFAULT_SPEED : animationSpeedMap.get(animationType);
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        spacingRule = new CSSRule("." + PortalConst.STYLENAME_SPACING, false);
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        final PickupDragController dragController = getDragController();
        for (final VPortlet p : portlets) {
            dragController.makeNotDraggable(p);
        }
        dragController.unregisterDropController(dropController);
    }
    
    @Override
    public void insert(Widget w, Element el, int beforeIndex) {
        super.insert(w, el, beforeIndex, true);
        //VPortalLayout.this.updateSpacingOnPortletPositionChange((PortalObject) w, true);
        if (!isRendering) {
            recalculateLayoutAndPortletSizes();
        }
    };

    @Override
    public boolean remove(Widget w) {
        boolean result = super.remove(w);
        //VPortalLayout.this.updateSpacingOnPortletPositionChange((PortalObject) w, false);
        portlets.remove(w);
        if (!isRendering) {
            recalculateLayoutAndPortletSizes();
        }
        return result;
    };
    
    @Override
    public void insert(Widget w, int beforeIndex) {
        insert(w, contentEl, beforeIndex);
    }

    @Override
    public void insert(IsWidget w, int beforeIndex) {
        insert(asWidgetOrNull(w), beforeIndex);
    }
    
    public int getVerticalSpacing() {
        if (isSpacingEnabled) {
            return ComputedStyle.parseInt(spacingRule.getProperty("width"));
        }
        return 0;
    }

    @Override
    public void updateMargins(VMarginInfo marginInfo) {
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_TOP, marginInfo.hasTop());
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_RIGHT, marginInfo.hasRight());
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_BOTTOM, marginInfo.hasBottom());
        setStyleName(marginWrapper, PortalConst.CLASSNAME + "-" + StyleConstants.MARGIN_LEFT, marginInfo.hasLeft());
    }

    @Override
    public void setPortletLock(VPortlet portlet, boolean isLocked) {
        PortletLockState formerLockState = portlet.getLockState();
        portlet.setLocked(isLocked);
        if (!isLocked && formerLockState != PortletLockState.PLS_NOT_LOCKED) {
            getDragController().makeDraggable(portlet, portlet.getDraggableArea());   
        } else if (isLocked && formerLockState == PortletLockState.PLS_NOT_LOCKED) {
            getDragController().makeNotDraggable(portlet);   
        }
    }

    @Override
    public ComplexPanel getRootPanel() {
        return this;
    }

    @Override
    public void acceptPortlet(PortalObject portlet, int pos) {
        int currentPosition = getWidgetIndex(portlet);
        if (pos != currentPosition) {
            portlet.asWidget().removeFromParent();
            insert(portlet.asWidget(), pos);
            if (portlet instanceof VPortlet) {
                setPortletLock((VPortlet) portlet, false);
                portlets.add((VPortlet)portlet);
            }
            
        }
    }

    @Override
    public void removePortlet(final VPortlet portlet) {
        remove(portlet);
        portlets.remove(portlet);
    }

}
