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

import org.vaadin.addon.portallayout.gwt.client.portlet.PortletChrome;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * AbsolutePortalViewImpl.
 */
public class AbsolutePortalViewImpl extends AbsolutePanel implements PortalView {

    @Override
    public Panel asWidget() {
        return (Panel)super.asWidget();
    }
    
    @Override
    public void addPortlet(PortletChrome p) {
        p.getAssociatedSlot().setWidget(p);
        if (getWidgetIndex(p.getAssociatedSlot()) < 0) {
            add(p.getAssociatedSlot());
        }
    }

    @Override
    public void removePortlet(PortletChrome portletWidget) {
        if (getWidgetIndex(portletWidget.getAssociatedSlot()) >= 0) {
            portletWidget.close();
        }
    }
}
