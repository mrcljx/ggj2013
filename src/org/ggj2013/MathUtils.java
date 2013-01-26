package org.ggj2013;

public class MathUtils {
	public static float clamp(float v, float min, float max) {
		return Math.min(max, Math.max(min, v));
	}

	public static float wrap(float v, float min, float max) {
		v = v - (float) Math.floor((v - min) / (max - min)) * (max - min);
		return v;
	}
}
