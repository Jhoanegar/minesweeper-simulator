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
public interface GameEvent extends SimulationEvent {
	
	/**
	 * @return
	 */
	public GameState getGameState();
	
	/**
	 * can be called only if state is different from GAME_ON
	 * 
	 * @return
	 * @throws IllegalStateException
	 */
	public int getMinesLeft() throws IllegalStateException;
	
	/**
	 * can be called only if state is equal to GAME_FINISHED
	 * 
	 * @return
	 * @throws IllegalStateException
	 */
	public Player getWinner() throws IllegalStateException;
	
	/**
	 * can be called only if state is equal to GAME_FINISHED
	 * 
	 * @param player
	 * @return
	 * @throws IllegalStateException
	 */
	public int getMineCount(Player player) throws IllegalStateException;

	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	public static enum GameState {
		/* game on going */
		GAME_ON,
		/* game on going, score changed */
		SCORE_CHANGED,
		/* game finished, tie */
		GAME_FINISHED;
	}
}