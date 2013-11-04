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
package mx.unam.fesa.mss.network;

import java.io.IOException;
import java.net.InetSocketAddress;

import mx.unam.fesa.mss.network.protocol.MSProtocolHandler;
import mx.unam.fesa.mss.network.protocol.MSRequestDecoder;
import mx.unam.fesa.mss.network.protocol.MSResponseEncoder;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.statemachine.StateMachine;
import org.apache.mina.statemachine.StateMachineFactory;
import org.apache.mina.statemachine.StateMachineProxyBuilder;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class MSServer {
	
	/* */
	private static final Logger LOGGER = LoggerFactory.getLogger(MSServer.class);
	
	/* */
	private NioDatagramAcceptor acceptor;
	/* */
	private InetSocketAddress address;
	
	/**
	 * @param port
	 * @throws IOException 
	 */
	public MSServer(int port) throws IOException {
		this.acceptor = new NioDatagramAcceptor();
		this.acceptor.getSessionConfig().setReuseAddress(true);
		this.acceptor.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new MSResponseEncoder(), new MSRequestDecoder()));
		//this.acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		this.acceptor.addListener(new IoServiceListener() {
			
			/* (non-Javadoc)
			 * @see org.apache.mina.core.service.IoServiceListener#sessionDestroyed(org.apache.mina.core.session.IoSession)
			 */
			@Override
			public void sessionDestroyed(IoSession session) throws Exception {
			}
			
			/* (non-Javadoc)
			 * @see org.apache.mina.core.service.IoServiceListener#sessionCreated(org.apache.mina.core.session.IoSession)
			 */
			@Override
			public void sessionCreated(IoSession session) throws Exception {				
			}
			
			/* (non-Javadoc)
			 * @see org.apache.mina.core.service.IoServiceListener#serviceIdle(org.apache.mina.core.service.IoService, org.apache.mina.core.session.IdleStatus)
			 */
			@Override
			public void serviceIdle(IoService service, IdleStatus idleStatus)
					throws Exception {
			}
			
			/* (non-Javadoc)
			 * @see org.apache.mina.core.service.IoServiceListener#serviceDeactivated(org.apache.mina.core.service.IoService)
			 */
			@Override
			public void serviceDeactivated(IoService service) throws Exception {
			}
			
			/* (non-Javadoc)
			 * @see org.apache.mina.core.service.IoServiceListener#serviceActivated(org.apache.mina.core.service.IoService)
			 */
			@Override
			public void serviceActivated(IoService service) throws Exception {
				LOGGER.info("Server connected to " + address);
			}
		});
		
		StateMachine sm = StateMachineFactory.getInstance(
				IoHandlerTransition.class).create(MSProtocolHandler.REGISTER_PLAYER1,
				new MSProtocolHandler(this));
		this.acceptor.setHandler(new StateMachineProxyBuilder().create(IoHandler.class, sm));
		this.acceptor.bind(this.address = new InetSocketAddress(port));
	}
	
	/**
	 * @param object
	 */
	public void broadcastMessage(Object object) {
		this.acceptor.broadcast(object);
	}
	
	/**
	 * 
	 */
	public void stop() {
		this.acceptor.unbind();
	}
}