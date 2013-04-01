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
package org.vaadin.addon.portallayout.gwt.client.portlet.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletChrome;

/**
 * PortletCollapseEventGwt.
 */
public class PortletCollapseEventGwt extends GwtEvent<PortletCollapseEventGwt.Handler>{
    
    public static final Type<Handler> TYPE = new Type<Handler>();
    
    public interface Handler extends EventHandler {
        void onPortletCollapse(PortletCollapseEventGwt e);
    }
    
    private final PortletChrome portlet;
    
    public PortletCollapseEventGwt(PortletChrome portletWidget) {
        this.portlet = portletWidget;
    }
    
    public PortletChrome getPortlet() {
        return portlet;
    }
    
    public interface HasPortletCollapseEventHandlers { 
        HandlerRegistration addPortletCollapseEventHandler(Handler handler);
    }
    
    @Override
    public GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onPortletCollapse(this);
    }
}
