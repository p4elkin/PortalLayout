package org.vaadin.addon.portallayout;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.addon.portallayout.client.ui.VPortalLayout;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;

@SuppressWarnings("serial")
@ClientWidget(value=VPortalLayout.class, loadStyle=LoadStyle.EAGER)
public class Portal extends AbstractLayout {

    private List<Portlet> portlets = new LinkedList<Portlet>();
    
    public Portal() {
        super();
        setWidth("500px");
        final TextArea tx = new TextArea();
        tx.setSizeFull();
        final TextArea tx1 = new TextArea();
        tx1.setSizeFull();
        
        final Portlet p1 = new Portlet(tx);
        tx.setCaption("TEST");
        final Portlet p2 = new Portlet(tx1);
        
        tx1.setCaption("TEST");
        p1.setHeight("300px");
        p2.setHeight("300px");
        addPortlet(p1);
        addPortlet(p2);
    }
    
    @Override
    public void addComponent(Component c) {
        addPortlet(c);
    }
    
    private void addPortlet(Component c) {
        final Portlet portlet = new Portlet(c);
        addPortlet(portlet);
    }
    
    private void addPortlet(Portlet portlet) {
        super.addComponent(portlet);
        portlets.add(portlet);
        requestRepaint();
    }

    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
    }
    
    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {}

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        for (final Portlet portlet : portlets) {
            portlet.paint(target);
        }
    }
    
    @Override
    public Iterator<Component> getComponentIterator() {
        return new Iterator<Component>() {
            
            private Iterator<Portlet> wrappedIt = portlets.iterator();
            
            @Override
            public void remove() {
                wrappedIt.remove();
            }
            
            @Override
            public Component next() {
                return wrappedIt.next();
            }
            
            @Override
            public boolean hasNext() {
                return wrappedIt.hasNext();
            }
        };
    }
}
