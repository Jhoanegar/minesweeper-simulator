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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import mx.unam.fesa.mss.core.Move;
import mx.unam.fesa.mss.network.protocol.MSRequestRegister;

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
public class MSRequestEncoder extends ProtocolEncoderAdapter {
	
	/* */
	public static final int MAX_MESSAGE_LENGTH = 16;	
	/* */
	public static final int MAX_NAME_LENGTH = 8;
	
	/* */
	private static final String COMMAND_FORMAT = "(%1$s %2$d %2$d)";
	/* */
	private static final String COMMAND_REMOVE_FLAG = "RF";
	/* */
	private static final String COMMAND_SET_FLAG = "SF";
	/* */
	private static final String COMMAND_UNCOVER = "UN";
	
	/* */
	private static final String REGISTER_FORMAT = "(REG %s)";
	
	/* */
	private static final Logger LOGGER = LoggerFactory.getLogger(MSRequestEncoder.class);
	
	/* */
	private CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();

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
		
		if (message instanceof Move) {
			Move command = (Move) message;
			switch (command.getType()) {
			case REMOVE_FLAG:
				printWriter.format(COMMAND_FORMAT, COMMAND_REMOVE_FLAG, command.getRow(), command.getCol());	
				break;
			case SET_FLAG:
				printWriter.format(COMMAND_FORMAT, COMMAND_SET_FLAG, command.getRow(), command.getCol());
				break;
			case UNCOVER:
				printWriter.format(COMMAND_FORMAT, COMMAND_UNCOVER, command.getRow(), command.getCol());
				break;
			}
		} else {
			MSRequestRegister register = (MSRequestRegister) message;
			String name = register.getName();
			printWriter.format(REGISTER_FORMAT,
					name.length() <= MAX_NAME_LENGTH
						? name : name.substring(0, MAX_NAME_LENGTH));
		}
		String encodedMessage = stringWriter.toString();
		
		//
		// encoding string
		//
		
		IoBuffer ioBuffer = IoBuffer.allocate(MAX_MESSAGE_LENGTH, false);
		ioBuffer.putString(encodedMessage, this.encoder);
		ioBuffer.flip();
        out.write(ioBuffer);
        
        LOGGER.debug("Command sent: {}", encodedMessage);
	}
}