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
package org.vaadin.addon.portallayout.gwt.client.portal.connection;

import org.vaadin.addon.portallayout.gwt.client.dnd.AbsolutePortalDropController;
import org.vaadin.addon.portallayout.gwt.client.portal.AbsolutePortalViewImpl;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalView;
import org.vaadin.addon.portallayout.gwt.shared.portal.AbsolutePortalState;
import org.vaadin.addon.portallayout.portal.AbsolutePortal;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.vaadin.shared.ui.Connect;

/**
 * AbsolutePortalConnector.
 */
@Connect(AbsolutePortal.class)
public class AbsolutePortalConnector extends PortalLayoutConnector {

    @Override
    public AbsolutePortalState getState() {
        return (AbsolutePortalState)super.getState();
    }
 
    @Override
    protected PortalView initView() {
        return new AbsolutePortalViewImpl();
    }
    
    @Override
    protected DropController initDropController() {
        return new AbsolutePortalDropController(this);
    }
}
