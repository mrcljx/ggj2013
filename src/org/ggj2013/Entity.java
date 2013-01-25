package org.ggj2013;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Entity {
	public Vector3D position;
	public double size;

	/**
	 * Orientation of entity in Radians.
	 */
	public double orientation;

	public double distanceTo(Entity e) {
		return distanceTo(e.position);
	}

	public double distanceTo(Vector3D v) {
		return vectorTo(v).getNorm();
	}

	public Vector3D vectorTo(Entity e) {
		return vectorTo(e.position);
	}

	public Vector3D vectorTo(Vector3D v) {
		return v.subtract(position);
	}

	public boolean collidesWith(Entity e) {
		return distanceTo(e) <= (size + e.size);
	}
}
