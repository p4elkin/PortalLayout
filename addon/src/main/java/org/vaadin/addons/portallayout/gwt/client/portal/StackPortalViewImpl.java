package org.vaadin.addons.portallayout.gwt.client.portal;

import org.vaadin.addons.portallayout.gwt.client.portlet.PortletChrome;

import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class StackPortalViewImpl extends FlowPanel implements PortalView {
    
    private final Presenter presenter;

    public StackPortalViewImpl(Presenter presenter) {
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
        presenter.recalculateHeights();
        w.setWidth("100%");
        beforeIndex = adjustIndex(w, beforeIndex);
        w.removeFromParent();
        getChildren().insert(w, beforeIndex);

        if (beforeIndex == 0 && getElement().getChildCount() > 0) {
            ((Element)getElement().getFirstChild()).getStyle().setDisplay(Style.Display.BLOCK);
        }

        DOM.insertChild(getElement(), w.getElement(), beforeIndex > 0 ? 2 * beforeIndex : 0);
        DOM.insertChild(getElement(), createSpacer(), beforeIndex > 0 ? 2 * beforeIndex : 0);

        if (getElement().getChildCount() > 0) {
            ((Element)getElement().getFirstChild()).getStyle().setDisplay(Style.Display.NONE);
        }

        // Adopt.
        adopt(w);
    }
    
    @Override
    public boolean remove(Widget w) {
        Element root = getElement();
        int index = getWidgetIndex(w);
        Element spacer = root.getChild(2 * index).cast();
        root.removeChild(spacer);
        boolean result =  super.remove(w);
        presenter.recalculateHeights();
        if (getWidgetCount() > 0) {
            ((Element)getElement().getFirstChild()).getStyle().setDisplay(Style.Display.NONE);
        }
        return result;
    }
    
    @Override
    public void addPortlet(PortletChrome p) {
        p.getAssociatedSlot().setWidget(p);
        if (getWidgetIndex(p.getAssociatedSlot()) < 0) {
            insert(p.getAssociatedSlot(), getWidgetCount());
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
