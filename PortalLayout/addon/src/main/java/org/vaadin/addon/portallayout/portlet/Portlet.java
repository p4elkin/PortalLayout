package org.vaadin.addon.portallayout.portlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vaadin.addon.portallayout.gwt.shared.portlet.PortletState;
import org.vaadin.addon.portallayout.gwt.shared.portlet.rpc.PortletServerRpc;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Notification;

public class Portlet extends AbstractComponent implements HasComponents {

    public Portlet(final Component content) {
        setContent(content);
        setStyleName("v-portlet");
        content.setSizeFull();
        registerRpc(new PortletServerRpc() {
            @Override
            public void setCollapsed(boolean isCollapsed) {
                getState().isCollapsed = isCollapsed;
                getContent().setVisible(!isCollapsed);
            }
            
            @Override
            public void updateCaptionFromContent(String caption) {
                getState(false).caption = caption;
            }
        });
        setHeaderToolbar(new Button("tests", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Notification.show("HUI");
            }
        }));
    }

    public Component getContent() {
        return (Component)getState().content;
    }
    
    public Component getHeaderToolbar() {
        return (Component)getState().headerToolbar;
    }
    
    public void setContent(Component c) {
        if (getState().content != null && getState().content != c) {
            Component oldContent = (Component)getState().content;
            oldContent.setParent(null);
        }
        if (c != null) {
            if (c.getParent() != null) {
                AbstractSingleComponentContainer.removeFromParent(c);
            }
            c.setParent(this);
        }
        getState().content = c;
    }
    
    public void setHeaderToolbar(Component c) {
        if (getState().headerToolbar != null && getState().headerToolbar != c) {
            Component oldContent = (Component)getState().headerToolbar;
            oldContent.setParent(null);
        }
        if (c != null) {
            if (c.getParent() != null) {
                AbstractSingleComponentContainer.removeFromParent(c);
            }
            c.setParent((Component)getState().content);
        }
        getState().headerToolbar = c;
    }
    
    @Override
    protected PortletState getState(boolean markAsDirty) {
        return (PortletState)super.getState(markAsDirty);
    }
    
    @Override
    protected PortletState getState() {
        return (PortletState) super.getState();
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width < 0 ? width : 100f, Unit.PERCENTAGE);
        getState().slotSize.width = width;
        getState().slotSize.widthUnit = unit.getSymbol();
    }
    
    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height < 0 ? height : 100f, Unit.PERCENTAGE);
        getState().slotSize.height = height;
        getState().slotSize.heightUnit = unit.getSymbol();
    }
    
    @Override
    public Iterator<Component> iterator() {
        List<Component> components = new ArrayList<Component>();
        if (getContent() != null) {
            components.add(getContent());
        }
        if (getHeaderToolbar() != null) {
            components.add(getHeaderToolbar());
        }
        return components.iterator();
    }
}
