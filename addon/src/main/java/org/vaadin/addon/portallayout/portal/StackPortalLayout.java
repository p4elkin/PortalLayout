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

import java.util.Iterator;

import org.vaadin.addon.portallayout.portlet.Portlet;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout.SpacingHandler;

/**
 * Stacks the child components inside of itself wrapping each of them into a {@link Portlet} instance. 
 */
@StyleSheet("portallayout_styles.css")
public class StackPortalLayout extends PortalBase implements SpacingHandler {

    @Override
    public void removePortlet(Portlet portlet) {
        portlet.getContent().setWidth(portlet.getPreferredFixedContentWidth());
        super.removePortlet(portlet);
    }
    
    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        Iterator<Component> it = portletContentIterator();
        while (it.hasNext()) {
            final Component c = it.next();
            String width = String.format("%d%s", (int)c.getWidth(), c.getWidthUnits().getSymbol());
            c.setWidth("100%");
            if (!"100%".equals(width)) {
                getPortlet(c).setPreferredFixedContentWidth(width);
                c.beforeClientResponse(initial);
            }
        }
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
