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
package org.vaadin.addon.portallayout.gwt.client.portal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vaadin.addon.portallayout.gwt.client.portal.connection.PortalLayoutConnector;
import org.vaadin.addon.portallayout.gwt.client.portlet.PortletConnector;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.Profiler;
import com.vaadin.client.Util;
import com.vaadin.shared.ui.ComponentStateUtil;

/**
 * StackHeightRedistributionStrategy.
 */
public class StackHeightRedistributionStrategy implements PortalHeightRedistributionStrategy {

    @Override
    public void redistributeHeights(PortalLayoutConnector portalConnector) {
        Profiler.enter("PLC.recalcHeight");
        Iterator<ComponentConnector> it = portalConnector.getCurrentChildren().iterator();
        List<ComponentConnector> relativeHeightPortlets = new ArrayList<ComponentConnector>();
        double totalPercentage = 0;
        int totalFixedHeightConsumption = 0;
        while (it.hasNext()) {
            ComponentConnector cc = it.next();
            if (ComponentStateUtil.isRelativeHeight(cc.getState())) {
                totalPercentage += Util.parseRelativeSize(cc.getState().height);
                relativeHeightPortlets.add(cc);
            } else {
                Widget portletWidget = PortalLayoutUtil.getPortletConnectorForContent(cc).getWidget();
                totalFixedHeightConsumption += cc.getLayoutManager().getOuterHeight(portletWidget.getElement());
            }
        }
        if (totalPercentage > 0) {
            totalPercentage = Math.max(totalPercentage, 100);
            int totalPortalHeight = portalConnector.getLayoutManager().getInnerHeight(portalConnector.getWidget().getElement());
            int reservedForRelativeSize = totalPortalHeight - totalFixedHeightConsumption;
            double ratio = reservedForRelativeSize / (double) totalPortalHeight * 100d;
            for (ComponentConnector cc : relativeHeightPortlets) {
                PortletConnector pc = PortalLayoutUtil.getPortletConnectorForContent(cc);
                if (!pc.isCollased()) {
                    float height = Util.parseRelativeSize(cc.getState().height);
                    double slotHeight = (height / totalPercentage * ratio);
                    pc.setSlotHeight(slotHeight + "%", slotHeight * totalPortalHeight / 100f);
                }
            }
        }
        Profiler.leave("PLC.recalcHeight");        
    }
}
