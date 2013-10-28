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

/**
 * @author Carlos Alegría Galicia
 *
 */
public class MSResponseDecondingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MSResponseDecondingException(String message, Throwable cause) {
		super(message, cause);
	}

	public MSResponseDecondingException(String message) {
		super(message);
	}

	public MSResponseDecondingException(Throwable cause) {
		super(cause);
	}
}
