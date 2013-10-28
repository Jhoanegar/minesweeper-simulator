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
package mx.unam.fesa.isoo.mss.core;

import static mx.unam.fesa.isoo.mss.core.GameEvent.GameState.GAME_FINISHED;
import static mx.unam.fesa.isoo.mss.core.GameEvent.GameState.GAME_ON;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mx.unam.fesa.isoo.mss.core.GameEvent.GameState;

/**
 * @author Carlos Alegría Galicia
 *
 */
public class Simulator {
	
	/* */
	private final Board board;
	/* */
	private final Lock boardLock;
	/* */
	private final String names[];
	/* */
	private final Move commands[];
	/* */
	private final Comparator<Move> comparator;
	
	/* */
	private int cycle = 0;
	/* */
	private final GameEventImpl gameEvent;
	/* */
	private final BoardEventImpl boardEvent;
	/* */
	private GameState state;
	/* */
	private SimulationListener listener = null;
	
			
	/**
	 * @param rows
	 * @param cols
	 * @param mines
	 */
	public Simulator(int rows, int cols, int mines) {
		
		//
		// initialization
		//

		this.board = new Board(rows, cols, mines);
		this.boardLock = new ReentrantLock();
		this.names = new String[2]; Arrays.fill(this.names, null);
		this.commands = new Move[2]; Arrays.fill(this.commands, null);
		this.comparator = new Comparator<Move>() {
			
			@Override
			public int compare(Move o1, Move o2) {
				if (o1 == null) {
					return o2 == null ? 0 : 1;
				} else {
					if (o2 == null) {
						return -1;
					} else {
						 return o1.getTimeStamp() > o2.getTimeStamp()
								 ? 1 : o1.getTimeStamp() < o2.getTimeStamp()
										 ? -1 : 0;
					}
				}
			}
		};
		
		this.gameEvent  = new GameEventImpl();
		this.boardEvent = new BoardEventImpl();
		this.state = GAME_ON;
	}
	
	/**
	 * 
	 */
	public void gameOn() throws SimulationException {
		
		if (this.names[0] == null || this.names[1] == null) {
			throw new SimulationException(
					"There are missing players to be registered");
		}
		
		if (listener != null) listener.gameStateChanged(gameEvent);
		
		new Thread() {
			
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				MAIN:
				while (!Thread.interrupted()) {
					try {
						
						boardLock.lock();
						Arrays.sort(commands, comparator);
						CYCLE: {
							for (Move move : commands) {
								if (move == null) continue;
								
								try {
									state = board.doMove(move);
								} catch (SimulationException e) {
									if (listener != null) listener.onException(e);
									continue;
								}
								
								switch (state) {
								case GAME_ON:
									if (listener != null)
										listener.boardStateChanged(boardEvent);
									break CYCLE;
								case GAME_FINISHED:
									if (listener != null)
										listener.gameStateChanged(gameEvent);
									this.interrupt();
									break MAIN;
								case SCORE_CHANGED:
									if (listener != null)
										listener.gameStateChanged(gameEvent);
									break CYCLE;
								}
							}
							if (listener != null)
								listener.boardStateChanged(boardEvent);
						}
					
					} finally {
						cycle++;
						Arrays.fill(commands, null);
						boardLock.unlock();
					}
					
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
	}
	
	/**
	 * @param command
	 */
	public void append(Move command) {
		if (this.boardLock.tryLock()) {
			try {
				this.commands[command.getSource().ordinal()] = command;
			} finally {
				this.boardLock.unlock();
			}
		}
	}
	
	/**
	 * @param listener
	 */
	public void setListener(SimulationListener listener) {
		this.listener = listener;
	}
	
	/**
	 * @param name
	 */
	public void register(Player player, String name) {
		if (name == null)
			throw new NullPointerException("name cannot be null");
		this.names[player.ordinal()] = name;
	}
	
	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	private final class GameEventImpl implements GameEvent {
		
		@Override
		public int getCycle() {
			return cycle;
		}

		@Override
		public GameState getGameState() {
			return state;
		}

		@Override
		public int getMinesLeft() throws IllegalStateException {
			if (state == GAME_ON)
				throw new IllegalStateException();
			return board.getMineCount();
		}

		@Override
		public int getMineCount(Player player) throws IllegalStateException {
			if (state != GAME_FINISHED)
				throw new IllegalStateException();
			return board.getMineCount(player);
		}

		@Override
		public Player getWinner() throws IllegalStateException {
			if (state != GAME_FINISHED)
				throw new IllegalStateException();
			
			Player player = board.getDeadPlayer();
			if (player == Player.NONE) {
				int p1 = board.getMineCount(Player.PLAYER_1);
				int p2 = board.getMineCount(Player.PLAYER_2);
				
				if (p1 > p2) return Player.PLAYER_1;
				if (p1 < p2) return Player.PLAYER_2;
				return Player.NONE;
			} else {
				return Player.values()[player.ordinal() ^ 0x0001];
			}
		}
	}
	
	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	private final class BoardEventImpl implements BoardEvent {
		
		@Override
		public int getCycle() {
			return cycle;
		}

		@Override
		public Cell[][] getBoard() {
			return board.getCells();
		}

		@Override
		public int getRows() {
			return board.getCells().length;
		}

		@Override
		public int getCols() {
			return board.getCells()[0].length;
		}
	}
}