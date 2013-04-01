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
package org.vaadin.addon.portallayout.gwt.client.portal.strategy;

import com.vaadin.client.ComponentConnector;
import com.vaadin.client.Profiler;
import com.vaadin.client.Util;
import org.vaadin.addon.portallayout.gwt.client.portal.PortalLayoutUtil;
import org.vaadin.addon.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletSlot;

/**
 * AbsolutePortalHeightRedistributionStrategy.
 */
public class AbsolutePortalHeightRedistributionStrategy implements PortalHeightRedistributionStrategy {

    @Override
    public void redistributeHeights(PortalLayoutConnector portalConnector) {
        Profiler.enter("PLC.recalcHeight");
        for (ComponentConnector cc : portalConnector.getCurrentChildren()) {
            PortletConnector portletConnector = PortalLayoutUtil.getPortletConnectorForContent(cc);
            PortletSlot slot = portletConnector.getPortletChrome().getAssociatedSlot();
            if (portletConnector.hasRelativeHeight()) {
                if (!portletConnector.isCollapsed()) {
                    float relativeHeight = Util.parseRelativeSize(portletConnector.getState().height);
                    slot.setHeight(relativeHeight + "%");
                }
            }
        }
        Profiler.leave("PLC.recalcHeight");
    }
}
