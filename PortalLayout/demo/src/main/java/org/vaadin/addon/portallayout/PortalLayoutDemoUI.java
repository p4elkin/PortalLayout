package org.vaadin.addon.portallayout;

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
        //layout.addComponent(new ActionDemoTab());
        layout.addComponent(hl);
        ((VerticalLayout)layout).setExpandRatio(hl, 1f);
    }

    private PortalLayout createPortal(final Layout layout) {
        final PortalLayout p = new PortalLayout();
        p.setCaption("Portal");
        p.setHeight("90%");
        p.setWidth("60%");
        
        Component c = new TextArea();
        //c.setSizeFull();
        c.setCaption("TextBox1");
        c.setHeight("50%");
        p.wrapInPortlet(c);

        final Component c1 = new TextArea();
        c1.setCaption("TextBox2");
        p.wrapInPortlet(c1);
        
        final Component c2 = new TextArea();
        //c2.setSizeFull();
        c2.setCaption("TextBox3");
        p.wrapInPortlet(c2);
        
        c.setIcon(new ExternalResource("http://cs323919.userapi.com/v323919017/28f1/kzjuLO59loc.jpg"));
        
        c.setWidth("100%");
        c1.setWidth("100%");
        c2.setWidth("100%");
        
        /*layout.addComponent(new Button("!", new ClickListener() {
            
            @Override
            public void buttonClick(ClickEvent event) { 
                i += 5;
                p.getPortlet(c1).setHeight(i + "px");
            }
        }));*/
        return p;
    }

}
