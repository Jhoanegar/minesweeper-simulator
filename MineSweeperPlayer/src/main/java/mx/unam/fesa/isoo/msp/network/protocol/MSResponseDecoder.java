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
package mx.unam.fesa.isoo.msp.network.protocol;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mx.unam.fesa.isoo.mss.core.BoardEvent;
import mx.unam.fesa.isoo.mss.core.Cell;
import mx.unam.fesa.isoo.mss.core.Cell.State;
import mx.unam.fesa.isoo.mss.core.GameEvent;
import mx.unam.fesa.isoo.mss.core.GameEvent.GameState;
import mx.unam.fesa.isoo.mss.core.Player;
import mx.unam.fesa.isoo.mss.network.protocol.MSResponseRegister;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class MSResponseDecoder extends ProtocolDecoderAdapter {

	/* */
	private static final int MAX_MESSAGE_LENGTH = 1930;
	/* */
	private static final Logger LOGGER = LoggerFactory.getLogger(MSResponseDecoder.class);

	/* */
	private static final String BOARD_CELL_COVERED = "C";
	/* */
	private static final String BOARD_CELL_FLAGGED = "F";
	/* */
	private static final String BOARD_CELL_REVEALED_EMPTY = "E";
	/* */
	private static final String BOARD_CELL_REVEALED_MINE = "M";

	/* */
	private static final String GAME_FINISHED_TIE = "TIE";
	/* */
	private static final String GAME_FINISHED_WINNER_P1 = "WP1";
	/* */
	private static final String GAME_FINISHED_WINNER_P2 = "WP2";

	/* */
	private static final String PLAYER_1 = "P1";

	/* */
	private final Matcher boardMatcher = Pattern.compile("\\(BE (\\d|[1-9]\\d*) ([1-9]\\d?) ([1-9]\\d?) ((?:C|(?:P1|P2)[FEM[1-8]])(?: (?:C|(?:P1|P2)[FEM[1-8]]))*)\\)").matcher("");
	/* */
	private final Matcher gameOnMatcher = Pattern.compile("\\(GE (\\d|[1-9]\\d*) ON\\)").matcher("");
	/* */
	private final Matcher gameScoreMatcher = Pattern.compile("\\(GE (\\d|[1-9]\\d*) SCORE (\\d|[1-9]\\d*)\\)").matcher("");
	/* */
	private final Matcher gameFinishedMatcher = Pattern.compile("\\(GE (\\d|[1-9]\\d*) FIN (TIE|WP1|WP2) (\\d|[1-9]\\d*) (\\d|[1-9]\\d*) (\\d|[1-9]\\d*)\\)").matcher("");
	/* */
	private final Matcher registerMatcher = Pattern.compile("\\(REG (?:(OK) (P1|P2)|(NO) \"((?:\\w| ){0,30})\")\\)").matcher("");

	/* */
	private CharBuffer charBuffer = CharBuffer.allocate(MAX_MESSAGE_LENGTH);
	/* */
	private CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
	/* */
	private BoardEventImpl boardEvent = null;
	/* */
	private GameEventImpl gameEvent = null;

	
	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolDecoder#decode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput)
	 */
	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
			throws Exception {

		//
		// clearing state
		//

		this.decoder.reset();
		this.charBuffer.clear();

		//
		// decoding bytes to string
		//

		byte data[] = new byte[in.limit()];
		in.get(data);
		this.decoder.decode(ByteBuffer.wrap(data), this.charBuffer, true);

		try {
			this.decoder.flush(this.charBuffer).throwException();
		} catch (BufferOverflowException | CharacterCodingException e) {
			throw new MSResponseDecondingException(
					"Error decoding request bytes", e);
		} catch (BufferUnderflowException e) {
		}

		String message = this.charBuffer.flip().toString();

		//
		// decoding String to pojo
		//

		if (this.boardMatcher.reset(message) != null
				&& this.boardMatcher.lookingAt()) {

			//
			// string matched a MSResponseBoardState message
			//

			int cycle = Integer.parseInt(this.boardMatcher.group(1));
			int rows = Integer.parseInt(this.boardMatcher.group(2));
			int cols = Integer.parseInt(this.boardMatcher.group(3));
			String[] cells = this.boardMatcher.group(4).split(" ");

			if (this.boardEvent == null) {
				this.boardEvent = new BoardEventImpl(rows, cols);
			}

			CellImpl cell;
			int i = 0, j = 0;
			for (String string : cells) {
				cell = this.boardEvent.board[i++][j++];
				if (string.equals(BOARD_CELL_COVERED)) {
					cell.state = Cell.State.COVERED;
				} else {
					cell.owner = string.startsWith(PLAYER_1)
							? Player.PLAYER_1 : Player.PLAYER_2;

					if (string.substring(2).equals(BOARD_CELL_FLAGGED)) {
						cell.state = State.FLAGGED;
					} else {
						cell.state = State.REVEALED;

						if (string.substring(2).equals(
								BOARD_CELL_REVEALED_EMPTY)) {
							cell.contentType = Cell.ContentType.EMPTY;
						} else if (string.substring(2).equals(
								BOARD_CELL_REVEALED_MINE)) {
							cell.contentType = Cell.ContentType.MINE;
						} else {
							cell.contentType = Cell.ContentType.MINE_COUNT;
							cell.mineCount = Byte.parseByte(string.substring(2));
						}
					}
				}
				i %= rows;
				j %= cols;
			}
			this.boardEvent.cycle = cycle;
			out.write(this.boardEvent);

		} else if (this.gameScoreMatcher.reset(message) != null
				&& this.gameScoreMatcher.lookingAt()) {

			//
			// string matched a game state message
			//
			
			if (this.gameEvent == null) {
				this.gameEvent = new GameEventImpl();
			}
			
			this.gameEvent.gameState = GameState.SCORE_CHANGED;
			this.gameEvent.cycle = Integer.parseInt(this.gameScoreMatcher.group(1));
			this.gameEvent.minesLeft = Integer.parseInt(this.gameScoreMatcher.group(2));
			
			out.write(gameEvent);

		} else if (this.gameFinishedMatcher.reset(message) != null
				&& this.gameFinishedMatcher.lookingAt()) {
			
			//
			// string matched a game state message
			//
			
			if (this.gameEvent == null) {
				this.gameEvent = new GameEventImpl();
			}
			
			this.gameEvent.gameState = GameState.GAME_FINISHED;
			this.gameEvent.cycle = Integer.parseInt(this.gameFinishedMatcher.group(1));
			switch(this.gameFinishedMatcher.group(2)) {
			case GAME_FINISHED_TIE:
				this.gameEvent.winner = Player.NONE;
				break;
			case GAME_FINISHED_WINNER_P1:
				this.gameEvent.winner = Player.PLAYER_1;
				break;
			case GAME_FINISHED_WINNER_P2:
				this.gameEvent.winner = Player.PLAYER_2;
				break;
			}
			this.gameEvent.minesLeft = Integer.parseInt(this.gameFinishedMatcher.group(3));
			this.gameEvent.mineCount[Player.PLAYER_1.ordinal()] = Integer.parseInt(this.gameFinishedMatcher.group(4));
			this.gameEvent.mineCount[Player.PLAYER_2.ordinal()] = Integer.parseInt(this.gameFinishedMatcher.group(5));
			
			out.write(gameEvent);
			
		} else if (this.gameOnMatcher.reset(message) != null
				&& this.gameOnMatcher.lookingAt()) {
			
			//
			// string matched a game state message
			//
			
			if (this.gameEvent == null) {
				this.gameEvent = new GameEventImpl();
			}
			
			this.gameEvent.gameState = GameState.GAME_ON;
			this.gameEvent.cycle = Integer.parseInt(this.gameOnMatcher.group(1));
			
			out.write(gameEvent);
			
		} else if (this.registerMatcher.reset(message) != null
				&& this.registerMatcher.lookingAt()) {

			//
			// string matched a register message
			//

			MSResponseRegister register = new MSResponseRegister();
			if (this.registerMatcher.group(1) != null) {
				register.setAccept(true);
				register.setPlayer(this.registerMatcher.group(2).equals(
						PLAYER_1) ? Player.PLAYER_1 : Player.PLAYER_2);
			} else {
				register.setAccept(false);
				register.setMessage(this.registerMatcher.group(4));
			}
			out.write(register);
		} else {
			throw new MSResponseDecondingException("Error decoding String "
					+ message + " to a valid message");
		}

		LOGGER.debug("Command received: {}", message);
	}
	
	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	private static final class GameEventImpl implements GameEvent {

		/* */
		int cycle = 0;
		/* */
		int minesLeft;
		/* */
		int mineCount[] = new int[2];
		/* */
		Player winner;
		/* */
		GameState gameState = GameState.GAME_ON;
		
		@Override
		public int getCycle() {
			return cycle;
		}

		@Override
		public GameState getGameState() {
			return gameState;
		}
		
		@Override
		public int getMinesLeft() throws IllegalStateException {
			return this.minesLeft;
		}

		@Override
		public Player getWinner() throws IllegalStateException {
			return this.winner;
		}

		@Override
		public int getMineCount(Player player) throws IllegalStateException {
			return this.mineCount[player.ordinal()];
		}
	}
	
	/**
	 * @author Carlos Alegría Galicia
	 *
	 */
	private static final class BoardEventImpl implements BoardEvent {
		
		/* */
		CellImpl board[][] = null;
		/* */
		int cycle = 0;

		/**
		 * @param rows
		 * @param cols
		 */
		public BoardEventImpl(int rows, int cols) {
			this.board = new CellImpl[rows][cols];
			
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[0].length; j++) {
					board[i][j] = new CellImpl();
				}
			}
		}
		
		@Override
		public int getCycle() {
			return this.cycle;
		}

		@Override
		public Cell[][] getBoard() {
			return this.board;
		}

		@Override
		public int getRows() {
			return this.board.length;
		}

		@Override
		public int getCols() {
			return this.board[0].length;
		}
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
	}
}