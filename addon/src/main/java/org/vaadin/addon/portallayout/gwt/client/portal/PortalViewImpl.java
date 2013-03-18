package org.vaadin.addon.portallayout.gwt.client.portal;

import org.vaadin.addon.portallayout.gwt.client.portlet.PortletChrome;

import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class PortalViewImpl extends FlowPanel implements PortalView {

    private final Element sentinelElement = DOM.createDiv();
    
    private final Presenter presenter;

    public PortalViewImpl(Presenter presenter) {
        super();
        this.presenter = presenter;
        this.sentinelElement.getStyle().setDisplay(Display.NONE);
        getElement().appendChild(sentinelElement);
        DragClientBundle.INSTANCE.css().ensureInjected();
    }

    @Override
    public Panel asWidget() {
        return this;
    }

    @Override
    public void insert(Widget w, int beforeIndex) {
        presenter.recalculateHeights();
        w.setWidth("100%");
        beforeIndex = adjustIndex(w, beforeIndex);
        w.removeFromParent();

        getChildren().insert(w, beforeIndex);
        if (getWidgetCount() > 1) {
            DOM.insertChild(getElement(), createSpacer(), beforeIndex > 0 ? 2 * beforeIndex - 1 : 0);
        }
        DOM.insertChild(getElement(), w.getElement(), beforeIndex > 0 ? 2 * beforeIndex : 0);
        // Adopt.
        adopt(w);
    }
    
   
    
    @Override
    public boolean remove(Widget w) {
        int index = getWidgetIndex(w);
        if (index <  getWidgetCount() - 1) {
            Element spacer = getElement().getChild(2 * index + 1).cast();
            getElement().removeChild(spacer);
        }
        boolean result =  super.remove(w);
        presenter.recalculateHeights();
        return result;
    }
    
    @Override
    public void addPortlet(PortletChrome p) {
        p.getAssociatedSlot().setWidget(p);
        if (getWidgetIndex(p.getAssociatedSlot()) < 0) {
            insert(p.getAssociatedSlot(), getElement().getChildCount() - 1);
        }
    }

    @Override
    public void removePortlet(PortletChrome portletWidget) {
        if (getWidgetIndex(portletWidget.getAssociatedSlot()) >= 0) {
            portletWidget.close();
        }
    }
    
    protected Element createSpacer() {
        Element result = DOM.createDiv();
        result.addClassName("portal-layout-spacing");
        return result;
    }
}
