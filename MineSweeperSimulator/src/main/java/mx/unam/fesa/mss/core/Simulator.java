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

import java.lang.Thread.State;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mx.unam.fesa.mss.core.GameEvent.GameState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Alegría Galicia
 *
 */
public final class Simulator {
	
	/* */
	public static final int MIN_ROWS = 4;
	/* */
	public static final int MAX_ROWS = 16;
	/* */
	public static final int MIN_COLS = 8;
	/* */
	public static final int MAX_COLS = 30;
	
	/* */
	private static final int CYCLE_LENGTH = 150;
	/* */
	private static final Logger LOGGER = LoggerFactory.getLogger(Simulator.class);
	
	/* */
	private final Board board;
	/* */
	private final Lock boardLock;
	/* */
	private final String names[];
	/* */
	private final Move moves[];
	/* the comparator used to*/
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
	
	/* */
	private final Thread thread;
	
		
	public Simulator() {
		
		// use a pseudo random generator to obtain rows in [MIN_ROWS, MAX_ROWS],
		// cols in [MIN_COLS, MAX_COLS], and mines in [1, cols * rows / 2]
		//
		Random random = new Random();
		int rows = MIN_ROWS + random.nextInt(MAX_ROWS - MIN_ROWS);
		int cols = MIN_COLS + random.nextInt(MAX_COLS - MIN_COLS);
		int mines = 1 + random.nextInt((cols * rows) >> 2);
		
		this.board = new Board(rows, cols, mines);
		this.boardLock = new ReentrantLock();
		this.names = new String[2]; Arrays.fill(this.names, null);
		this.moves = new Move[2]; Arrays.fill(this.moves, null);
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
		this.thread = new Thread(new GameSimulation());
	}
	
	/**
	 * 
	 */
	public void start() throws SimulationException {
		
		// thread has already been started
		//
		if (this.thread.getState() != State.NEW) return;
		
		// missing player to be registered
		//
		if (this.names[0] == null || this.names[1] == null)
			throw new SimulationException(
					"There are missing players to be registered");
		
		// everything ok, sending the first status
		//
		if (listener != null) listener.gameStateChanged(gameEvent);
		
		// starting thread
		//
		this.thread.start();
		LOGGER.info("Simulator started: {} x {} and "
				+ this.board.getMineCount() + "mines",
				this.board.getCells().length,
				this.board.getCells()[0].length);
	}
	
	/**
	 * @param listener
	 */
	public void stop() {
		
		// the thread is not alive
		//
		if (!this.thread.isAlive()) return;
		
		this.thread.interrupt();
		try {
			this.thread.join();
		} catch (InterruptedException e) {
		}
		LOGGER.info("Simulator stopped: {} on cycle {}", state, cycle);
	}
	
	/**  
	 * @param move
	 */
	public void append(Move move) throws SimulationException {
		if (this.boardLock.tryLock()) {
			try {
				this.moves[move.getSource().ordinal()] = move;
				LOGGER.info("Move {} successfully appended", move);
			} finally {
				this.boardLock.unlock();
			}
		} else {
			LOGGER.info("Move {} missed the execution cycle", move);
			throw new SimulationException("Move missed the execution cycle");
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
			throw new IllegalArgumentException("name cannot be null");
		this.names[player.ordinal()] = name;
		LOGGER.info("'{}' successfully registered as {}", name, player);
	}
	
	
	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	private final class GameSimulation implements Runnable {
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				
				// send board notifications to mark the start of the cycle
				//
				LOGGER.info("Board state: {}", boardEvent);
				if (listener != null)
					listener.boardStateChanged(boardEvent);
				
				// wait for players moves
				//
				try {
					Thread.sleep(CYCLE_LENGTH);
				} catch (InterruptedException e) {
					return;
				}
				
				// execute received moves at the end of the cycle
				//
				try {
					boardLock.lock();
					
					// moves execution
					//
					Arrays.sort(moves, comparator);
					for (Move move : moves) {
						if (move == null) break;

						try {
							state = board.doMove(move);
						} catch (SimulationException e) {
							if (listener != null) listener.onException(e);
							LOGGER.info("Could not execute move {}: {}",
									move, e.getMessage());
							continue;
						}
						
						LOGGER.info(
								"Move {} successfully executed. Result: {}",
								move, state);
						
						switch (state) {
						case GAME_FINISHED:
							LOGGER.info("GameState: {}", boardEvent);
							if (listener != null) {
								listener.gameStateChanged(gameEvent);
								listener.boardStateChanged(boardEvent);
							}
							
							// finishing the game
							//
							return;
						case SCORE_CHANGED:
							LOGGER.info("GameState: {}", boardEvent);
							if (listener != null)
								listener.gameStateChanged(gameEvent);
							break;
						default:
						}
					}
				} finally {
					cycle++;
					Arrays.fill(moves, null);
					boardLock.unlock();
				}
			}
		}
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
		public int getMinesLeft() {
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

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			switch (state) {
			case GAME_ON:
				return "(GE " + cycle + " ON)";
			case GAME_FINISHED:
				return "(GE " + cycle + " FIN " + getWinner()
						+ " " + getMinesLeft()
						+ " " + getMineCount(Player.PLAYER_1) 
						+ " " + getMineCount(Player.PLAYER_2) + ")";
			case SCORE_CHANGED:
				return "(GE " + cycle + " SCORE " + getMinesLeft() + ")";
			default:
				return "";
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

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String event = "(BE " + cycle + " " + getRows() + " " + getCols();
			for (Cell[] row : board.getCells()) {
				for (Cell cell : row) {
					event += " " + cell;
				}
			}
			return event + ")";
		}
	}
}