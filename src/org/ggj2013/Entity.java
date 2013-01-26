package org.ggj2013;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Entity {
	public Vector3D position = Vector3D.ZERO;
	public static final Vector3D FORWARD = Vector3D.PLUS_J;
	public static final Vector3D UP = Vector3D.PLUS_K;
	public float size = 0.5f;

	/**
	 * Orientation of entity in Radians.
	 */
	public float orientation;
	public String name;

	public Entity(String name) {
		this.name = name;
	}

	public float distanceTo(Entity e) {
		return distanceTo(e.position) - e.size - size;
	}

	public float distanceTo(Vector3D v) {
		return (float) vectorTo(v).getNorm();
	}

	public Vector3D vectorTo(Entity e) {
		return vectorTo(e.position);
	}

	public Vector3D vectorTo(Vector3D v) {
		return v.subtract(position);
	}

	public boolean collidesWith(Entity e) {
		return distanceTo(e) <= 0;
	}

	public Vector3D getLookDirection() {
		Vector3D forward = new Vector3D(0, 1, 0);
		Rotation rotation = new Rotation(UP, Math.toRadians(-orientation));
		return rotation.applyTo(forward);
	}

	public void moveForward(float amount) {
		position = position.add(amount, getLookDirection());
	}

	public float volume() {
		return 5;
	}
}
