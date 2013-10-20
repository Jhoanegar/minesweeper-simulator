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
package mx.unam.fesa.isoo.dominance;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Random;

/**
 * @author Carlos Alegría Galicia
 * 
 */
public class TestFileGenerator {
	
	/* the file name for the point set containing only maximals */
	private static final String ALL_MAXIMALS = "all_maximals.txt";
	
	/* the file name for the point set containing one maximal and one minimal */
	private static final String ONE_MAXIMAL_ONE_MINIMAL = "one_maximal_one_minimal.txt";
	
	/* the file name for the point set containing random coordinates */
	private static final String RANDOM = "random.txt";
	
	/* 
	 * the file name for the point set containing one maximal and d minimals,
	 * where d is the dimension of the euclidean space 
	 */
	private static final String ONE_MAXIMAL_D_MINIMALS = "one_maximal_d_minimals.txt";
	
	/* */
	private static final int MIN_POINTS_NUMBER = 3;
	/* */
	private static final int MAX_POINTS_NUMBER = 500;
	
	/* */
	private static final int MIN_DIMENSION = 2;
	/* */
	private static final int MAX_DIMENSION = 9;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		TestFileGenerator.generate(args[0]);
	}

	/**
	 * @param dirpath
	 * @throws IOException
	 */
	public static void generate(String dirpath) throws IOException {
		
		// creating a file with maximal elements
		//
		// The points are placed over a plane in 3D, intersecting the planes XY,
		// XZ, and ZY. This is in resemblance of the fact that all points in the
		// plane over a line (or an XY-monotone curve) with negative slope are
		// maximals.
		//
		// The points are created by using the following parametric equation,
		// obtained by considering the points (0, 0, 1), (0, 1, 0), and
		// (1, 0, 0):
		//
		// P = (0, 0, 1) + s(1, 0, -1) + t(-1, 2, 1)
		//
		try (PrintWriter out = new PrintWriter(
				Files.newOutputStream(FileSystems.getDefault().getPath(
						dirpath, ALL_MAXIMALS)))) {

			Random random = new Random();
			final int pointsNumber = MIN_POINTS_NUMBER + random.nextInt(MAX_POINTS_NUMBER);
			final int coords[] = new int[3];

			out.println("3 " + pointsNumber);
			for (int i = 1; i <= pointsNumber; i++) {

				int randAlpha = random.nextInt();
				int randBeta = random.nextInt();

				coords[0] = randAlpha - randBeta;
				coords[1] = 2 * randBeta;
				coords[2] = 1 - randAlpha + randBeta;

				printPoint(out, "p" + i, coords);
			}
		}

		// points with one maximal and one minimal
		//
		// The points are placed over a line passing by the origin, contained
		// in the first quadrant of the d-dimensional space. This is in
		// resemblance of the fact that, when the points of the set are placed
		// over a line (or an XY-monotone curve) with positive slope, there are
		// one maximal and one minimal.
		//
		// The points are created by using the following parametric equation,
		// obtained by considering the direction (1, ..., 1):
		//
		// P = (0, ..., 0) + t(1, ..., 1)
		//
		try (PrintWriter out = new PrintWriter(
				Files.newOutputStream(FileSystems.getDefault().getPath(
						dirpath, ONE_MAXIMAL_ONE_MINIMAL)))) {

			Random random = new Random();
			final int pointsNumber = MIN_POINTS_NUMBER + random.nextInt(MAX_POINTS_NUMBER);
			final int coords[] = new int[MIN_DIMENSION + random.nextInt(MAX_DIMENSION)];

			out.println(coords.length + " " + pointsNumber);
			for (int i = 1; i <= pointsNumber; i++) {

				int randAlpha = random.nextInt();
				for (int j = 0; j < coords.length; j++) {
					coords[j] = randAlpha;
				}

				printPoint(out, "p" + i, coords);
			}
		}

		// random points
		//
		// The points contain random coordinates.
		//
		try (PrintWriter out = new PrintWriter(
				Files.newOutputStream(FileSystems.getDefault().getPath(
						dirpath, RANDOM)))) {

			Random random = new Random();
			final int pointsNumber = MIN_POINTS_NUMBER + random.nextInt(MAX_POINTS_NUMBER);
			final int coords[] = new int[MIN_DIMENSION + random.nextInt(MAX_DIMENSION)];

			out.println(coords.length + " " + pointsNumber);
			for (int i = 1; i <= pointsNumber; i++) {

				for (int j = 0; j < coords.length; j++) {
					coords[j] = random.nextInt();
				}

				printPoint(out, "p" + i, coords);
			}
		}

		// one maximal, d minimals
		//
		// The points are located over the border of a d-dimensional orthant
		// with its vertex placed over a point with random coordinates. Thus,
		// there is one maximal element, d minimal elements, and n - d - 1
		// neither maximal nor minimal points. This is in resemblance to the
		// fact that, if the points are placed over isothetic lines going down
		// and left from a point in the plane, there is one maximal (the initial
		// point), 2 minimals (the extreme bottom and left points), and n - 2 -1
		// neither maximal nor minimal points.
		//
		try (PrintWriter out = new PrintWriter(
				Files.newOutputStream(FileSystems.getDefault().getPath(
						dirpath, ONE_MAXIMAL_D_MINIMALS)))) {

			Random random = new Random();
			final int pointsNumber = MIN_POINTS_NUMBER + random.nextInt(MAX_POINTS_NUMBER);

			final int basePoint[] = new int[MIN_DIMENSION + random.nextInt(MAX_DIMENSION)];
			final int printablePoint[] = new int[basePoint.length];
			final int tempPoint[] = new int[basePoint.length];

			// fill base point
			//
			for (int i = 0; i < basePoint.length; i++) {
				basePoint[i] = random.nextInt();
				tempPoint[i] = basePoint[i];
			}

			out.println(basePoint.length + " " + pointsNumber);
			printPoint(out, "p1", basePoint);
			for (int i = 2, coord = 0; i <= pointsNumber; i++, coord = ++coord
					% basePoint.length) {

				for (int j = 0; j < basePoint.length; j++) {
					if (j == coord) {
						tempPoint[j] -= random.nextInt(1 + 50);
						printablePoint[j] = tempPoint[j];
					} else {
						printablePoint[j] = basePoint[j];
					}
				}

				printPoint(out, "p" + i, printablePoint);
			}
		}
	}
	
	/**
	 * @param out
	 * @param label
	 * @param coords
	 */
	private static void printPoint(PrintWriter out, String label, int[] coords) {
		out.print(label);
		for (int i : coords) {
			out.print(" " + i);
		}
		out.println();
	}
}