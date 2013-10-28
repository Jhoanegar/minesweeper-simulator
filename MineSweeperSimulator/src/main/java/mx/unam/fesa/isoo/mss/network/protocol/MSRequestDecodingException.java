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

/**
 * @author Carlos Alegría Galicia
 *
 */
public final class MSRequestDecodingException extends RuntimeException {

	private static final long serialVersionUID = 1L;
		
	public MSRequestDecodingException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MSRequestDecodingException(String message, Throwable cause) {
		super(message, cause);
	}

	public MSRequestDecodingException(String message) {
		super(message);
	}

	public MSRequestDecodingException(Throwable cause) {
		super(cause);
	}
}