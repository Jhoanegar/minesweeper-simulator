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
package mx.unam.fesa.isoo.mss.network.protocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import mx.unam.fesa.isoo.mss.core.Board;
import mx.unam.fesa.isoo.mss.core.BoardEvent;
import mx.unam.fesa.isoo.mss.core.Cell;
import mx.unam.fesa.isoo.mss.core.GameEvent;
import mx.unam.fesa.isoo.mss.core.Player;
import mx.unam.fesa.isoo.mss.core.Cell.State;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class MSResponseEncoder extends ProtocolEncoderAdapter {

	/**
	 * The maximum number of chars per message
	 */
	public static final int MAX_MESSAGE_LENGTH = (Board.MAX_COLS * Board.MAX_ROWS) << 4
			+ Integer.toString(Board.MAX_COLS).length()
			+ Integer.toString(Board.MAX_ROWS).length() + 6;

	/* */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MSResponseEncoder.class);

	/* */
	private static final String BOARD_STATE_FORMAT = "(BE %1$d %2$d %3$d%4$s)";
	/* */
	private static final String BOARD_CELL_COVERED = "C";
	/* */
	private static final String BOARD_CELL_FLAGGED = "F";
	/* */
	private static final String BOARD_CELL_REVEALED_EMPTY = "E";
	/* */
	private static final String BOARD_CELL_REVEALED_MINE = "M";

	/* */
	private static final String REG_OK_FORMAT = "(REG OK %s)";
	/* */
	private static final String REG_NO_FORMAT = "(REG NO \"%s\")";
	/* */
	private static final int REG_MAX_MESSAGE_LENGTH = MAX_MESSAGE_LENGTH - 11;

	/* */
	private static final String GAME_EVENT_FINISHED_FORMAT = "(GE %1$d FIN %2$s %3$d %4$d %5$d)";
	/* */
	private static final String GAME_EVENT_SCORE_FORMAT = "(GE %1$d SCORE %2$d)";
	/* */
	private static final String GAME_EVENT_ON_FORMAT = "(GE %1$d ON)";
	/* */
	private static final String GAME_FINISHED_WINNER[];
	
	static {
		GAME_FINISHED_WINNER = new String[Player.values().length];
		GAME_FINISHED_WINNER[Player.NONE.ordinal()] = "TIE";
		GAME_FINISHED_WINNER[Player.PLAYER_1.ordinal()] = "WP1";
		GAME_FINISHED_WINNER[Player.PLAYER_2.ordinal()] = "WP2";
	}

	/* */
	private static final String PLAYER_1 = "P1";
	/* */
	private static final String PLAYER_2 = "P2";

	/* */
	private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.core.session.IoSession, java.lang.Object, org.apache.mina.filter.codec.ProtocolEncoderOutput)
	 */
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		
		//
		// clear state
		//
		
		this.encoder.reset();
		
		//
		// translating command to string
		//
		
		StringWriter stringWriter = new StringWriter(MAX_MESSAGE_LENGTH);
		PrintWriter printWriter = new PrintWriter(stringWriter, true);
		
		if (message instanceof MSResponseRegister) {
			encodeMessage((MSResponseRegister) message, printWriter);
		} else if (message instanceof GameEvent) {
			encodeMessage((GameEvent) message, printWriter);
		} else {
			encodeMessage((BoardEvent) message, printWriter);
		}
		String encodedMessage = stringWriter.toString();

		// encoding string
		//
		IoBuffer iobuffer = IoBuffer.allocate(MAX_MESSAGE_LENGTH, false);
		iobuffer.putString(encodedMessage, encoder);
		iobuffer.flip();
		out.write(iobuffer);

		LOGGER.debug("Command sent: {}", encodedMessage);
	}

	/**
	 * @param register
	 * @return
	 */
	private void encodeMessage(MSResponseRegister register, PrintWriter writer) {

		// translating command to string
		//
		if (register.isAccept()) {
			writer.format(REG_OK_FORMAT,
					register.getPlayer() == Player.PLAYER_1 ? PLAYER_1 : PLAYER_2);
		} else {
			String message = register.getMessage();
			message = message == null ? ""
					: message.length() > REG_MAX_MESSAGE_LENGTH ? message
							.substring(0, REG_MAX_MESSAGE_LENGTH) : message;
							writer.format(REG_NO_FORMAT, message);
		}
	}

	/**
	 * @param gameEvent
	 * @return
	 */
	private void encodeMessage(GameEvent gameEvent, PrintWriter writer) {
		
		// translating command to string
		//
		switch (gameEvent.getGameState()) {
		case SCORE_CHANGED:
			writer.format(GAME_EVENT_SCORE_FORMAT, gameEvent.getCycle(),
					gameEvent.getMinesLeft());
			break;
		case GAME_FINISHED:
			writer.format(GAME_EVENT_FINISHED_FORMAT, gameEvent.getCycle(),
					GAME_FINISHED_WINNER[gameEvent.getWinner().ordinal()],
					gameEvent.getMinesLeft(),
					gameEvent.getMineCount(Player.PLAYER_1),
					gameEvent.getMineCount(Player.PLAYER_2));
			break;
		case GAME_ON:
			writer.format(GAME_EVENT_ON_FORMAT, gameEvent.getCycle());
			break;
		}
	}

	/**
	 * @param board
	 * @return
	 */
	private void encodeMessage(BoardEvent boardEvent, PrintWriter writer) {

		// translating command to string
		//
		Cell [][]board = boardEvent.getBoard();
		String content = new String();
		for (Cell[] row : board) {
			for (Cell cell : row) {
				content += " ";
				if (cell.getState() == State.COVERED) {
					content += BOARD_CELL_COVERED;
					continue;
				}

				content += (cell.getOwner() == Player.PLAYER_1 ? PLAYER_1
						: PLAYER_2);
				switch (cell.getState()) {
				case FLAGGED:
					content += BOARD_CELL_FLAGGED;
					break;
				case REVEALED:
					switch (cell.getContentType()) {
					case EMPTY:
						content += BOARD_CELL_REVEALED_EMPTY;
						break;
					case MINE:
						content += BOARD_CELL_REVEALED_MINE;
						break;
					case MINE_COUNT:
						content += cell.getMineCount();
						break;
					}
				default:
					break;
				}
			}
		}
		writer.format(BOARD_STATE_FORMAT, boardEvent.getCycle(), board.length,
				board[0].length, content);
	}
}