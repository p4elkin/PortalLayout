package org.vaadin.addon.portallayout.client.ui.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.addon.portallayout.client.ui.PortalConst;
import org.vaadin.addon.portallayout.client.ui.PortalDropController;
import org.vaadin.addon.portallayout.client.ui.portlet.AnimationType;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalDropPositioner;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalObject;
import org.vaadin.addon.portallayout.client.ui.portlet.VPortlet;
import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.EventId;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderInformation.Size;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.LayoutClickEventHandler;
import com.vaadin.terminal.gwt.client.ui.VMarginInfo;

@SuppressWarnings("serial")
public class VPortal extends Composite implements Container, ClientSideHandler, HasWidgets, VPortalView.Presenter {

    protected ApplicationConnection client;

    protected String paintableId;

    private VPortalView view;

    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            
            register("setSpacing", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    view.setSpacing((Boolean)params[0]);
                }
            });
            
            register("setCommunicative", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    view.setCommunicative((Boolean)params[0]);
                }
            });
            
            register("setAnimationEnabled", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    view.setAnimationEnabled((AnimationType)AnimationType.valueOf(String.valueOf(params[0])), (Boolean)params[1]);
                }
            });
            
            register("setAnimationSpeed", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    view.setAnimationSpeed((AnimationType)AnimationType.valueOf(String.valueOf(params[0])), (Integer)params[1]);
                }
            });
        }
    };
    
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
    
    public VPortal() {
        super();
        this.view = new VPortalViewImpl();
        view.setDropCotroller(new PortalDropController(this));
        initWidget(view.asWidget());
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        proxy.update(this, uidl, client);
        clickEventHandler.handleEventHandlerRegistration(client);
        updateMarginsFromUidl(uidl);
        int pos = 0;
        final Map<VPortlet, UIDL> realtiveSizePortletUIDLS = new HashMap<VPortlet, UIDL>();
        final List<VPortlet> orphanCandidates = new ArrayList<VPortlet>(view.getPortlets());
        for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext(); ++pos) {
            final UIDL portletUidl = (UIDL) it.next();
            if (!Util.isCached(portletUidl)) {
                final Paintable child = client.getPaintable(portletUidl);
                if (child instanceof VPortlet) {
                    final VPortlet portlet = (VPortlet) child;
                    orphanCandidates.remove(portlet);
                    view.acceptPortlet(portlet, pos);
                    final Size portletSize = portlet.getContentSizeInfo();
                    //portletSize.setWidth(actualSizeInfo.getWidth());
                    portlet.tryDetectRelativeHeight(portletUidl);
                    if (portlet.isHeightRelative()) {
                        realtiveSizePortletUIDLS.put(portlet, portletUidl);
                    } else {
                        portlet.updateFromUIDL(portletUidl, client);
                        if (!portlet.isCollapsed()) {
                            portletSize.setHeight(Util.getRequiredHeight(portlet.getElement()));
                        }
                    }
                    portlet.setPortal(this);
                }   
            }
        }

        //recalculateLayoutAndPortletSizes();

        for (final VPortlet p : realtiveSizePortletUIDLS.keySet()) {
            final UIDL relUidl = realtiveSizePortletUIDLS.get(p);
            p.updateFromUIDL(relUidl, client);
        }


        for (final VPortlet w : orphanCandidates) {
            view.removePortlet(w);
            client.unregisterPaintable((Paintable) w);
        }
    }
    
    public void updatePortletPosition(VPortlet portlet, int newPosition) {
        final Paintable child = portlet.getContentAsPaintable();
        if (child != null) {
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put(PortalConst.PAINTABLE_MAP_PARAM, child);
            params.put(PortalConst.PORTLET_POSITION, newPosition);
            client.updateVariable(paintableId, PortalConst.PORTLET_POSITION_UPDATED, params, true);
            view.acceptPortlet(portlet, newPosition);
            portlet.setPortal(this);
        }
    }
    
    public void updateDummyPosition(PortalDropPositioner dummy, int position) {
        view.acceptPortlet(dummy, position);
    }
    
    private void setLock(final VPortlet portlet, boolean isLocked) {
        view.setPortletLock(portlet, isLocked);
    }
    
    @Override
    public void add(Widget w) {
        view.add(w);
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return view.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return view.remove(w);
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return (component instanceof VPortlet) && (getPortletIndex((VPortlet)component) >= 0);
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        return null;
    }

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    private Paintable getComponent(Element element) {
        return Util.getPaintableForElement(client, this, element);
    }
    
    @Override
    public void handleCallFromServer(String method, Object[] params) {
        throw new RuntimeException("Unknown server call " + method);
    }
    
    private void updateMarginsFromUidl(UIDL uidl) {
        VMarginInfo marginInfo = new VMarginInfo(uidl.getIntAttribute("margins"));
        view.updateMargins(marginInfo);
    }

    public ComplexPanel asPanel() {
        return view.getRootPanel();
    }

    public int getPortletIndex(final PortalObject portlet) {
        return view.getWidgetIndex(portlet);
    }

    public int getPortletCount() {
        return view.getWidgetCount();
    }

    public void onPortletClose(final VPortlet portlet) {
        final Paintable child = portlet.getContentAsPaintable();
        if (child != null) {
            view.removePortlet(portlet);
            client.updateVariable(paintableId, PortalConst.PORTLET_CLOSED, child, true);
        }
    }

    public void onPortletCollapseStateChanged(final VPortlet portlet) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PortalConst.PAINTABLE_MAP_PARAM, portlet.getContentAsPaintable());
        params.put(PortalConst.PORTLET_COLLAPSED, portlet.isCollapsed());
        client.updateVariable(paintableId, PortalConst.PORTLET_COLLAPSE_STATE_CHANGED, params, true);
    }

    public void onPortletMovedOut(VPortlet portlet) {
        
    }

}
