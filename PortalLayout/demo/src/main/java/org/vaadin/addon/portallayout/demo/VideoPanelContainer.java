package org.vaadin.addon.portallayout.demo;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.addon.portallayout.PortalLayout;
import org.vaadin.youtubeplayer.YouTubePlayer;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class VideoPanelContainer extends HorizontalSplitPanel {
    private final Map<String, String> initialVideoIds = new HashMap<String, String>();

    private final Panel rightPanel = new Panel();

    private final Panel mainPanel = new Panel();

    final PortalLayout rightPortal = new PortalLayout();

    /**
     * Constructor
     */
    public VideoPanelContainer() {
        super();
        setSizeFull();
        bundleStartVideos();
        setSplitPosition(80);
        buildContent();
    }

    private void bundleStartVideos() {
        initialVideoIds.put("eqfiHfDmOnw", "Interpol - Evil");
        initialVideoIds.put("oZDiOuJR5HM", "Sick Of It All - Death Or Jail");
        initialVideoIds.put("51Bpx63wkbA", "Royksopp - Happy Up There");
        initialVideoIds.put("Bz8iEJeh26E", "Sigur Ros - Glosoli");
    }

    private void buildContent() {
        buildMainPanel();
        buildRightPanel();
    }

    private void buildRightPanel() {
        VerticalLayout sideLayout = new VerticalLayout();
        sideLayout.setSizeFull();
        rightPortal.setSizeFull();
        for (final String str : initialVideoIds.keySet())
            addVideo(str, initialVideoIds.get(str));
        sideLayout.addComponent(new ArtistCombo());
        sideLayout.addComponent(rightPortal);
        sideLayout.setExpandRatio(rightPortal, 1f);
        addComponent(sideLayout);
    }

    private void addVideo(String str, String name) {
        final YouTubePlayer pl = new YouTubePlayer();
        pl.setWidth("100%");
        pl.setHeight("100%");
        pl.setVideoId(str);
        pl.setImmediate(true);
        rightPortal.addComponent(pl);
        pl.setCaption(name);
    }

    private void buildMainPanel() {
        mainPanel.setSizeFull();
        final HorizontalLayout mainPanelLayout = new HorizontalLayout();
        mainPanelLayout.addStyleName("dark-background");
        mainPanelLayout.setSizeFull();
        mainPanel.setContent(mainPanelLayout);

        mainPanelLayout.setMargin(false);
        mainPanelLayout.setWidth("100%");
        final PortalLayout widePortal = new PortalLayout();
        widePortal.setSpacing(false);
        widePortal.setSizeFull();
        mainPanelLayout.setSpacing(true);
        mainPanelLayout.addComponent(widePortal);
        mainPanelLayout.setExpandRatio(widePortal, 1f);
        addComponent(mainPanel);
    }

    private class ArtistCombo extends ComboBox {
        private Map<String, String> artistVideos = new HashMap<String, String>();

        public ArtistCombo() {
            super();
            setWidth("100%");
            setImmediate(true);
            bundleArtists();
            for (final String str : artistVideos.keySet())
                addItem(str);
            rightPanel.addComponent(new Button());
            addListener(new ValueChangeListener() {
                @Override
                public void valueChange(
                        com.vaadin.data.Property.ValueChangeEvent event) {
                    if (event.getProperty().getValue() == null)
                        return;
                    String name = event.getProperty().getValue().toString();
                    addVideo(artistVideos.get(name), name);
                }
            });
        }

        private void bundleArtists() {
            artistVideos.put("Editors - Munich", "oVLdaa4Wplo");
            artistVideos.put("Joy Division - Disorder", "QrzGpVOPcTI");
            artistVideos.put("The Killers - When You Were Young", "ff0oWESdmH0");
            artistVideos.put("Interpol - Evil", "eqfiHfDmOnw");
            artistVideos.put("Sick Of It All - Death Or Jail", "oZDiOuJR5HM");
            artistVideos.put("Royksopp - Happy Up There", "51Bpx63wkbA");
            artistVideos.put("Sigur Ros - Glosoli", "Bz8iEJeh26E");
        }
    }
}
