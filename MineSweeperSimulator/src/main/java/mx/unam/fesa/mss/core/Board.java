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

import static mx.unam.fesa.mss.core.GameEvent.GameState.GAME_FINISHED;
import static mx.unam.fesa.mss.core.GameEvent.GameState.GAME_ON;
import static mx.unam.fesa.mss.core.GameEvent.GameState.SCORE_CHANGED;

import java.util.Arrays;
import java.util.Random;

import mx.unam.fesa.mss.core.Cell.ContentType;
import mx.unam.fesa.mss.core.GameEvent.GameState;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public final class Board {

	/* */
	private final int minesNumber;
	/* */
	private int globalMineCount;
	/* */
	private final int playerMineCount[];
	/* */
	private Player deadPlayer;
	/* */
	private CellImpl cells[][] = null;

	/**
	 * @param rows
	 * @param cols
	 * @param mines
	 */
	public Board(int rows, int cols, int mines) {

		if (rows <= 0) {
			throw new IllegalArgumentException("Specified rows number " + rows
					+ " is lower than or equal to cero");
		}
		
		if (cols <= 0) {
			throw new IllegalArgumentException("Specified cols number " + cols
					+ " is lower than or equal to cero");
		}
		
		if (mines <= 0 || mines > cols * rows) {
			throw new IllegalArgumentException("Specified mines number " + mines
					+ " is out of the allowed interval [1, rows * cols]");
		}

		//
		// member initialization
		//

		this.minesNumber = this.globalMineCount = mines;
		this.playerMineCount = new int[2]; Arrays.fill(this.playerMineCount, 0);
		this.deadPlayer = Player.NONE;
		this.cells = new CellImpl[rows][cols];
		for (int i = 0; i < this.cells.length; i++) {
			for (int j = 0; j < this.cells[0].length; j++) {
				this.cells[i][j] = new CellImpl();
			}
		}
		
		// the cells are spread with almost uniform probability over the board
		//
		int col, row, i = 0;
		Random colRandomizer = new Random();
		Random rowRandomizer = new Random();
		while (i < mines) {
			row = rowRandomizer.nextInt(rows);
			col = colRandomizer.nextInt(cols);

			if (this.cells[row][col].getContentType() == Cell.ContentType.MINE)
				continue;
			this.cells[row][col].contentType = Cell.ContentType.MINE;

			incrementMineCount(row - 1, col - 1);
			incrementMineCount(row, col - 1);
			incrementMineCount(row + 1, col - 1);
			incrementMineCount(row - 1, col);
			incrementMineCount(row + 1, col);
			incrementMineCount(row - 1, col + 1);
			incrementMineCount(row, col + 1);
			incrementMineCount(row + 1, col + 1);
			i++;
		}

	}

	/**
	 * Increments the mine count on 8-conn neighbor cells of the cell with
	 * located at the given coordinates.
	 * 
	 * @param row
	 * @param col
	 */
	private void incrementMineCount(int row, int col) {
		if (row < 0 || cells.length <= row || col < 0 || cells[0].length <= col)
			return;

		CellImpl cell = cells[row][col];
		if (cell.getContentType() == Cell.ContentType.MINE)
			return;
		cell.contentType = Cell.ContentType.MINE_COUNT;
		cell.mineCount++;
	}

	/**
	 * @return
	 */
	public Cell[][] getCells() {
		return this.cells;
	}

	/**
	 * Execute the given move on this board.
	 * 
	 * @param move
	 * @return
	 */
	public GameState doMove(Move move) {

		CellImpl cell;
		try {
			cell = this.cells[move.getRow()][move.getCol()];
		} catch (IndexOutOfBoundsException e) {
			throw new SimulationException (
					"Target cell coordinates are out of bounds", e);
		}

		switch (move.getType()) {
		case REMOVE_FLAG:
			if (cell.getState() != Cell.State.FLAGGED) {
				throw new SimulationException("Unable to execute command'"
					+ move + "': Target cell in wrong state.");
			}
			if (cell.getOwner() != move.getSource()) {
				throw new SimulationException("Unable to execute command'"
					+ move + "': Target cell belongs to a different player.");
			}

			cell.state = Cell.State.COVERED;
			if (cell.getContentType() == ContentType.MINE) {
				this.playerMineCount[cell.getOwner().ordinal()]--;
				this.globalMineCount++;
				return SCORE_CHANGED;
			}
			break;
		case SET_FLAG:
			if (cell.getState() != Cell.State.COVERED) {
				throw new SimulationException("Unable to execute command'"
						+ move + "': Target cell in wrong state.");
			}
			
			cell.state = Cell.State.FLAGGED;
			cell.owner = move.getSource();
				
			if (cell.getContentType() == ContentType.MINE) {
				this.playerMineCount[cell.getOwner().ordinal()]++;
				this.globalMineCount--;
				
				return this.globalMineCount == 0 ? GAME_FINISHED : SCORE_CHANGED;
			}
			break;
		case UNCOVER:
			if (cell.getState() != Cell.State.COVERED) {
				throw new SimulationException("Unable to execute command'"
						+ move + "': Target cell in wrong state.");
			}
			
			cell.state = Cell.State.REVEALED;
			cell.owner = move.getSource();
				
			if (cell.getContentType() == ContentType.MINE) {
				this.deadPlayer = cell.getOwner();
				return GAME_FINISHED;
			}
			break;
		}
		
		return GAME_ON;
	}
	
	/**
	 * Return the mines spread in this board.
	 * 
	 * @return
	 */
	public int getMinesNumber() {
		return this.minesNumber;
	}
	
	/**
	 * Get the number of mines yet to be discovered.
	 * 
	 * @return
	 */
	public int getMineCount() {
		return this.globalMineCount;
	}
	
	/**
	 * Get the number of mines discovered so far by the given {@link Player}.
	 * 
	 * @return
	 */
	public int getMineCount(Player player) {
		return this.playerMineCount[player.ordinal()];
	}
	
	/**
	 * Gets the player that discovered a mine, if any
	 * 
	 * @return
	 */
	public Player getDeadPlayer() {
		return this.deadPlayer;
	}
	
	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	private final static class CellImpl implements Cell {
		/* */
		ContentType contentType = Cell.ContentType.EMPTY;
		/* */
		State state = Cell.State.COVERED;
		/* */
		Player owner = Player.NONE;
		/* */
		byte mineCount = 0;
		
		/**
		 * @return the contentType
		 */
		public ContentType getContentType() {
			return contentType;
		}

		/**
		 * @return the state
		 */
		public State getState() {
			return state;
		}

		/**
		 * @return the owner
		 */
		public Player getOwner() {
			return owner;
		}

		/**
		 * @return the mineCount
		 */
		public byte getMineCount() {
			return mineCount;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			switch (state) {
			case COVERED:
				return "C";
			case FLAGGED:
				return owner + "F";
			case REVEALED:
				switch (contentType) {
				case EMPTY:
					return owner + "E";
				case MINE:
					return owner + "M";
				case MINE_COUNT:
					return owner + Integer.toString(mineCount);
				default:
					return "";
				}
			default:
				return "";
			}
		}
	}
}