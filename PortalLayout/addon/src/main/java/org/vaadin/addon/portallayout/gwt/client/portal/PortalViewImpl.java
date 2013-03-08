package org.vaadin.addon.portallayout.gwt.client.portal;

import org.vaadin.addon.portallayout.gwt.client.portlet.PortletWidget;

import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class PortalViewImpl extends FlowPanel implements PortalView {

    private final Presenter presenter;

    public PortalViewImpl(Presenter presenter) {
        super();
        this.presenter = presenter;
        DragClientBundle.INSTANCE.css().ensureInjected();
    }

    @Override
    public Panel asWidget() {
        return this;
    }
    
    @Override
    public void insert(Widget w, int beforeIndex) {
        super.insert(w, beforeIndex);
        presenter.recalculateHeights();
    }
    
    @Override
    public boolean remove(Widget w) {
        boolean result =  super.remove(w);
        presenter.recalculateHeights();
        return result;
    }
    
    @Override
    public void addPortlet(PortletWidget p) {
        p.getSlot().setWidget(p);
        if (getWidgetIndex(p.getSlot()) < 0) {
            add(p.getSlot());
        }
    }

    @Override
    public void removePortlet(PortletWidget portletWidget) {
        if (getWidgetIndex(portletWidget.getSlot()) >= 0) {
            portletWidget.close();
        }
    }
}
