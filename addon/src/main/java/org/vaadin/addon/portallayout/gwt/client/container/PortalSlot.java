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
package org.vaadin.addon.portallayout.gwt.client.container;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * PortalSlot.
 */
public class PortalSlot extends Composite {

    private final FlowPanel root = new FlowPanel();
    
    private final HTML header = new HTML();
    
    public PortalSlot() {
        initWidget(root);
        root.add(header);
        root.getElement().getStyle().setWidth(100, Unit.PCT);
        root.getElement().getStyle().setHeight(100, Unit.PCT);
    }
    
    public HTML getHeader() {
        return header;
    }
    
    public void setPortal(Widget w) {
        root.add(w);
        w.getElement().getStyle().setTop(0, Unit.PX);
        w.getElement().getStyle().setBottom(0, Unit.PX);
        w.getElement().getStyle().setLeft(0, Unit.PX);
        w.getElement().getStyle().setRight(0, Unit.PX);
    }
}
