package org.vaadin.addon.portallayout.demo;

import org.vaadin.addon.portallayout.PortalLayout;
import org.vaadin.addon.portallayout.PortalLayout.PortletCloseListener;
import org.vaadin.addon.portallayout.PortalLayout.PortletClosedEvent;
import org.vaadin.addon.portallayout.PortalLayout.PortletCollapseEvent;
import org.vaadin.addon.portallayout.PortalLayout.PortletCollapseListener;
import org.vaadin.addon.portallayout.ToolbarAction;
import org.vaadin.addon.portallayout.demo.DemoTable.NameType;
import org.vaadin.addon.portallayout.event.Context;
import org.vaadin.teemu.ratingstars.RatingStars;
import org.vaadin.youtubeplayer.YouTubePlayer;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class ActionDemoTab extends Panel implements PortletCloseListener, PortletCollapseListener {
   
    public class DemoPortal extends PortalLayout {
        
        public DemoPortal() {
            setWidth("100%");
            setHeight("800px");
            addCloseListener(ActionDemoTab.this);
            addCollapseListener(ActionDemoTab.this);
            setMargin(true);
        }
    }
    
    private final PortalLayout videoPortal = new DemoPortal() {
        @Override
        public void addComponent(Component c, int position) {
            super.addComponent(c, position);
            clearPortletStyleNames(c);
            addPortletStyleName(c, "red");
        };
    };
    
    private final PortalLayout imagePortal = new DemoPortal()  {
        @Override
        public void addComponent(Component c, int position) {
            super.addComponent(c, position);
            clearPortletStyleNames(c);
            addPortletStyleName(c, "green");
        };
    };
    
    private final PortalLayout miscPortal = new DemoPortal()  {
        @Override
        public void addComponent(Component c, int position) {
            super.addComponent(c, position);
            clearPortletStyleNames(c);
            addPortletStyleName(c, "yellow");
        };
    };
    
    private final Application app;
    
    private final GridLayout layout = new GridLayout(3, 1);
    
    private boolean init = false;
    
    public ActionDemoTab(Application app) {
        super();
        this.app = app;
        setSizeFull();
        setContent(layout);
        layout.setWidth("100%");
        layout.setMargin(true);
        layout.setSpacing(true);
        buildPortals();
        construct();
    }

    private void buildPortals() {
        layout.addComponent(videoPortal, 0, 0);
        layout.addComponent(imagePortal, 1, 0);
        layout.addComponent(miscPortal, 2, 0);
    }
    

    public void construct() {
        if (!init) {
            init = true;
            createVideoContents();
            createImageContents();
            createMiscContents();   
        }
    }
    
    private void createMiscContents() {
        final DemoTable table = new DemoTable();
        miscPortal.addComponent(table);
        table.setCaption("Artists");
        table.setIcon(new ThemeResource("chart.png"));
        
        final HorizontalLayout header =  new HorizontalLayout();
        final TextField filterField = new TextField();
        filterField.setWidth("100px");
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
        miscPortal.setHeaderComponent(table, header);
        filterField.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                table.filter((NameType)filterType.getValue(), filterField.getValue().toString());
            }
        });
    }

    private void createImageContents() {
        final PortalImage image = new PortalImage(app);
        imagePortal.addComponent(image);
        final RatingStars rating = new RatingStars();
        rating.setImmediate(true);
        rating.addListener(new ValueChangeListener() {            
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (rating.getValue() != null) {
                    image.setRating((Double)rating.getValue());
                }
            }
        });
        imagePortal.setHeaderComponent(image, rating);
        imagePortal.addAction(image, new ToolbarAction(new ThemeResource("arrow_right.png")) {
            @Override
            public void execute(final Context context) {
                if (!image.isEmpty()) {
                    image.showNextFile();
                    final Component header = context.getPortal().getHeaderComponent(image);
                    if (header instanceof Field) {
                        ((Field) header).setValue(image.getRating());
                    }
                }
            }
        });
        imagePortal.addAction(image, new ToolbarAction(new ThemeResource("arrow_left.png")) {
            @Override
            public void execute(final Context context) {
                if (!image.isEmpty()) {
                    image.showPrevFile();
                    final Component header = context.getPortal().getHeaderComponent(image);
                    if (header instanceof Field) {
                        ((Field) header).setValue(image.getRating());
                    }
                }
            }
        });
    }
    
    private void createVideoContents() {
        final YouTubePlayer pl = new YouTubePlayer();
        pl.setHeight("100%");
        pl.setVideoId("QrzGpVOPcTI");
        pl.setImmediate(true);
        videoPortal.addComponent(pl);
        pl.setCaption("Player");
        pl.setIcon(new ThemeResource("video.png"));
        
        final HorizontalLayout header =  new HorizontalLayout();
        final TextField idField = new TextField();
        final Label caption = new Label("Id: ");
        caption.addStyleName("v-white-text");
        idField.setImmediate(true);
        idField.setWidth("100px");
        header.setSizeUndefined();
        header.addComponent(caption);
        header.addComponent(idField);
        header.setSpacing(true);
        header.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
        
        idField.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                pl.setVideoId(idField.getValue().toString());
            }
        });
        
        videoPortal.setHeaderComponent(pl, header);
        videoPortal.addAction(pl, new ToolbarAction(new ThemeResource("stop.png")) {
            @Override
            public void execute(final Context context) {
                pl.stop();
                final Notification n = new Notification("Stop! If didn't stop - DO NOT use YouTube add-on and FF!");
                n.setDelayMsec(1000);
                getWindow().showNotification(n);
            }
        });
        
        videoPortal.addAction(pl, new ToolbarAction(new ThemeResource("pause.png")) {
            @Override
            public void execute(final Context context) {
                pl.pause();
                final Notification n = new Notification("Pause! If didn't pause - DO NOT use YouTube add-on and FF!");
                n.setDelayMsec(1000);
                getWindow().showNotification(n);
            }
        });
        
        videoPortal.addAction(pl, new ToolbarAction(new ThemeResource("play.png")) {
            @Override
            public void execute(final Context context) {
                pl.requestRepaint();
                pl.play();
                final Notification n = new Notification("Play! If didn't start - DO NOT use YouTube add-on and FF!");
                n.setDelayMsec(1000);
                getWindow().showNotification(n);
            }
        });
    }


    @Override
    public void portletCollapseStateChanged(PortletCollapseEvent event) {
        final Context context = event.getContext();
        getWindow().showNotification(context.getComponent().getCaption() + "collapsed " + 
                context.getPortal().isCollapsed(context.getComponent()));
    }

    @Override
    public void portletClosed(PortletClosedEvent event) {
        getWindow().showNotification(event.getContext().getComponent().getCaption() + "closed");
    }

}
