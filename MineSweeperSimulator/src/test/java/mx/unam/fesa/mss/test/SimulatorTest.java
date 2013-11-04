package mx.unam.fesa.mss.test;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.LogManager;

import mx.unam.fesa.mss.core.BoardEvent;
import mx.unam.fesa.mss.core.GameEvent;
import mx.unam.fesa.mss.core.Move;
import mx.unam.fesa.mss.core.Player;
import mx.unam.fesa.mss.core.SimulationException;
import mx.unam.fesa.mss.core.SimulationListener;
import mx.unam.fesa.mss.core.Simulator;
import mx.unam.fesa.mss.core.Cell.State;
import mx.unam.fesa.mss.core.GameEvent.GameState;
import mx.unam.fesa.mss.core.Move.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Carlos Alegría Galicia
 *
 */
public class SimulatorTest implements SimulationListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorTest.class);
	
	private static final String DEFAULT_LOGGING_CONFIG_FILE = "logging.properties";
	private static final String CORRECT = "Correct";
	private static final String INCORRECT = "Incorrect";
	private static final String ONE_MOVE = "One Move";
	private static final String ONE_GAME_DISCOVERED = "One Game Discovered";
	private static final String ONE_GAME_FLAGGED = "One Game Flagged";
	private static final String TWO_PLAYER_GAME = "Two Player Game";
	
	private static final long AWAIT_MILLIS = 200;
	
	private Move move;
	private Simulator simulator;
	private GameEvent gameEvent;
	private BoardEvent boardEvent;
	private SimulationException exception;
	
	private Lock eventLock = new ReentrantLock();
	private Condition gameEventOcurred = eventLock.newCondition();
	private Condition boardEventOcurred = eventLock.newCondition();
	private Condition exceptionOcurred = eventLock.newCondition();
	

	@Test(groups = {CORRECT, ONE_MOVE}, enabled = false)
	//@Test(groups = {CORRECT, ONE_MOVE})
	public void correctSetFlag() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			setFlag();
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {CORRECT, ONE_MOVE}, dependsOnMethods = {"correctSetFlag"}, enabled = false)
	//@Test(groups = {CORRECT, ONE_MOVE}, dependsOnMethods = {"correctSetFlag"})
	public void correctRemoveFlag() {
		
		move.setType(Type.REMOVE_FLAG);
		appendMove(move);
		
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}

		try {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
			
			int cycle = this.boardEvent.getCycle();
			
			do {
				try {
					this.boardEventOcurred.await();
				} catch (InterruptedException e) {
				}
			} while (cycle >= this.boardEvent.getCycle());
			
			assert this.boardEvent.getBoard()[move.getRow()][move.getCol()].getState() == State.COVERED;
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {CORRECT, ONE_MOVE}, dependsOnMethods = {"correctRemoveFlag"}, enabled = false)
	//@Test(groups = {CORRECT, ONE_MOVE}, dependsOnMethods = {"correctRemoveFlag"})
	public void correctUncover() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
			
			move = getRandomMove(
					this.boardEvent.getRows(), this.boardEvent.getCols());
			move.setType(Type.UNCOVER);
			appendMove(move);
			
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
			
			int cycle = this.boardEvent.getCycle();
			
			do {
				try {
					this.boardEventOcurred.await();
				} catch (InterruptedException e) {
				}
			} while (cycle >= this.boardEvent.getCycle());
			
			assert this.boardEvent.getBoard()[move.getRow()][move.getCol()].getState() == State.REVEALED;
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT}, enabled = false)
	//@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT})
	public void incorrectRows() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
			
			move.setRow(this.boardEvent.getRows() + 1);
			move.setCol(this.boardEvent.getCols() + 1);
			move.setType(Type.UNCOVER);
			appendMove(move);
			
			try {
				assert this.exceptionOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
			
			assert this.exception.getCause() instanceof IndexOutOfBoundsException;
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT}, enabled = false)
	//@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT})
	public void incorrectSetFlagFlagged() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			setFlag();
			appendMove(move);
			
			try {
				assert this.exceptionOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT}, enabled = false)
	//@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT})
	public void incorrectSetFlagUncovered() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			uncover();
			move.setType(Type.SET_FLAG);
			appendMove(move);
			
			try {
				assert this.exceptionOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT}, enabled = false)
	//@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT})
	public void incorrectRemoveFlagWrongState() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			uncover();
			move.setType(Type.REMOVE_FLAG);
			appendMove(move);
			
			try {
				assert this.exceptionOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT}, enabled = false)
	//@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT})
	public void incorrectRemoveFlagWrongPlayer() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			setFlag();
			move.setSource(Player.values()[move.getSource().ordinal() ^ 0x0001]);
			move.setType(Type.REMOVE_FLAG);
			appendMove(move);
			
			try {
				assert this.exceptionOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT}, enabled = false)
	//@Test(groups = {INCORRECT, ONE_MOVE}, dependsOnGroups = {CORRECT})
	public void incorrectUncoverWrongState() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		try {
			setFlag();
			move.setType(Type.UNCOVER);
			appendMove(move);
			
			try {
				assert this.exceptionOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		} finally {
			this.eventLock.unlock();
		}
	}
	
	@Test(groups = {ONE_GAME_DISCOVERED}, dependsOnGroups = {ONE_MOVE}, enabled = false)
	//@Test(groups = {ONE_GAME_DISCOVERED}, dependsOnGroups = {ONE_MOVE})
	public void mineDiscovered() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		int rows, cols = 0;
		try {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
			
			rows = this.boardEvent.getRows();
			cols = this.boardEvent.getCols();
		} finally {
			this.eventLock.unlock();
		}
		
		Move move = new Move();
		move.setSource(Player.PLAYER_1);
		move.setType(Type.UNCOVER);
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				
				move.setRow(i);
				move.setCol(j);
				appendMove(move);
				
				try {
					while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e1) {
				}
				
				try {
					try {
						if (!this.gameEventOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS)) {
							continue;
						}
					} catch (InterruptedException e) {
					}
				
					if (this.gameEvent.getGameState() == GameState.GAME_FINISHED) {
						assert this.gameEvent.getWinner() != Player.PLAYER_1;
						LOGGER.info("Player 1 exploted!");
						return;
					}
				} finally {
					this.eventLock.unlock();
				}
			}
		}
		assert false;
	}
	
	@Test(groups = {ONE_GAME_FLAGGED}, dependsOnGroups = {ONE_MOVE}, enabled = false)
	//@Test(groups = {ONE_GAME_FLAGGED}, dependsOnGroups = {ONE_MOVE})
	public void minesFlagged() {
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		int rows, cols = 0;
		try {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
			
			rows = this.boardEvent.getRows();
			cols = this.boardEvent.getCols();
		} finally {
			this.eventLock.unlock();
		}
		
		Move move = new Move();
		move.setSource(Player.PLAYER_1);
		move.setType(Type.SET_FLAG);
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				move.setRow(i);
				move.setCol(j);
				appendMove(move);
				
				try {
					while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e1) {
				}
				
				try {
					try {
						if (!this.gameEventOcurred.await(AWAIT_MILLIS, TimeUnit.MILLISECONDS)) {
							continue;
						}
					} catch (InterruptedException e) {
					}
				
					if (this.gameEvent.getGameState() == GameState.GAME_FINISHED) {
						assert this.gameEvent.getWinner() == Player.PLAYER_1;
						assert this.gameEvent.getMinesLeft() == 0;
						LOGGER.info("Player 1 won!");
						return;
					}
				} finally {
					this.eventLock.unlock();
				}
			}
		}
		assert false;
	}

	//@Test(groups = {TWO_PLAYER_GAME}, dependsOnGroups = {ONE_MOVE}, enabled = false)
	@Test(groups = {TWO_PLAYER_GAME})
	public void twoPlayerGame() {
		
		//
		// obtain rows and cols from BoardEvent
		//
		
		try {
			while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e1) {
		}
		
		int rows, cols = 0;
		try {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
			
			rows = this.boardEvent.getRows();
			cols = this.boardEvent.getCols();
		} finally {
			this.eventLock.unlock();
		}
		
		//
		// make 100 moves
		//
		
		int cycle = 0;
		while (cycle++ <= 100) {
			Move move = null;
			
			// append PLAYER_1 command
			//
			try {
				move = getRandomMove(Player.PLAYER_1, rows, cols);
				this.simulator.append(move);
			} catch (SimulationException e) {
				LOGGER.info("Command " + move + " ignored", e);
			}
			LOGGER.info("Command appended: {}", move);
			
			// append PLAYER_2 command
			//
			try {
				move = getRandomMove(Player.PLAYER_2, rows, cols);
				this.simulator.append(move);
			} catch (SimulationException e) {
				LOGGER.info("Command " + move + " ignored", e);
			}
			LOGGER.info("Command appended: {}", move);
			
			// check if the game finished because of the moves
			//
			try {
				while (!this.eventLock.tryLock(30, TimeUnit.MILLISECONDS));
					
				if (this.gameEventOcurred.await(
						AWAIT_MILLIS, TimeUnit.MILLISECONDS)
						&& this.gameEvent.getGameState() == GameState.GAME_FINISHED) {
					return;
				}
			} catch (InterruptedException e) {
			} finally {
				this.eventLock.unlock();
			}
		}
	}
	
	@BeforeGroups(groups = {ONE_MOVE, ONE_GAME_DISCOVERED, ONE_GAME_FLAGGED, TWO_PLAYER_GAME})
	public void beforeGroups() {

		// creating simulator
		//
		this.simulator = new Simulator();
		this.simulator.setListener(this);

		// starting game
		//
		this.simulator.register(Player.PLAYER_1, "Jugador 1");
		this.simulator.register(Player.PLAYER_2, "Jugador 2");
		this.simulator.start();
	}
	
	@BeforeTest
	public void beforeTest() {
		try {
			LogManager.getLogManager().readConfiguration(
					ClassLoader.getSystemResourceAsStream(DEFAULT_LOGGING_CONFIG_FILE));
		} catch (Exception e) {
			throw new Error("Could not load logging properties file", e);
		}
	}
	
	@AfterGroups(groups = {ONE_MOVE, ONE_GAME_DISCOVERED, ONE_GAME_FLAGGED})
	public void afterGroups() {
		// ending game
		//
		this.simulator.stop();
		LOGGER.info("Server stopped");
	}

	@Override
	public void onException(SimulationException e) {
		LOGGER.info("received exception", e);
		
		this.eventLock.lock();
		try {
			this.exception = e;
			this.exceptionOcurred.signal();
		} finally {
			this.eventLock.unlock();
		}
	}

	@Override
	public void gameStateChanged(GameEvent event) {
		LOGGER.info("received GameEvent {}", event);
		
		this.eventLock.lock();
		try {
			this.gameEvent = event;
			this.gameEventOcurred.signal();
		} finally {
			this.eventLock.unlock();
		}
	}

	@Override
	public void boardStateChanged(BoardEvent event) {
		LOGGER.info("received BoardEvent {}", event);
		
		this.eventLock.lock();
		try {
			this.boardEvent = event;
			this.boardEventOcurred.signal();
		} finally {
			this.eventLock.unlock();
		}
	}
	
	/**
	 * @return
	 */
	private Move getRandomMove(int rows, int cols) {
		Move move = new Move();
		Random random = new Random();

		move.setType(Type.values()[random.nextInt(3)]);
		move.setSource(Player.values()[random.nextInt(2)]);
		move.setRow(random.nextInt(rows));
		move.setCol(random.nextInt(cols));

		return move;
	}
	
	/**
	 * @return
	 */
	private Move getRandomMove(Player player, int rows, int cols) {
		Random random = new Random();
		
		Move move = new Move();
		move.setType(Type.values()[random.nextInt(3)]);
		move.setTimeStamp(System.currentTimeMillis());
		move.setRow(random.nextInt(rows));
		move.setCol(random.nextInt(cols));
		move.setSource(player);

		return move;
	}
	
	/**
	 * @param move
	 */
	private void appendMove(Move move) {
		while (true) {
			try {
				this.simulator.append(move);
				break;
			} catch (SimulationException e) {
				LOGGER.info("Command " + move, e);
				try {
					Thread.sleep(30);
				} catch (InterruptedException e1) {
				}
			}	
		}
		LOGGER.info("Move " + move + " appended");
	}
	
	private void setFlag() {
		try {
			this.boardEventOcurred.await();
		} catch (InterruptedException e) {
		}
		
		move = getRandomMove(
				this.boardEvent.getRows(), this.boardEvent.getCols());
		move.setType(Type.SET_FLAG);
		appendMove(move);
		
		try {
			this.boardEventOcurred.await();
		} catch (InterruptedException e) {
		}
		
		int cycle = this.boardEvent.getCycle();
		
		do {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
		} while (cycle >= this.boardEvent.getCycle());
		
		assert this.boardEvent.getBoard()[move.getRow()][move.getCol()].getState() == State.FLAGGED;
	}
	
	private void uncover() {
		try {
			this.boardEventOcurred.await();
		} catch (InterruptedException e) {
		}
		
		move = getRandomMove(
				this.boardEvent.getRows(), this.boardEvent.getCols());
		move.setType(Type.UNCOVER);
		appendMove(move);
		
		try {
			this.boardEventOcurred.await();
		} catch (InterruptedException e) {
		}
		
		int cycle = this.boardEvent.getCycle();
		
		do {
			try {
				this.boardEventOcurred.await();
			} catch (InterruptedException e) {
			}
		} while (cycle >= this.boardEvent.getCycle());
		
		assert this.boardEvent.getBoard()[move.getRow()][move.getCol()].getState() == State.REVEALED;
	}
}