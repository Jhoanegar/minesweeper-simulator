/* Copyright 2013, Carlos Alegría Galicia
 *
 * This file is part of Vector Dominance.
 *
 * Vector Dominance is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Mine Sweeper Simulator is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Vector Dominance. If not, see <http://www.gnu.org/licenses/>.
 */
package mx.unam.fesa.isoo.dominance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A program to compute the set of maximal, minimal, and common elements of a
 * given d-dimensional point set, as described in https:/
 * /sites.google.com/site/isoofesa20141/proyectos/proyecto_2
 * /dominacion_vectorial.pdf?attredirects=0&d=1.
 * </p>
 * The points are read from a text file that specifies points in the following
 * format:
 * <pre>
 * d n
 * id1 x11 x12 ... x1d
 * id2 x21 y22 ... x2d
 * ...
 * idn xn1 xn2 ... xnd
 * </pre>
 * The first row specifies that there are <code>n</code> <code>d</code>-dimensional
 * points in the set to be processed. The following <code>n</code> describe the
 * points, using a unique id (the elements id#) and its coordinates (the
 * elements x##).
 * </p>
 * The results are printed in the standard output, using the following format:
 * <pre>
 * maximales
 * id1 id5 id3 ...
 * minimales
 * id4 id7 id11 ...
 * restantes
 * id2 id8 id9 ...
 * </pre>
 * 
 * @author Carlos Alegría Galicia
 * 
 */
public class VectorDominance {

	/**
	 * Main entry of the program
	 * 
	 * @param args
	 * @throws IOException If there is any problem reading the text file 
	 */
	public static void main(String[] args) throws IOException {

		//
		// reading points from file
		//

		// the text file is given as a resource; i.e., a file in the program's
		// classpath
		//
		BufferedReader in = new BufferedReader(new InputStreamReader(
				ClassLoader.getSystemResourceAsStream("points.txt")));

		String params[] = in.readLine().split(" ");
		int dim = Integer.parseInt(params[0]);
		int num = Integer.parseInt(params[1]);

		Set<Point> maximalElements = new HashSet<Point>(num);
		Set<Point> minimalElements = new HashSet<Point>();
		Set<Point> commonElements = new HashSet<Point>();

		for (int i = 0; i < num; i++) {
			maximalElements.add(new Point(in.readLine(), dim));
		}

		//
		// computing maximal elements
		//

		Point onTest;
		for (Iterator<Point> iterator = maximalElements.iterator(); iterator
				.hasNext();) {
			onTest = iterator.next();

			for (Point point : maximalElements) {
				if (onTest.equals(point))
					continue;
				if (point.dominates(onTest)) {
					iterator.remove();
					minimalElements.add(onTest);
					break;
				}
			}
		}

		//
		// computing minimal elements
		//

		for (Iterator<Point> iterator = minimalElements.iterator(); iterator
				.hasNext();) {
			onTest = iterator.next();

			for (Point point : minimalElements) {
				if (onTest.equals(point))
					continue;
				if (onTest.dominates(point)) {
					iterator.remove();
					commonElements.add(onTest);
					break;
				}
			}
		}

		//
		// print results
		//

		VectorDominance.printSet("maximales", maximalElements);
		VectorDominance.printSet("minimales", minimalElements);
		VectorDominance.printSet("restantes", commonElements);
	}

	/**
	 * Prints the elements of the given {@link Set}, separated by a space.
	 * 
	 * @param label A String printed before the set's contents
	 * @param set The {@link Set} whose contents are to be printed.
	 */
	private static final void printSet(String label, Set<?> set) {
		System.out.println(label);
		for (Object object : set) {
			System.out.print(object + " ");
		}
		System.out.println();
	}
}