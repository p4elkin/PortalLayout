package org.vaadin.addon.portallayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.addon.portallayout.client.ui.PortalConst;
import org.vaadin.addon.portallayout.client.ui.VPortalLayout;
import org.vaadin.addon.portallayout.client.ui.portlet.AnimationType;
import org.vaadin.addon.portallayout.event.Context;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickNotifier;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.client.EventId;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout.SpacingHandler;

/**
 * Layout that presents its contents in a portal style.
 * 
 * @author p4elkin
 */
@SuppressWarnings("serial")
@ClientWidget(value = VPortalLayout.class, loadStyle = LoadStyle.EAGER)
public class PortalLayout extends AbstractLayout implements SpacingHandler, LayoutClickNotifier {

    public static class PortletClosedEvent extends Component.Event {

        public static final java.lang.reflect.Method PORTLET_CLOSED;

        static {
            try {
                PORTLET_CLOSED = PortletCloseListener.class.getDeclaredMethod("portletClosed",
                        new Class[] { PortletClosedEvent.class });
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        private final Context context;

        public PortletClosedEvent(Component source, final Context context) {
            super(source);
            this.context = context;
        }

        public Context getContext() {
            return context;
        }
    }

    public interface PortletCloseListener {
        public void portletClosed(final PortletClosedEvent event);
    }

    public static class PortletCollapseEvent extends Component.Event {

        public static final java.lang.reflect.Method PORTLET_COLLAPSE_STATE_CHANGED;

        static {
            try {
                PORTLET_COLLAPSE_STATE_CHANGED = PortletCollapseListener.class.getDeclaredMethod(
                        "portletCollapseStateChanged", new Class[] { PortletCollapseEvent.class });
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        private final Context context;

        public PortletCollapseEvent(Component source, final Context context) {
            super(source);
            this.context = context;
        }

        public Context getContext() {
            return context;
        }
    }

    public interface PortletCollapseListener {
        void portletCollapseStateChanged(final PortletCollapseEvent context);
    }

    private static final String CLICK_EVENT = EventId.LAYOUT_CLICK;

    private final Map<AnimationType, Boolean> animationModeMap = new EnumMap<AnimationType, Boolean>(
            AnimationType.class);

    private final Map<AnimationType, Integer> animationSpeedMap = new EnumMap<AnimationType, Integer>(
            AnimationType.class);

    private final List<Component> components = new ArrayList<Component>();

    private final Map<Component, ComponentDetails> componentToDetails = new HashMap<Component, ComponentDetails>();

    /**
     * Flag indicating whether this portal can accept portlets from other
     * portals and its portlets can be dragged to the other Portals.
     */
    private boolean isCommunicative = true;

    /**
     * The flag indicating that spacing is enabled.
     */
    private boolean isSpacingEnabled = true;

    /**
     * Constructor
     */
    public PortalLayout() {
        super();
        setSizeFull();
    }

    /**
     * Add an action to the components' portlet.
     * 
     * @param c
     *            Component.
     * @param action
     *            Action to be performed.
     * @return New action id. Action can be removed using this id.
     */
    public String addAction(final Component c, final ToolbarAction action) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Component does not belong to this portal!");
        }
        return details.addAction(action);
    }

    public void addCloseListener(final PortletCloseListener listener) {
        addListener(VPortalLayout.PORTLET_CLOSED_EVENT_ID, PortletClosedEvent.class, listener,
                PortletClosedEvent.PORTLET_CLOSED);
    }

    public void addCollapseListener(final PortletCollapseListener listener) {
        addListener(VPortalLayout.PORTLET_COLLAPSE_EVENT_ID, PortletCollapseEvent.class, listener,
                PortletCollapseEvent.PORTLET_COLLAPSE_STATE_CHANGED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.ui.AbstractComponentContainer#addComponent(com.vaadin.ui.Component
     * )
     */
    @Override
    public void addComponent(Component c) {
        addComponent(c, components.size());
    }

    public void addComponent(Component c, int position) {
        doAddComponent(c, position);
        requestRepaint();
    }

    @Override
    public void addListener(LayoutClickListener listener) {
        addListener(CLICK_EVENT, LayoutClickEvent.class, listener, LayoutClickListener.clickMethod);
    }

    public void addPortletStyleName(final Component c, final String style) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details != null && details.getStyles().indexOf(style) < 0) {
            details.addStyle(style);
            requestRepaint();
        }
    }

