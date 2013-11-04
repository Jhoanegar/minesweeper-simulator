/* Copyright 2013, Carlos Alegría Galicia
 *
 * This file is part of Mine Sweeper Simulator.
 *
 * Mine Sweeper Simulator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mine Sweeper Simulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mine Sweeper Simulator. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package mx.unam.fesa.mss.core;



/**
 * @author Carlos Alegría Galicia
 *
 */
public interface Cell {
	
	/**
	 * @return the contentType
	 */
	public ContentType getContentType();

	/**
	 * @return the state
	 */
	public State getState();

	/**
	 * @return the owner
	 */
	public Player getOwner();

	/**
	 * @return the mineCount
	 */
	public byte getMineCount();

	/**
	 * @author Carlos Alegría Galicia
	 */
	public static enum ContentType {
		/* no content */
		EMPTY,
		/* cell contains a mine */
		MINE,
		/* cell contains a positive number indicating how many neighbors (using
		 * 8-connectivity) contain mines
		 */
		MINE_COUNT;
	}
	
	/**
	 * @author Carlos Alegría Galicia
	 */
	public static enum State {
		/* the cell has not been revealed yet */
		COVERED,
		/* the cell has already been revealed */
		REVEALED,
		/* the cell has not been revealed yet, and is marked with a flag */
		FLAGGED;
	}
}