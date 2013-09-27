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

/**
 * An abstraction of a d-dimensional point in an euclidean space. Provides ways
 * to create a point using the text-based specification as described in https:/
 * /sites.google.com/site/isoofesa20141/proyectos/proyecto_2
 * /dominacion_vectorial.pdf?attredirects=0&d=1, and to compare this point with
 * other d-dimensional points using vector dominance.
 * 
 * @author Carlos Alegría Galicia
 * 
 */
public class Point {
	/* the unique identificator of this Point */
	private final String id;
	/* the coordinates of this point */
	private final int coords[];

	/**
	 * Creates a Point from the given file line, using the given dimension. As
	 * the syntax of the points specification file is supposed to correct, no
	 * consistency checks are made (eg., number of components in the line
	 * corresponds to <code>dim</code>).
	 * 
	 * @param line
	 *            The line of the file specifying points,
	 * @param dim
	 *            The number of coordinates on this point
	 */
	public Point(String line, int dim) {
		String components[] = line.split(" ");

		this.id = components[0];
		this.coords = new int[dim];

		for (int i = 0; i < dim; i++) {
			this.coords[i] = Integer.parseInt(components[i + 1]);
		}
	}

	/**
	 * Returns the id of this Point
	 * 
	 * @return The id of this Point
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the ith coordinate of this Point
	 * 
	 * @return The ith coordinate of this Point
	 */
	public int getCoordinate(int i) {
		return this.coords[i];
	}

	/**
	 * Checks if this Point dominates the specified Point, using vector
	 * dominance.
	 * 
	 * @param point
	 *            The Point to compare this Point with
	 * @return true if this Point dominates point, false otherwise
	 */
	public boolean dominates(Point point) {
		for (int i = 0; i < this.coords.length; i++) {
			if (this.coords[i] < point.coords[i]) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// obj and this are references to the same object
		//
		if (this == obj)
			return true;
		// obj is not an instance of Point
		//
		if (!(obj instanceof Point))
			return false;

		// obj is a Point, and does not hace the same number of coordinates as
		// this Point
		//
		Point point = (Point) obj;
		if (this.coords.length != point.coords.length)
			return false;
		
		// obj is a Point, and all its coordinates are equal to the coordinates
		// of this point
		//
		for (int i = 0; i < this.coords.length; i++) {
			if (this.coords[i] != point.coords[i])
				return false;
		}
		return true;
	}
}