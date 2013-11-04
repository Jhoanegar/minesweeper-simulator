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
package mx.unam.fesa.isoo.msp;

import static org.apache.mina.statemachine.event.IoHandlerEvents.EXCEPTION_CAUGHT;
import static org.apache.mina.statemachine.event.IoHandlerEvents.MESSAGE_RECEIVED;
import static org.apache.mina.statemachine.event.IoHandlerEvents.SESSION_OPENED;

import java.util.Random;

import mx.unam.fesa.isoo.msp.network.protocol.MSResponseDecondingException;
import mx.unam.fesa.mss.core.BoardEvent;
import mx.unam.fesa.mss.core.Cell;
import mx.unam.fesa.mss.core.GameEvent;
import mx.unam.fesa.mss.core.Move;
import mx.unam.fesa.mss.core.Move.Type;
import mx.unam.fesa.mss.network.protocol.MSRequestRegister;
import mx.unam.fesa.mss.network.protocol.MSResponseRegister;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.statemachine.StateControl;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.statemachine.annotation.State;
import org.apache.mina.statemachine.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Alegría Galicia
 *
 */
public class MSPlayer {
	
	@State public static final String ROOT = "Root";
	@State(ROOT) public static final String REGISTER = "Register";
	@State(ROOT) public static final String GAME_ON = "Game on";
	@State(ROOT) public static final String GAME_FINISHED = "Game finished";
	
	/* */
	private static final Logger LOGGER = LoggerFactory.getLogger(MSPlayer.class);
	/* */
	private Move move = null;
	/* */
	private Random random = new Random();
	
	
	/**
	 * 
	 */
	public MSPlayer() {
		move = new Move();
	}
	
	@IoHandlerTransition(on = SESSION_OPENED, in = REGISTER)
	public void onRegister(IoSession session) {
		MSRequestRegister register = new MSRequestRegister();
		register.setName("Jugador");
		session.write(register);
		
		LOGGER.debug("Registering \"Jugador\" ...");
	}
	
	@IoHandlerTransition(on = MESSAGE_RECEIVED, in = REGISTER)
	public void onRegister(MSResponseRegister register) {
		if (register.isAccept()) {
			LOGGER.debug("Jugador registrado exitosamente");
			StateControl.breakAndCallNext(GAME_ON);
		} else {
			LOGGER.debug("registro rechazado");
			StateControl.breakAndCallNow(ROOT);
		}
	}
	
	@IoHandlerTransition(on = MESSAGE_RECEIVED, in = GAME_ON)
	public void onGameEvent(GameEvent gameEvent) {
		switch (gameEvent.getGameState()) {
		case GAME_ON:
			LOGGER.info("Cycle {}: Game On", gameEvent.getCycle());
			break;
		case SCORE_CHANGED:
			LOGGER.info("Cycle {}: Score changed, mines {}",
					gameEvent.getCycle(), gameEvent.getMinesLeft());
			StateControl.breakAndCallNext(GAME_FINISHED);
			break;
		case GAME_FINISHED:
			LOGGER.info("Cycle {}: Game finished, winner {}",
					gameEvent.getCycle(), gameEvent.getWinner());
			break;
		}
	}
	
	@IoHandlerTransition(on = MESSAGE_RECEIVED, in = GAME_ON)
	public void onBoard(IoSession session, BoardEvent boardEvent) {

		Cell board[][] = boardEvent.getBoard();
		move.setRow(random.nextInt(board.length));
		move.setCol(random.nextInt(board[0].length));
		move.setType(Type.values()[random.nextInt(3)]);
		
		session.write(move);
	}
	
	@IoHandlerTransition(on = EXCEPTION_CAUGHT, in = ROOT)
	public void onDecondingException(MSResponseDecondingException e) {
		LOGGER.info("Deconding exception caught, response ignored", e);
	}
	
	@IoHandlerTransition(on = EXCEPTION_CAUGHT, in = ROOT, weight = 10)
	public void onException(Exception e) {
		LOGGER.info("Exception caught", e);
	}

	@IoHandlerTransition(in = ROOT, weight=100)
	public void unhandledEvents(Event event) {
		LOGGER.debug("Unhandled event: " + event.getId());
	}
}