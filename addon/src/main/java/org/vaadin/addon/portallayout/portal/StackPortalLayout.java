/*
 * Copyright 2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.addon.portallayout.portal;

import com.vaadin.shared.Connector;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout.SpacingHandler;
import org.vaadin.addon.portallayout.gwt.shared.portal.StackPortalLayoutState;
import org.vaadin.addon.portallayout.gwt.shared.portal.rpc.StackPortalRpc;
import org.vaadin.addon.portallayout.portlet.Portlet;

import java.util.LinkedList;

/**
 * Stacks the child components inside of itself wrapping each of them into a {@link Portlet} instance. 
 */
public class StackPortalLayout extends PortalBase implements SpacingHandler {

    public StackPortalLayout() {
        super();
        registerRpc(new StackPortalRpc() {
            
            @Override
            public void removePortlet(Connector portletContent) {
                StackPortalLayout.this.removePortlet((Component) portletContent);
                //fireEvent(new PortletCloseEventGwt(StackPortalLayout.this, getPortlet((Component) portletContent)));
            }

            @Override
            public void insertPortletAt(Connector portlet, int index) {
                if (index >= 0) {
                    addPortletAt((Component) portlet, index);
                }
            }
        });
    }
    
    private void addPortletAt(Component c, int index) {
        Portlet portlet = getOrCreatePortletForComponent(c);
        LinkedList<Connector> portlets = (LinkedList<Connector>) getState().portlets();
        if (portlets.contains(portlet)) {
            portlets.remove(portlet);
        }
        portlets.add(index, portlet);
    }
    
    @Override
    public void removePortlet(Portlet portlet) {
        //portlet.getContent().setWidth(portlet.getPreferredFixedContentWidth());
        super.removePortlet(portlet);
    }

    @Override
    protected StackPortalLayoutState getState() {
        return (StackPortalLayoutState)super.getState();
    }
    
    @Override
    public Class<? extends SharedState> getStateType() {
        return StackPortalLayoutState.class;
    }
    
    @Override
    public void setSpacing(boolean enabled) {
        getState().spacing = enabled;
    }

    @Override
    public boolean isSpacing() {
        return getState().spacing;
    }
}
