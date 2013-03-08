package org.vaadin.addon.portallayout.gwt.client.portlet;

import org.vaadin.addon.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCloseEvent;
import org.vaadin.addon.portallayout.gwt.client.portlet.event.PortletCollapseEvent;
import org.vaadin.addon.portallayout.gwt.shared.portlet.PortletState;
import org.vaadin.addon.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;
import org.vaadin.addon.portallayout.portlet.Portlet;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.ComponentConstants;
import com.vaadin.shared.ui.Connect;

@Connect(Portlet.class)
public class PortletConnector extends AbstractComponentContainerConnector implements
        PortletCollapseEvent.Handler, PortletCloseEvent.Handler {

    private final PortletServerRpc rpc = RpcProxy.create(PortletServerRpc.class, this);
    
    private int headerHeight = 0;
    
    public PortletSlot getSlot() {
        return getWidget().getSlot();
    }

    @Override
    protected void init() {
        super.init();
        getLayoutManager().addElementResizeListener(getWidget().getHeader().getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                headerHeight = e.getLayoutManager().getOuterHeight(e.getElement());
            }
        });
        //getWidget().setConnector(this);
        getWidget().getHeader().addPortletCollapseEventHandler(this);
        getWidget().getHeader().addPortletCloseEventHandler(this);
        addStateChangeHandler("isCollapsed", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                if (getState().isCollapsed) {
                    getSlot().setHeight(headerHeight + "px");   
                }
            }
        });
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        getSlot().setWidth(getState().getFormattedWidth());
        getSlot().setHeight(getState().getFormattedHeight());
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        final String caption = connector.getState().caption;
        doUpdateCaption(caption);
    }

    public void updateOwnCaption() {
        if (getContent() == null || getContent().getState().caption == null) {
            doUpdateCaption(getState().caption);   
        }
    }

    private ComponentConnector getContent() {
        return getState().content == null ? null : (ComponentConnector)getState().content;
    }

    private Widget getContentWidget() { 
        ComponentConnector cc = getContent();
        return cc == null ? null : cc.getWidget();
    }
    
    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        if (getContentWidget() != null) {
            getWidget().add(getContentWidget());
            getContent().addStateChangeHandler("caption", new StateChangeHandler() { 
                @Override
                public void onStateChanged(StateChangeEvent event) {
                    rpc.updateCaptionFromContent(((AbstractComponentState)event.getConnector().getState()).caption);
                }
            });
            
            if (getHeaderToolbar() != null) {
                getWidget().setHeaderToolbar(getHeaderToolbar().getWidget());
            }
        }
    }

    private ComponentConnector getHeaderToolbar() {
        return getState().headerToolbar == null ? null : (ComponentConnector)getState().headerToolbar;
    }

    @Override
    public PortletState getState() {
        return (PortletState) super.getState();
    }

    @Override
    public PortletWidget getWidget() {
        return (PortletWidget) super.getWidget();
    }

    public void doUpdateCaption(String caption) {
        getWidget().getHeader().setCaptionText(caption);
        getWidget().getHeader().setIcon(getResourceUrl(ComponentConstants.ICON_RESOURCE));
    }

    @Override
    public void onPortletClose(PortletCloseEvent e) {
        ((PortalLayoutConnector)getParent()).removePortlet(this);
    }

    @Override
    public void onPortletCollapse(PortletCollapseEvent e) {
        rpc.setCollapsed(!getState().isCollapsed);
    }

    public boolean isSlotRelativeHeight() {
        return getState().slotSize.heightUnit.endsWith("%") && 
               getState().slotSize.height >= 0;
    }

    public double getHeightValue() {
        return getState().isCollapsed ? 0 : getState().slotSize.height;
    }

    public void setSlotHeight(String height) {
        getSlot().setHeight(height);
    }

    public double getHeaderHeight() {
        return headerHeight;
    }

    public boolean isCollased() {
        return getState().isCollapsed;
    }
}
