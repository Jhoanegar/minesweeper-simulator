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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mx.unam.fesa.isoo.mss.core.Move;
import mx.unam.fesa.isoo.mss.core.Player;

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
public final class MSRequestDecoder extends ProtocolDecoderAdapter {
	
	/* */
	public static final int MAX_MESSAGE_LENGTH = 16;
	
	/* */
	private static final String REMOVE_FLAG = "RF";
	/* */
	private static final String SET_FLAG = "SF";
	/* */
	private static final String UNCOVER = "UN";
	/* */
	private static final Logger LOGGER = LoggerFactory.getLogger(MSRequestDecoder.class);
	
	/* */
	private final Matcher moveMatcher = Pattern.compile("\\((RF|SF|UN) (0|[1-9]\\d?) (0|[1-9]\\d?)").matcher("");
	/* */
	private final Matcher registerMatcher = Pattern.compile("\\(REG (\\w{1,8})\\)").matcher("");
	/* */
	private CharBuffer charBuffer;
	/* */
	private CharsetDecoder decoder;

	/**
	 * 
	 */
	public MSRequestDecoder() {
		this.decoder = StandardCharsets.UTF_8.newDecoder();
		this.charBuffer = CharBuffer.allocate(MAX_MESSAGE_LENGTH);
	}
	
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
		// decoding command string
		//
		
		// low level decoding
		//
		byte data[] = new byte[in.limit()]; in.get(data);
		this.decoder.decode(ByteBuffer.wrap(data), this.charBuffer, true);
		
		try {
			this.decoder.flush(this.charBuffer).throwException();
		} catch (BufferOverflowException | CharacterCodingException e) {
			throw new MSRequestDecodingException("Error decoding request bytes", e);
		} catch (BufferUnderflowException e) {
		}
		
		// String to Command decoding
		//
		String message = this.charBuffer.flip().toString();
		if (this.moveMatcher.reset(message) != null
				&& this.moveMatcher.lookingAt()) {
			
			Move move = new Move();
			move.setCol(Integer.parseInt(this.moveMatcher.group(2)));
			move.setRow(Integer.parseInt(this.moveMatcher.group(3)));
			move.setTimeStamp(System.currentTimeMillis());
			move.setSource((Player) session.getAttribute(Player.class));
			
			switch (this.moveMatcher.group(1)) {
			case REMOVE_FLAG:
				move.setType(Move.Type.REMOVE_FLAG);
				break;
			case SET_FLAG:
				move.setType(Move.Type.SET_FLAG);
				break;
			case UNCOVER:
				move.setType(Move.Type.UNCOVER);
				break;
			}
			
			// writing message to output
			//
			out.write(move);
			
			LOGGER.debug("Move received from {}: {}", move.getSource(), message);
			
		} else if (this.registerMatcher.reset(message) != null
				&& this.registerMatcher.lookingAt()) {
			MSRequestRegister register = new MSRequestRegister();
			register.setName(this.registerMatcher.group(1));
			
			// writing message to output
			//
			out.write(register);
			
			LOGGER.debug("Register message received: {} ", message);	
		} else {
			throw new MSRequestDecodingException("Unkown message received: "
					+ message);
		}
	}
}