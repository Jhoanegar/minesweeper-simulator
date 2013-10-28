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

import static org.apache.mina.statemachine.event.IoHandlerEvents.EXCEPTION_CAUGHT;
import static org.apache.mina.statemachine.event.IoHandlerEvents.MESSAGE_RECEIVED;
import static org.apache.mina.statemachine.event.IoHandlerEvents.MESSAGE_SENT;

import java.util.Random;

import mx.unam.fesa.isoo.mss.core.Board;
import mx.unam.fesa.isoo.mss.core.BoardEvent;
import mx.unam.fesa.isoo.mss.core.GameEvent;
import mx.unam.fesa.isoo.mss.core.GameEvent.GameState;
import mx.unam.fesa.isoo.mss.core.Move;
import mx.unam.fesa.isoo.mss.core.Player;
import mx.unam.fesa.isoo.mss.core.SimulationException;
import mx.unam.fesa.isoo.mss.core.SimulationListener;
import mx.unam.fesa.isoo.mss.core.Simulator;
import mx.unam.fesa.isoo.mss.network.MSServer;

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
public class MSProtocolHandler implements SimulationListener {
	
	@State public static final String ROOT = "Root";
	@State(ROOT) public static final String REGISTER_PLAYER1 = "Register Player 1";
	@State(ROOT) public static final String REGISTER_PLAYER2 = "Register Player 2";
	@State(ROOT) public static final String REGISTER_FINSHED = "Register finished";
	@State(ROOT) public static final String GAME_ON = "Game on";
	@State(ROOT) public static final String GAME_FINISHED = "Game finished";
	
	/* */
	private static final Logger LOGGER = LoggerFactory.getLogger(Simulator.class);
	
	/* */
	private Simulator simulator = null;
	/* */
	private MSServer server = null;
	
	
	/**
	 * 
	 */
	public MSProtocolHandler(MSServer server) {
		Random random = new Random();
		int rows = 1 + random.nextInt(Board.MAX_ROWS);
		int cols = 1 + random.nextInt(Board.MAX_COLS);
		int mines = 1 + random.nextInt((cols * rows) >> 2);
		this.simulator = new Simulator(rows, cols, mines);
		this.simulator.setListener(this);
		this.server = server;
	}
	
	@IoHandlerTransition(on = MESSAGE_RECEIVED, in = REGISTER_PLAYER1, next = REGISTER_PLAYER2)
	public void registerPlayer1(IoSession session, MSRequestRegister register) {
		
		// registering Player1 in MSSimulator
		//
		this.simulator.register(Player.PLAYER_1, register.getName());

		// setting attribute to connection session, for player identification
		//
		session.setAttribute(Player.class, Player.PLAYER_1);
		
		// sending ack response to Player 
		//
		MSResponseRegister responseRegister = new MSResponseRegister();
		responseRegister.setAccept(true);
		responseRegister.setPlayer(Player.PLAYER_1);
		session.write(responseRegister);
	}
	
	@IoHandlerTransition(on = MESSAGE_RECEIVED, in = REGISTER_PLAYER2, next = REGISTER_FINSHED)
	public void registerPlayer2(IoSession session, MSRequestRegister register) {
		
		// registering Player1 in MSSimulator
		//
		this.simulator.register(Player.PLAYER_2, register.getName());
		
		// setting attribute to connection session, for player identification
		//
		session.setAttribute(Player.class, Player.PLAYER_2);
		
		// sending ack response to Player 
		//
		MSResponseRegister responseRegister = new MSResponseRegister();
		responseRegister.setAccept(true);
		responseRegister.setPlayer(Player.PLAYER_2);
		session.write(responseRegister);
	}
	
	@IoHandlerTransition(on = MESSAGE_SENT, in = REGISTER_FINSHED, next = GAME_ON)
	public void registerFinished() {
		this.simulator.gameOn();
	}
	
	@IoHandlerTransition(on = MESSAGE_RECEIVED, in = GAME_ON)
	public void gameOn(Move command) {
		this.simulator.append(command);
	}
	
	@Override
	public void onException(SimulationException e) {
		LOGGER.info(e.getMessage());
	}

	@Override
	public void gameStateChanged(GameEvent event) {
		server.broadcastMessage(event);
		if (event.getGameState() == GameState.GAME_FINISHED)
			StateControl.breakAndCallNext(GAME_FINISHED);
	}

	@Override
	public void boardStateChanged(BoardEvent event) {
		server.broadcastMessage(event);
	}
	
	@IoHandlerTransition(on = EXCEPTION_CAUGHT, in = ROOT)
	public void onException(MSRequestDecodingException e) {
		LOGGER.info("Exception caught", e);
	}
	
	@IoHandlerTransition(on = EXCEPTION_CAUGHT, in = ROOT, weight = 10)
	public void onException(Exception e) {
		LOGGER.info("Exception caught", e);
	}
	
	@IoHandlerTransition(in = ROOT, weight=100)
	public void unhandledEvents(Event event) {
		LOGGER.info(event.getId().toString());
	}
}