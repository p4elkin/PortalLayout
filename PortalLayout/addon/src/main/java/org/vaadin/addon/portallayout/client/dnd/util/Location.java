package org.vaadin.addon.portallayout.client.dnd.util;

/**
 * Class representing a location defined by left (x) and top (y) coordinates.
 */
public interface Location {

  /**
   * Get x coordinate.
   * 
   * @return the left offset in pixels
   */
  int getLeft();

  /**
   * Get the y coordinate.
   * 
   * @return the top offset in pixels
   */
  int getTop();

  /**
   * Return a new location, snapped to the grid based on a spacing of <code>(gridX, gridY)</code>.
   * 
   * @param gridX the horizontal grid spacing in pixels
   * @param gridY the vertical grid spacing in pixels
   * @return the new location
   */
  Location newLocationSnappedToGrid(int gridX, int gridY);
}