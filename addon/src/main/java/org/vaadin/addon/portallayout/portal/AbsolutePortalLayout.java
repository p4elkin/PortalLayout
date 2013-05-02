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

import org.vaadin.addon.portallayout.gwt.shared.portal.AbsolutePortalState;
import org.vaadin.addon.portallayout.gwt.shared.portal.rpc.AbsolutePortalServerRpc;
import org.vaadin.addon.portallayout.portlet.AbsolutePortletExtension;
import org.vaadin.addon.portallayout.portlet.Portlet;

import com.vaadin.server.Extension;
import com.vaadin.shared.Connector;
import com.vaadin.ui.Component;


/**
 * AbsolutePortal.
 */
public class AbsolutePortalLayout extends PortalBase {
    
    public AbsolutePortalLayout() {
        registerRpc(new AbsolutePortalServerRpc() {
            @Override
            public void removePortlet(Connector portletContent) {
                AbsolutePortalLayout.this.removePortlet((Component)portletContent);
            }
            
            @Override
            public void addPortlet(Connector cc) {
                getOrCreatePortletForComponent((Component)cc);
            }
        });
    }
    
    @Override
    public void removePortlet(Portlet portlet) {
        AbsolutePortletExtension ex = getAbsoluteExtension(portlet);
        if (ex != null) {
            ex.remove();
        }
        super.removePortlet(portlet);
    }
    
    @Override
    protected Portlet getOrCreatePortletForComponent(Component c) {
        Portlet p = super.getOrCreatePortletForComponent(c);
        ensureAbsoluteExtension(p);
        return p;
    }
    
    private void ensureAbsoluteExtension(Portlet p) {
        AbsolutePortletExtension ex = getAbsoluteExtension(p);
        if (ex == null) {
            ex = new AbsolutePortletExtension(p);
        }
    }
    
    private AbsolutePortletExtension getAbsoluteExtension(Portlet p) {
        for (Extension ex : p.getExtensions()) {
            if (ex instanceof AbsolutePortletExtension) {
                return (AbsolutePortletExtension)ex;
            }
        }
        return null;
    }
    
    @Override
    protected AbsolutePortalState getState() {
        return (AbsolutePortalState)super.getState();
    }
}
