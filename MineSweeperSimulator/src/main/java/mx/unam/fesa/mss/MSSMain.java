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
package mx.unam.fesa.mss;

import java.util.logging.LogManager;

import mx.unam.fesa.mss.network.MSServer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class MSSMain {

	/* */
	private static final char OPT_HELP = 'h';
	/* */
	private static final char OPT_PORT = 'p';

	/* */
	private static final int DEFAULT_PORT = 4444;
	/* */
	private static final String DEFAULT_LOGGING_CONFIG_FILE = "logging.properties";

	/**
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		//
		// managing command line options using commons-cli
		//

		Options options = new Options();

		// help option
		//
		options.addOption(OptionBuilder
				.withDescription("Prints this message.")
				.withLongOpt("help")
				.create(OPT_HELP));

		// port option
		//
		options.addOption(OptionBuilder
				.withDescription("The port in which the MineSweeperServer will listen to.")
				.hasArg()
				.withType(new Integer(0))
				.withArgName("PORT")
				.withLongOpt("port")
				.create(OPT_PORT));

		// parsing options
		//
		int port = DEFAULT_PORT;
		try {
			// using GNU standard
			//
			CommandLine line = new GnuParser().parse(options, args);

			if (line.hasOption(OPT_HELP)) {
				new HelpFormatter().printHelp("mss [options]", options);
				return;
			}

			if (line.hasOption(OPT_PORT)) {
				try {
					port = (Integer) line.getOptionObject(OPT_PORT);
				} catch (Exception e) {
				}
			}
		} catch (ParseException e) {
			System.err.println(
					"Could not parse command line options correctly: "
							+ e.getMessage());
			return;
		}

		//
		// configuring logging services
		//

		try {
			LogManager.getLogManager().readConfiguration(
					ClassLoader.getSystemResourceAsStream(DEFAULT_LOGGING_CONFIG_FILE));
		} catch (Exception e) {
			throw new Error("Could not load logging properties file", e);
		}

		//
		// setting up UDP server
		//		

		try {
			new MSServer(port);
		} catch (Exception e) {
			LoggerFactory.getLogger(MSSMain.class).error(
					"Could not execute MineSweeper server: ", e);
		}
	}
}