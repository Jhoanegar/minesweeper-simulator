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
package mx.unam.fesa.isoo.msp.network;

import java.net.InetSocketAddress;

import mx.unam.fesa.isoo.msp.MSPlayer;
import mx.unam.fesa.isoo.msp.network.protocol.MSRequestEncoder;
import mx.unam.fesa.isoo.msp.network.protocol.MSResponseDecoder;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.statemachine.StateMachine;
import org.apache.mina.statemachine.StateMachineFactory;
import org.apache.mina.statemachine.StateMachineProxyBuilder;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class MSClient {
	
	/* */
	private NioDatagramConnector connector;

	/**
	 * @param hostname
	 * @param port
	 */
	public MSClient(String hostname, int port) {
		this.connector = new NioDatagramConnector();
		this.connector.getFilterChain().addLast(
				"ms protocol codec",
				new ProtocolCodecFilter(new MSRequestEncoder(), new MSResponseDecoder()));

		StateMachine sm = StateMachineFactory.getInstance(
				IoHandlerTransition.class).create(MSPlayer.REGISTER, new MSPlayer(this));
		this.connector.setHandler(new StateMachineProxyBuilder().create(IoHandler.class, sm));
		this.connector.connect(new InetSocketAddress(hostname, port));
	}
	
	public void stop() {
		this.connector.dispose();
	}
}