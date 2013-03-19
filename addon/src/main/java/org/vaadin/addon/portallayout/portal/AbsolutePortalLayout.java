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
    protected AbsolutePortalState getState() {
        return (AbsolutePortalState)super.getState();
    }
}
