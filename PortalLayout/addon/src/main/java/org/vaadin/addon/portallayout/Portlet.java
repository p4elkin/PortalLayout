package org.vaadin.addon.portallayout;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.addon.portallayout.client.ui.VPortlet;
import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
@ClientWidget(value=VPortlet.class, loadStyle = LoadStyle.EAGER)
public class Portlet extends AbstractComponent implements ServerSideHandler {
    
    private boolean isLocked = false;

    private boolean isCollapsed = false;

    private boolean isClosable = true;

    private boolean isCollapsible = true;

    private Component content;
    
    private Component header;

    private ServerSideProxy proxy = new ServerSideProxy(this);

    public Portlet(final Component content) {
        super();
        setContent(content);
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public boolean isClosable() {
        return isClosable;
    }

    public boolean isCollapsible() {
        return isCollapsible;
    }
    
    public boolean isLocked() {
        return isLocked;
    }
    
    public void setCollapsed(boolean isCollapsed) {
        if (this.isCollapsed != isCollapsed) {
            this.isCollapsed = isCollapsed;
            proxy.call("setCollapsed", isCollapsed);
        }
        
    }
    
    public void setLocked(boolean isLocked) {
        if (this.isLocked != isLocked) {
            this.isLocked = isLocked;
            proxy.call("setLocked", isLocked);
        }
    }
    
    public void setClosable(boolean isClosable) {
        if (this.isClosable != isClosable) {
            this.isClosable = isClosable;
            proxy.call("setClosable", isClosable);
        }
    }

    public void setCollapsible(boolean isCollapsible) {
        if (this.isCollapsible != isCollapsible) {
            this.isCollapsible = isCollapsible;
            proxy.call("setCollapsible", isCollapsible);
        }
        
    }

    public Component getHeaderComponent() {
        return header;
    }

    public void setHeaderComponent(Component headerComponent) {
        if (this.header != headerComponent) {
            this.header = headerComponent;
            headerComponent.setParent(this);
            requestRepaint();
        }
    }
    
    public void setContent(Component content) {
        if (content != this.content) {
            this.content = content;
            content.setParent(this);
            requestRepaint();
        }
    }
    
    @Override
    public void attach() {
        super.attach();
        if (content != null) {content.attach();}
        if (header != null) {header.attach();}
    }
    
    @Override
    public void detach() {
        super.detach();
        if (content != null) {content.detach();}
        if (header != null) {header.detach();}
    }
    
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        proxy.paintContent(target);
        if (header != null) {
            target.startTag("header");
            header.paint(target);
            target.endTag("header");   
        }
        
        if (content != null) {
            target.startTag("content");
            content.paint(target);
            target.endTag("content");   
        }
    }

    public Component getContent() {
        return content;
    }
    
    public Component getHeader() {
        return header;
    }
    
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }
    
    @Override
    public Object[] initRequestFromClient() {
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unknown call " + method);
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Portlet) {
            final Portlet other = (Portlet)obj;
            final Component otherContent = other.getContent();
            if (otherContent == null) {
                result = (this.content == null);
            } else {
                result = this.content.equals(otherContent);
            }
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        if (content != null) {
            return content.hashCode();
        }
        return super.hashCode();
    }
}
