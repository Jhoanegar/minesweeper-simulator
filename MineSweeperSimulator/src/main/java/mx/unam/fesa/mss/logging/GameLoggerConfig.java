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
package mx.unam.fesa.mss.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class GameLoggerConfig {
	
	public static final String LOGGER_NAME = "mx.unam.fesa.mss.GameLogger";
	public GameLoggerConfig() {
		
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler("%h/mss.log", 2000000, 5, false);
			fileHandler.setLevel(Level.INFO);
			fileHandler.setFormatter(new Formatter() {
				
				@Override
				public String format(LogRecord record) {
					return formatMessage(record) + "\n";
				}
			});
			Logger logger = Logger.getLogger(LOGGER_NAME);
			logger.addHandler(fileHandler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
}
