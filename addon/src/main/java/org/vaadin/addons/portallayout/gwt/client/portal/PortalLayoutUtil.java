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
package org.vaadin.addons.portallayout.gwt.client.portal;

import org.vaadin.addons.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addons.portallayout.gwt.client.portlet.PortletChrome;
import org.vaadin.addons.portallayout.gwt.client.portlet.PortletConnector;

import com.vaadin.client.ComponentConnector;
import com.vaadin.client.Util;

/**
 * PortalLayoutUtil.
 */
public class PortalLayoutUtil {
    
    public static PortletConnector getPortletConnectorForContent(ComponentConnector cc) {
        ComponentConnector parent = (ComponentConnector) cc.getParent();
        if (parent instanceof PortalLayoutConnector) {
            PortalLayoutConnector portalConnector = (PortalLayoutConnector) parent;
            return (PortletConnector) portalConnector.getState().contentToPortlet.get(cc);
        }
        return null;
    }
    
    public static PortalLayoutConnector getPortalLayoutConnectorForPortlet(PortletConnector pc) {
        ComponentConnector cc = Util.findConnectorFor(pc.getPortletChrome().getContentWidget());
        return cc.getParent() instanceof PortalLayoutConnector ? (PortalLayoutConnector)cc.getParent() : null;
    }
    
    public static void lockPortlet(PortletConnector portlet) {
        PortalLayoutConnector portal = getPortalLayoutConnectorForPortlet(portlet);
        if (portal != null) {
            PortletChrome portletWidget = portlet.getPortletChrome();
            /**
             * If the component wasn't draggable before - the
             * makeNotDraggable(..) call will result in NPE. So we first make
             * the portlet draggable just in case and then make it not
             * draggable.
             */
            portal.getDragController().makeDraggable(portletWidget, portletWidget.getHeader());
            portal.getDragController().makeNotDraggable(portletWidget);
        }
    }
    
    public static void unlockPortlet(PortletConnector portlet) {
        PortalLayoutConnector portal = getPortalLayoutConnectorForPortlet(portlet);
        if (portal != null) {
            portal.getDragController().makeDraggable(portlet.getPortletChrome(), portlet.getPortletChrome().getHeader());
        }
    }
}
