package org.vaadin.addon.portallayout.demo;

import org.vaadin.addon.portallayout.demo.DemoTable.NameType;
import org.vaadin.addon.portallayout.event.PortletCloseEvent;
import org.vaadin.addon.portallayout.event.PortletCollapseEvent;
import org.vaadin.addon.portallayout.portal.PortalBase;
import org.vaadin.addon.portallayout.portal.StackPortalLayout;
import org.vaadin.addon.portallayout.portlet.Portlet;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class ActionDemoTab extends Panel implements PortletCloseEvent.Listener, PortletCollapseEvent.Listener {
   
    public class DemoPortal extends StackPortalLayout {
        
        public DemoPortal() {
            setWidth("100%");
            setHeight("100%");
            addPortletCloseListener(ActionDemoTab.this);
            addPortletCollapseListener(ActionDemoTab.this);
            setMargin(true);
        }
    }
    
    private final PortalBase videoPortal = new DemoPortal() {
        @Override
        public Portlet portletFor(Component c) {
            c.setHeight("300px");
            Portlet p = super.portletFor(c);
            return p;
        }
    };
    
    private final PortalBase imagePortal = new DemoPortal() {
        @Override
        public Portlet portletFor(Component c/*, int position*/) {
            setWidth("100%");
            setHeight("800px");
            c.setHeight("30%");
            Portlet p = super.portletFor(c/*, position*/);
            addPortletCloseListener(ActionDemoTab.this);
            addPortletCollapseListener(ActionDemoTab.this);
            return p;
        };
    };
    
    private final PortalBase miscPortal = new DemoPortal()  {
        @Override
        public Portlet portletFor(Component c/*, int position*/) {
            Portlet p = super.portletFor(c/*, position*/);
            return p;
        };
    };

    private boolean init = false;

    private final HorizontalLayout layout = new HorizontalLayout();
    
    public ActionDemoTab() {
        super();
        setSizeFull();
        setContent(layout);
        layout.setWidth("100%");
        buildPortals();
        construct();
    }

    private void buildPortals() {
        ((StackPortalLayout)videoPortal).setSpacing(false);
        ((StackPortalLayout)miscPortal).setSpacing(true);
        layout.addComponent(videoPortal);
        layout.addComponent(imagePortal);
        layout.addComponent(miscPortal);
    }
    

    public void construct() {
        if (init)
            return;
        init = true;
        createVideoContents();
        createImageContents();
        createMiscContents();
    }
    
    private void createMiscContents() {
        final DemoTable table = new DemoTable();
        Portlet portlet = miscPortal.portletFor(table);
        table.setCaption("Artists");
        //table.setIcon(new ThemeResource("chart.png"));
        
        final HorizontalLayout header =  new HorizontalLayout();
        final TextField filterField = new TextField();
        final NativeSelect filterType = new NativeSelect();
        final Label caption = new Label("Filter: ");
        for (final NameType t : NameType.values()) {
            filterType.addItem(t);
        }
        filterType.setValue(NameType.NT_FIRST_NAME);
        caption.addStyleName("v-white-text");
        filterField.setImmediate(true);
        header.setSizeUndefined();
        header.addComponent(caption);
        header.addComponent(filterField);
        header.addComponent(filterType);
        header.setSpacing(true);
        header.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
        header.setComponentAlignment(filterType, Alignment.MIDDLE_LEFT);
        portlet.setHeaderComponent(header);
        filterField.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                table.filter((NameType)filterType.getValue(), filterField.getValue());
            }
        });
    }

    private void createImageContents() {
        final PortalImage image = new PortalImage();
        Portlet portlet = imagePortal.portletFor(image);
        portlet.setClosable(false);
    }
    
    private void createVideoContents() {
        
        Embedded pl = new Embedded(null, new ExternalResource(
                "http://www.youtube.com/v/meXvxkn1Y_8&hl=en_US&fs=1&"));
        pl.setAlternateText("Vaadin Eclipse Quickstart video");
        pl.setMimeType("application/x-shockwave-flash");
        pl.setParameter("allowFullScreen", "true");
        pl.setHeight("100%");
        pl.setWidth("100%");
        pl.setImmediate(true);
        final TextArea tf = new TextArea();
        tf.setSizeFull();
        Portlet portlet = videoPortal.portletFor(tf);
        portlet.setCollapsible(false);
        pl.setCaption("Joy Division - Disorder");
        //pl.setIcon(new ThemeResource("video.png"));
        
        final HorizontalLayout header =  new HorizontalLayout();
        final TextField idField = new TextField();
        final Label caption = new Label("Enter video id: ");
        caption.addStyleName("v-white-text");
        idField.setImmediate(true);
        header.setSizeUndefined();
        header.addComponent(caption);
        header.addComponent(idField);
        header.setSpacing(true);
        header.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
        
        idField.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                //pl.setVideoId(idField.getValue().toString());
            }
        });

    }

    @Override
    public void portletCollapseStateChanged(PortletCollapseEvent event) {
        Notification.show(event.getPortlet().getParent().getCaption() + "collapsed " + event.getPortlet().isCollapsed());
    }

    @Override
    public void portletClosed(PortletCloseEvent event) {
        Notification.show(event.getPortlet().getParent().getCaption() + "closed");
    }

}
