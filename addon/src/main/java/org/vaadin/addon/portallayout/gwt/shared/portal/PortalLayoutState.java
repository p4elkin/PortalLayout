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
package org.vaadin.addon.portallayout.gwt.shared.portal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.AbstractLayoutState;

/**
 * PortalLayoutState.
 */
public abstract class PortalLayoutState extends AbstractLayoutState {

    public boolean spacing = false;
    
    public int marginsBitmask = 0;
    
    public Map<Connector, Connector> contentToPortlet = new HashMap<Connector, Connector>();
    
    public abstract Collection<Connector> portlets();
}
