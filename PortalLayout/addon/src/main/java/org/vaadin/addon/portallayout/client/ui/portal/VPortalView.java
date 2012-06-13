package org.vaadin.addon.portallayout.client.ui.portal;

import java.util.List;

import org.vaadin.addon.portallayout.client.ui.PortalDropController;
import org.vaadin.addon.portallayout.client.ui.portlet.AnimationType;
import org.vaadin.addon.portallayout.client.ui.portlet.PortalObject;
import org.vaadin.addon.portallayout.client.ui.portlet.VPortlet;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.vaadin.terminal.gwt.client.ui.VMarginInfo;

public interface VPortalView extends IsWidget, HasWidgets, InsertPanel.ForIsWidget {

    public interface Presenter {
        
    }
    
    void setDropCotroller(final PortalDropController controller);
    
    List<VPortlet> getPortlets();

    void setSpacing(boolean hasSpacing);

    void setCommunicative(boolean isCommunicative);

    void setAnimationEnabled(AnimationType type, boolean isEnabled);
    
    void setAnimationSpeed(AnimationType type, int speed);
    
    int getAnimationSpeed(final AnimationType animationType);

    boolean isAnimationEnabled(AnimationType type);

    void updateMargins(VMarginInfo marginInfo);

    void setPortletLock(VPortlet portlet, boolean isLocked);
    
    ComplexPanel getRootPanel();

    void acceptPortlet(PortalObject portlet, int pos);

    void removePortlet(VPortlet w);
}
