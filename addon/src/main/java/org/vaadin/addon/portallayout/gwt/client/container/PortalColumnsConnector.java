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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.addon.portallayout.container.PortalColumns;
import org.vaadin.addon.portallayout.gwt.shared.container.PortalColumnsState;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * PortalColumnsContainer.
 */
@Connect(PortalColumns.class)
public class PortalColumnsConnector extends AbstractLayoutConnector {

    private PortalColumnsDropController dropController;
    
    private final PickupDragController dragController = new PickupDragController(RootPanel.get(), false);
    
    private final Map<ComponentConnector, PortalSlot> portals = new HashMap<ComponentConnector, PortalSlot>();
    
    @Override
    protected void init() {
        super.init();
        this.dropController = new PortalColumnsDropController(this);
        dragController.registerDropController(dropController);
        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() { 
            @Override
            public void onElementResize(ElementResizeEvent e) {
                for (PortalSlot slot : portals.values()) {
                    slot.setHeight(e.getLayoutManager().getOuterHeight(e.getElement()) + "px");
                    slot.setWidth(e.getLayoutManager().getOuterWidth(e.getElement()) / 3 + "px");
                }
            }
        });
    }
    
    @Override
    public void updateCaption(ComponentConnector connector) {
        
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        final List<ComponentConnector> oldChildren = event.getOldChildren();
        final List<ComponentConnector> currentChildren = getChildComponents();
        oldChildren.removeAll(currentChildren);
        
        for (final ComponentConnector cc : oldChildren) {
            PortalSlot slot = portals.get(cc);
            if (slot != null) {
                portals.remove(cc);
                getWidget().remove(slot);    
            }
        }
        
        for (int i = 0; i < currentChildren.size(); ++i) {
            ComponentConnector cc = currentChildren.get(i);
            PortalSlot slot = portals.get(cc);
            if (slot != null) {
                int index = getWidget().getWidgetIndex(slot);
                if (index != i) {
                    getWidget().insert(slot, i);
                    getWidget().setCellWidth(slot, "33%");
                } 
            } else {
                slot = new PortalSlot();
                portals.put(cc, slot);
                getWidget().insert(slot, i);
                getWidget().setCellWidth(slot, "33%");
                slot.setPortal(cc.getWidget());
            }
        }
    }
    
    @Override
    public PortalColumnsWidget getWidget() {
        return (PortalColumnsWidget)super.getWidget();
    }

    @Override
    public PortalColumnsState getState() {
        return (PortalColumnsState)super.getState();
    }

}
