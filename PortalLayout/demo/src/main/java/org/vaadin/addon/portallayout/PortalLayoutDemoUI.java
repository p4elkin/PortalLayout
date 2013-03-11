package org.vaadin.addon.portallayout;

import org.vaadin.addon.portallayout.demo.ActionDemoTab;
import org.vaadin.addon.portallayout.portal.PortalLayout;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class PortalLayoutDemoUI extends UI {

    static int i = 200;
    
    @Override
    protected void init(VaadinRequest request) {
        final Layout layout = new VerticalLayout();
        //layout.setMargin(true);
        layout.setSizeFull();
        
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        
        setContent(layout);

        hl.addComponent(createPortal(layout));
        hl.addComponent(createPortal(layout));
        layout.addComponent(new ActionDemoTab());
        //layout.addComponent(hl);
        //((VerticalLayout)layout).setExpandRatio(hl, 1f);
    }

    private PortalLayout createPortal(final Layout layout) {
        final PortalLayout portalLayout = new PortalLayout();
        portalLayout.setCaption("Portal");
        portalLayout.setHeight("90%");
        portalLayout.setWidth("60%");
        
        Component childComponent = new TextArea();
        //c.setSizeFull();
        childComponent.setCaption("TextBox1");
        childComponent.setHeight("50%");
        portalLayout.wrapInPortlet(childComponent);

        final Component c1 = new TextArea();
        c1.setCaption("TextBox2");
        portalLayout.wrapInPortlet(c1);
        
        final Component c2 = new TextArea();
        //c2.setSizeFull();
        c2.setCaption("TextBox3");
        portalLayout.wrapInPortlet(c2);
        
        childComponent.setIcon(new ExternalResource("http://cs323919.userapi.com/v323919017/28f1/kzjuLO59loc.jpg"));
        
        childComponent.setWidth("100%");
        c1.setWidth("100%");
        c2.setWidth("100%");
        
        return portalLayout;
    }

}
