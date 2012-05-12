/*
 * Copyright 2009 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.addon.portallayout.client.dnd.util.impl;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

/*
 * {@link com.allen_sauer.gwt.dnd.client.util.DOMUtil} default cross-browser implementation.
 */
public abstract class DOMUtilImpl {

    public abstract String adjustTitleForBrowser(String title);

    public abstract void cancelAllDocumentSelections();

    public abstract int getBorderLeft(Element elem);

    public abstract int getBorderTop(Element elem);

    public abstract int getClientHeight(Element elem);

    public abstract int getClientWidth(Element elem);

    public native String getEffectiveStyle(Element elem, String style) /*-{
    var computedStyle = 
        this.@org.vaadin.addon.portallayout.client.dnd.util.impl.DOMUtilImpl::getComputedStyle(Lcom/google/gwt/dom/client/Element;Ljava/lang/String;)(elem,style);
    if (computedStyle) {
        return computedStyle;
    } else if (elem.currentStyle) {
        return elem.currentStyle[style];
    } else {
        return elem.style[style];
    }
    }-*/;

    public final int getHorizontalBorders(Widget widget) {
        return widget.getOffsetWidth() - getClientWidth(widget.getElement());
    }

    public final int getVerticalBorders(Widget widget) {
        return widget.getOffsetHeight() - getClientHeight(widget.getElement());
    }

    public final int getHorizontalBorders(Element elem) {
        return elem.getOffsetWidth() - getClientWidth(elem);
    }

    public final int getVerticalBorders(Element elem) {
        return elem.getOffsetHeight() - getClientHeight(elem);
    }
    
    private native String getComputedStyle(Element elem, String style) /*-{
        if ($doc.defaultView && $doc.defaultView.getComputedStyle) {
            var styles = $doc.defaultView.getComputedStyle(elem, "");
            if (styles) {
                return styles[style];
            }
        }
        return null;
    }-*/;
    
    public static native Integer parseInt(final String value)
    /*-{
        var number = parseInt(value, 10);
        if (isNaN(number))
            return null;
        else
            return @java.lang.Integer::valueOf(I)(number);
    }-*/;
    
    public final int getIntProperty(Element elem, String name) {
        final String style = getEffectiveStyle(elem, name);
        int result = 0;
        if (style != null) {
            try {
                result = parseInt(style).intValue();
            } catch (Exception e) {
                return 0;
            }
        }
        return result;
    }
}
