package org.vaadin.addon.portallayout.demo;

import com.vaadin.Application;
import com.vaadin.ui.TabSheet;

@SuppressWarnings("serial")
public class PortalTabSheet extends TabSheet {

    private final ActionDemoTab actionTab;
    /**
     * Constructor
     */
    public PortalTabSheet(Application app) {
        super();
        actionTab = new ActionDemoTab(app);
        
        
        addTab(actionTab, 
                "The first portal adds the red border to contents, second - green, third - yellow. Check out interactive headers! Try e.g. 51Bpx63wkbA for video, " +
                "rate the pictures, filter the table!", null);
        addListener(new SelectedTabChangeListener() {

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                if (getSelectedTab() != null) {
                    if (getSelectedTab().equals(actionTab)) {
                        actionTab.construct();
                    }
                }
            }
        });
    }

}
