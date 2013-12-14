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

import java.util.logging.LogManager;

import mx.unam.fesa.isoo.msp.network.MSClient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class MSPMain {

	/* */
	private static final char OPT_SERVER = 's';
	/* */
	private static final char OPT_PORT = 'p';
	/* */
	private static final char OPT_HELP = 'h';

	/* */
	private static final int DEFAULT_PORT = 4444;
	/* */
	private static final String DEFAULT_SERVER = "localhost";
	/* */
	private static final String DEFAULT_LOGGING_CONFIG_FILE = "logging.properties";

	/**
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		//
		// creating options
		//

		Options options = new Options();

		// help option
		//
		options.addOption(OptionBuilder
				.withDescription("Prints this message.")
				.withLongOpt("help")
				.create(OPT_HELP));

		// server option
		//
		options.addOption(OptionBuilder
				.withDescription(
						"The server this MineSweeperPlayer will connect to.")
				.hasArg()
				.withArgName("SERVER")
				.withLongOpt("server")
				.create(OPT_SERVER));
		
		// port option
		//
		options.addOption(OptionBuilder
				.withDescription(
						"The port this MineSweeperPlayer will connect to.")
				.hasArg()
				.withType(new Integer(0))
				.withArgName("PORT")
				.withLongOpt("port")
				.create(OPT_PORT));

		// parsing options
		//
		String hostname = DEFAULT_SERVER;
		int port = DEFAULT_PORT;
		try {
			// using GNU standard
			//
			CommandLine line = new GnuParser().parse(options, args);

			if (line.hasOption(OPT_HELP)) {
				new HelpFormatter().printHelp("msc [options]", options);
				return;
			}

			if (line.hasOption(OPT_PORT)) {
				try {
					port = (Integer) line.getOptionObject(OPT_PORT);
				} catch (Exception e) {
				}
			}

			if (line.hasOption(OPT_SERVER)) {
				hostname = line.getOptionValue(OPT_PORT);
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
			throw new Error("Could not load logging properties file.", e);
		}

		//
		// setting up Mine Sweeper client
		//

		try {
			new MSClient(hostname, port);
		} catch (Exception e) {
			System.err.println("Could not execute MineSweeper client: "
					+ e.getMessage());
		}
	}
}