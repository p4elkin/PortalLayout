package org.vaadin.addons.portallayout.gwt.shared.portlet.rpc;

import com.vaadin.shared.annotations.Delayed;
import com.vaadin.shared.communication.ServerRpc;

/**
 *
 */
public interface AbsolutePortletServerRpc extends ServerRpc {

    @Delayed
    void updateCoordinates(int x, int y);
}
