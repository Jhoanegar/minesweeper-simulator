package mx.unam.fesa.mss.test;

import java.util.Random;

import mx.unam.fesa.mss.core.Board;
import mx.unam.fesa.mss.core.Move;
import mx.unam.fesa.mss.core.Player;
import mx.unam.fesa.mss.core.SimulationException;
import mx.unam.fesa.mss.core.Simulator;
import mx.unam.fesa.mss.core.Cell.State;
import mx.unam.fesa.mss.core.GameEvent.GameState;
import mx.unam.fesa.mss.core.Move.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class BoardTest {

	private final static Logger LOGGER = LoggerFactory.getLogger(BoardTest.class);
	private final static String GROUP_WRONG_COMMAND = "Wrong command";
	private final static String GROUP_RIGHT_COMMAND = "Right command";

	private Board board = null;
	private int rows;
	private int cols;
	private int mines;

	@Test
	public void testMembers() {
		assert this.board.getMinesNumber() == this.mines;
		assert this.board.getMineCount() == this.mines;
		assert this.board.getMineCount(Player.PLAYER_1) == 0;
		assert this.board.getMineCount(Player.PLAYER_2) == 0;
		assert this.board.getCells().length == this.rows;
		assert this.board.getCells()[0].length == this.cols;
	}

	@Test(groups = { GROUP_RIGHT_COMMAND })
	public void testRightSF() {
		Move move = getRandomMove();
		move.setType(Type.SET_FLAG);
		this.board.doMove(move);
		assert this.board.getCells()[move.getRow()][move.getCol()].getState() == State.FLAGGED;
	}
	
	@Test(groups = { GROUP_RIGHT_COMMAND }, dependsOnMethods = {"testRightSF"})
	public void testRightRF() {
		Move move = getRandomMove();
		move.setType(Type.SET_FLAG);
		this.board.doMove(move);
		move.setType(Type.REMOVE_FLAG);
		this.board.doMove(move);
		assert this.board.getCells()[move.getRow()][move.getCol()].getState() == State.COVERED;
	}
	
	@Test(groups = { GROUP_RIGHT_COMMAND })
	public void testRightUN() {
		Move move = getRandomMove();
		move.setType(Type.UNCOVER);
		this.board.doMove(move);
		assert this.board.getCells()[move.getRow()][move.getCol()].getState() != State.COVERED;
	}

	@Test(expectedExceptions = { SimulationException.class }, groups = { GROUP_WRONG_COMMAND }, dependsOnGroups = {GROUP_RIGHT_COMMAND})
	public void testWrongIndexes() {
		Move move = getRandomMove();
		move.setRow(-1);
		move.setCol(0);
		this.board.doMove(move);
	}

	@Test(expectedExceptions = { SimulationException.class }, groups = { GROUP_WRONG_COMMAND }, dependsOnGroups = {GROUP_RIGHT_COMMAND})
	public void testWrongRF() {
		Move move = getRandomMove();
		move.setType(Type.REMOVE_FLAG);
		this.board.doMove(move);
	}

	@Test(expectedExceptions = { SimulationException.class }, groups = { GROUP_WRONG_COMMAND }, dependsOnGroups = {GROUP_RIGHT_COMMAND})
	public void testWrongUN() {
		Move move = getRandomMove();
		move.setType(Type.SET_FLAG);

		assert this.board.doMove(move) == GameState.GAME_ON;
		move.setType(Type.UNCOVER);
		this.board.doMove(move);
	}

	@Test(expectedExceptions = { SimulationException.class }, groups = { GROUP_WRONG_COMMAND }, dependsOnGroups = {GROUP_RIGHT_COMMAND})
	public void testWrongSF() {
		Move move = getRandomMove();
		move.setType(Type.UNCOVER);

		assert this.board.doMove(move) == GameState.GAME_ON;
		move.setType(Type.SET_FLAG);
		this.board.doMove(move);
	}

	@BeforeTest
	public void beforeTest() {
		Random random = new Random();
		rows = Simulator.MIN_ROWS + random.nextInt(Simulator.MAX_ROWS - Simulator.MIN_ROWS);
		cols = Simulator.MIN_COLS + random.nextInt(Simulator.MAX_COLS - Simulator.MIN_COLS);
		mines = 1 + random.nextInt((rows * cols) >> 2);

		LOGGER.info("Using {} rows, {} cols, and " + mines + " mines", rows,
				cols);
		this.board = new Board(rows, cols, mines);
	}

	/**
	 * @return
	 */
	private Move getRandomMove() {
		Move move = new Move();
		Random random = new Random();

		move.setType(Type.values()[random.nextInt(3)]);
		move.setSource(Player.values()[random.nextInt(2)]);
		move.setRow(random.nextInt(rows));
		move.setCol(random.nextInt(cols));

		return move;
	}
}
