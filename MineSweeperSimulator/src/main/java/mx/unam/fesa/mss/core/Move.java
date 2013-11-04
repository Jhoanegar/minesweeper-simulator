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

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * @author Carlos Alegría Galicia
 *
 */
public final class Move {
	
	/* */
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
	
	/* */
	private Player source;
	/* */
	private Type type;
	/* */
	private int row;
	/* */
	private int col;
	/* */
	private String formattedTimeStamp;
	/* */
	private long timeStamp;
	

	/**
	 * @return the source
	 */
	public Player getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Player source) {
		this.source = source;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the row
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @param row the row to set
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * @return the col
	 */
	public int getCol() {
		return col;
	}

	/**
	 * @param col the col to set
	 */
	public void setCol(int col) {
		this.col = col;
	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
		synchronized (Move.dateFormat) {
			this.formattedTimeStamp = dateFormat.format(timeStamp);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + source + " '" + formattedTimeStamp + "' " + type + " "
				+ row + " " + col + ")";
	}
	
	
	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	public static enum Type {
		/* */
		SET_FLAG,
		/* */
		REMOVE_FLAG,
		/* */
		UNCOVER;
	}
}