    public void removePortletStyleName(final Component c, final String style) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details != null) {
            if (details.getStyles().indexOf(style) != -1) {
                requestRepaint();
            }
            details.removeStyle(style);
        }
    }

    public void clearPortletStyleNames(final Component c) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details != null) {
            details.setStyles(new ArrayList<String>());
            requestRepaint();
        }
    }

    /**
     * Receive and handle events and other variable changes from the client.
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {

        super.changeVariables(source, variables);

        if (variables.containsKey(PortalConst.PORTLET_ACTION_TRIGGERED)) {
            final Map<String, Object> portletParameters = (Map<String, Object>) variables
                    .get(PortalConst.PORTLET_ACTION_TRIGGERED);
            final Component component = (Component) portletParameters.get(PortalConst.PAINTABLE_MAP_PARAM);
            final String actionId = (String) portletParameters.get(PortalConst.PORTLET_ACTION_ID);
            onActionTriggered(component, actionId);
        }

        if (variables.containsKey(PortalConst.PORTLET_POSITION_UPDATED)) {
            final Map<String, Object> portletParameters = (Map<String, Object>) variables
                    .get(PortalConst.PORTLET_POSITION_UPDATED);
            final Component component = (Component) portletParameters.get(PortalConst.PAINTABLE_MAP_PARAM);
            final Integer portletPosition = (Integer) portletParameters.get(PortalConst.PORTLET_POSITION);
            onComponentPositionUpdated(component, portletPosition);
        }

        if (variables.containsKey(PortalConst.PORTLET_COLLAPSE_STATE_CHANGED)) {
            final Map<String, Object> params = (Map<String, Object>) variables
                    .get(PortalConst.PORTLET_COLLAPSE_STATE_CHANGED);

            onPortletCollapsed((Component) params.get(PortalConst.PAINTABLE_MAP_PARAM),
                    (Boolean) params.get(PortalConst.PORTLET_COLLAPSED));
        }

        if (variables.containsKey(PortalConst.PORTLET_REMOVED)) {
            final Component child = (Component) variables.get(PortalConst.PORTLET_REMOVED);
            doComponentRemoveLogic(child);
        }

        if (variables.containsKey(PortalConst.PORTLET_CLOSED)) {
            final Component child = (Component) variables.get(PortalConst.PORTLET_CLOSED);
            fireCloseEvent(child);
            removeComponent(child);
        }
    }

    @Override
    public void detach() {
        for (final ComponentDetails details : componentToDetails.values()) {
            if (details.getHeaderComponent() != null) {
                details.getHeaderComponent().detach();
            }
        }
        super.detach();
    }

    private void doAddComponent(final Component c, int position) {
        final int index = components.indexOf(c);

        if (index != -1) {
            throw new IllegalArgumentException("Component has already been added to the portal!");
        }

        c.setWidth("100%");
        final ComponentDetails details = c.getParent() instanceof PortalLayout ? ((PortalLayout) c.getParent())
                .getDetails(c) : new ComponentDetails();
        componentToDetails.put(c, details);
        if (position == components.size()) {
            components.add(c);
        } else {
            components.add(position, c);
        }
        if (details.getHeaderComponent() != null) {
            details.getHeaderComponent().setParent(c);
            details.getHeaderComponent().attach();
        }
        super.addComponent(c);
    }

    private void doComponentRemoveLogic(final Component c) {
        componentToDetails.remove(c);
        components.remove(c);
    }

    private void fireCloseEvent(final Component c) {
        fireEvent(new PortletClosedEvent(this, new Context(this, c)));
    }

    private void fireCollapseEvent(final Component c) {
        fireEvent(new PortletCollapseEvent(this, new Context(this, c)));
    }

    public int getAnimationSpeed(final AnimationType animationType) {
        final Integer speed = animationSpeedMap.get(animationType);
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

    /**
     * Get caption of the portlet containing this component.
     * 
     * @deprecated
     */
    @Deprecated
    public String getComponentCaption(Component c) {
        return c.getCaption();
    }

    @Override
    public Iterator<Component> getComponentIterator() {
        return Collections.unmodifiableCollection(components).iterator();
    }

    private ComponentDetails getDetails(final Component c) {
        return componentToDetails.get(c);
    }

    /**
     * Check if the portlet containing this component can be closed.
     * 
     * @param c
     *            Component
     * @return true if portlet can be closed.
     */
    public boolean isClosable(Component c) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        return details.isClosable();
    }

    /**
     * Check if portlet containing this component is collapsed.
     * 
     * @param c
     *            Component
     * @return true if the portlet is collapsed
     */
    public boolean isCollapsed(Component c) {
        final ComponentDetails details = componentToDetails.get(c);

        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }

        return details.isCollapsed();
    }

    /**
     * Check if the portlet containing this component can be collapsed.
     * 
     * @param c
     *            Component
     * @return true if can be collpsed
     */
    public boolean isCollapsible(Component c) {
        final ComponentDetails details = componentToDetails.get(c);

        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }

        return details.isCollapsible();
    }

    /**
     * Check if portal accepts portlets from other portals and its portlets can
     * be dragged to other portals.
     * 
     * @return true if can share portlets.
     */
    public boolean isCommunicative() {
        return isCommunicative;
    }

    /**
     * Check if the portlet containing this component is locked (cannot be
     * dragged).
     * 
     * @param c
     *            Component
     * @return true if portlet is locked.
     */
    public boolean isLocked(Component c) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        return details.isLocked();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.SpacingHandler#isSpacing()
     */
    @Override
    public boolean isSpacing() {
        return isSpacingEnabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.SpacingHandler#isSpacingEnabled()
     */
    @Override
    public boolean isSpacingEnabled() {
        return isSpacingEnabled;
    }

    private void onActionTriggered(final Component component, final String actionId) {
        final ComponentDetails details = componentToDetails.get(component);
        if (details == null) {
            throw new IllegalArgumentException("Wrong Component! Action Trigger Failed!");
        }
        final ToolbarAction action = details.getActionById(actionId);
        action.execute(new Context(this, component));
    }

    /**
     * Handler that should be invoked when the components position in the portal
     * was changed.
     * 
     * @param component
     *            Component whose position was updated
     * @param newPosition
     *            New position of the component.
     */
    private void onComponentPositionUpdated(final Component component, int newPosition) {

        // The client side reported that portlet is no longer there - remove
        // component if so.
        if (newPosition == -1) {
            removeComponent(component);
            return;
        }

        final int oldPosition = components.indexOf(component);
        if (oldPosition == -1) {
            addComponent(component, newPosition);
            return;
        }

        // Component is in the right position - nothing to do.
        if (newPosition == oldPosition) {
            return;
        }

        components.remove(component);
        components.add(newPosition, component);
    }

    /**
     * Handler that should be invoked when the components collapse state
     * changes.
     * 
     * @param component
     *            Component which collapse state has changed.
     * @param isCollapsed
     *            True if the portlet was collapsed, false - expanded.
     */
    private void onPortletCollapsed(final Component component, Boolean isCollapsed) {
        final ComponentDetails details = componentToDetails.get(component);

        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }

        details.setCollapsed(isCollapsed);
        fireCollapseEvent(component);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("spacing", isSpacingEnabled);
        target.addAttribute(PortalConst.PORTAL_COMMUNICATIVE, isCommunicative);
        for (final AnimationType at : Arrays.asList(AnimationType.values())) {
            target.addAttribute(at.toString(), shouldAnimate(at));
            target.addAttribute(at.toString() + "-SPEED", getAnimationSpeed(at));
        }
        final Iterator<Component> it = components.iterator();
        while (it.hasNext()) {
            final Component childComponent = it.next();
            final ComponentDetails childComponentDetails = componentToDetails.get(childComponent);

            target.startTag("portlet");
            target.startTag("body");
            target.addAttribute(PortalConst.PORTLET_CLOSABLE, childComponentDetails.isClosable());
            target.addAttribute(PortalConst.PORTLET_LOCKED, childComponentDetails.isLocked());
            target.addAttribute(PortalConst.PORTLET_COLLAPSED, childComponentDetails.isCollapsed());
            target.addAttribute(PortalConst.PORTLET_COLLAPSIBLE, childComponentDetails.isCollapsible());
            target.addAttribute("styles", childComponentDetails.getStyles().toArray());
            final Map<String, ToolbarAction> actions = childComponentDetails.getActions();
            if (actions != null && actions.entrySet().size() > 0) {
                final Iterator<?> actionIt = actions.entrySet().iterator();
                final String[] ids = new String[actions.entrySet().size()];
                final String[] iconUrls = new String[actions.entrySet().size()];
                int pos = 0;
                while (actionIt.hasNext()) {
                    final Map.Entry<?, ?> entry = (Entry<?, ?>) actionIt.next();
                    final String id = (String) entry.getKey();
                    final ThemeResource r = ((ToolbarAction) entry.getValue()).getIcon();
                    final String icon = "theme://" + r.getResourceId();
                    ids[pos] = id;
                    iconUrls[pos++] = icon;
                }
                target.addAttribute(PortalConst.PORTLET_ACTION_IDS, ids);
                target.addAttribute(PortalConst.PORTLET_ACTION_ICONS, iconUrls);
            }
            childComponent.paint(target);
            target.endTag("body");
            final Component headerComponent = childComponentDetails.getHeaderComponent();
            if (headerComponent != null) {
                target.startTag("header");
                headerComponent.addStyleName("v-portlet-header-widget");
                headerComponent.paint(target);
                target.endTag("header");
            }
            target.endTag("portlet");
        }
    }

    /**
     * Remove action by its ID.
     * 
     * @param c
     *            Component.
     * @param actionId
     *            ID of action to be removed.
     */
    public void removeAction(final Component c, String actionId) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Component does not belong to this portal!");
        }
        details.removeAction(actionId);
    }

    public void removeCloseListener(final PortletCloseListener listener) {
        removeListener(VPortalLayout.PORTLET_CLOSED_EVENT_ID, PortletClosedEvent.class, listener);
    }

    public void removeCollapseListener(final PortletCollapseListener listener) {
        removeListener(VPortalLayout.PORTLET_COLLAPSE_EVENT_ID, PortletCollapseEvent.class, listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.ui.AbstractComponentContainer#removeComponent(com.vaadin.ui
     * .Component)
     */
    @Override
    public void removeComponent(Component c) {
        doComponentRemoveLogic(c);
        super.removeComponent(c);
    }

    @Override
    public void removeListener(LayoutClickListener listener) {
        removeListener(CLICK_EVENT, LayoutClickEvent.class, listener);
    }

    @Override
    public void replaceComponent(final Component oldComponent, final Component newComponent) {
        final int position = components.indexOf(oldComponent);
        if (position < 0) {
            throw new IllegalArgumentException("Portal does not contain the portlet. Replacement failed.");
        }
        componentToDetails.put(newComponent, componentToDetails.get(oldComponent));
        removeComponent(oldComponent);
        doAddComponent(newComponent, position);
    }

    public void setAnimationMode(final AnimationType animationType, boolean animate) {
        animationModeMap.put(animationType, animate);
        requestRepaint();
    }

    public void setAnimationSpeed(final AnimationType animationType, int speed) {
        animationSpeedMap.put(animationType, speed);
        requestRepaint();
    }

    /**
     * Make portlet closable or not closable.
     * 
     * @param c
     *            Component.
     * @param closable
     *            true if portlet can be closed.
     */
    public void setClosable(final Component c, boolean closable) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        if (details.isClosable() != closable) {
            details.setClosable(closable);
            requestRepaint();
        }
    }

    /**
     * Set collapse state of the portlet.
     * 
     * @param c
     *            Component.
     * @param isCollapsed
     *            true if portlet is collapsed.
     */
    public void setCollapsed(final Component c, boolean isCollapsed) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        if (isCollapsed != details.isCollapsed()) {
            details.setCollapsed(isCollapsed);
            requestRepaint();
        }
    }

    /**
     * 
     * @param c
     * @param isCollapsible
     */
    public void setCollapsible(final Component c, boolean isCollapsible) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        if (isCollapsible != details.isCollapsible()) {
            details.setCollapsible(isCollapsible);
            requestRepaint();
        }
    }

    /**
     * Set if portal accepts portlets from other portals and its portlets can be
     * dragged to other portals.
     * 
     * @param isCommunicative
     *            true if portlets can be dragged to other portals.
     */
    public void setCommunicative(boolean isCommunicative) {
        this.isCommunicative = isCommunicative;
    }

    /**
     * Set caption of the portlet containing this component.
     * 
     * @deprecated use components setCation method instead
     */
    @Deprecated
    public void setComponentCaption(final Component c, final String caption) {
        c.setCaption(caption);
    }

    /**
     * Set lock state of the portlet containing this component.
     * 
     * @param c
     *            Component.
     * @param isLocked
     *            true if locked.
     */
    public void setLocked(final Component c, boolean isLocked) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        if (isLocked != details.isLocked()) {
            details.setLocked(isLocked);
            requestRepaint();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.SpacingHandler#setSpacing(boolean)
     */
    @Override
    public void setSpacing(boolean enabled) {
        isSpacingEnabled = enabled;
        requestRepaint();
    }

    public boolean shouldAnimate(final AnimationType animationType) {
        final Boolean result = animationModeMap.get(animationType);
        return result == null || result;
    }

    public void setHeaderComponent(Component child, Component headerComponent) {
        final ComponentDetails details = componentToDetails.get(child);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        details.setHeaderComponent(headerComponent);
        headerComponent.setParent(child);
        headerComponent.attach();
    }

    public Component getHeaderComponent(Component c) {
        final ComponentDetails details = componentToDetails.get(c);
        if (details == null) {
            throw new IllegalArgumentException("Portal doesn not contain this component!");
        }
        return details.getHeaderComponent();
    }
}
