package org.ggj2013;

public class MathUtils {
	public static float clamp(float v, float min, float max) {
		return Math.min(max, Math.max(min, v));
	}

	public static float wrap(float v, float min, float max) {
		v = v - (float) Math.floor((v - min) / (max - min)) * (max - min);
		return v;
	}

	public static float lerp(float v, float fromMin, float fromMax,
			float toMin, float toMax) {
		v = (v - fromMin) / (fromMax - fromMin);
		return toMin + clamp(v, 0, 1) * (toMax - toMin);
	}

	public static final int INSIDE = 0; // 0000
	public static final int LEFT = 1; // 0001
	public static final int RIGHT = 2; // 0010
	public static final int BOTTOM = 4; // 0100
	public static final int TOP = 8; // 1000

	// Compute the bit code for a point (x, y) using the clip rectangle
	// bounded diagonally by (xmin, ymin), and (xmax, ymax)

	// ASSUME THAT xmax, xmin, ymax and ymin are global constants.

	public static int ComputeOutCode(double x, double y, double xmin,
			double ymin, double xmax, double ymax) {
		int code;

		code = INSIDE; // initialised as being inside of clip window

		if (x < xmin) // to the left of clip window
			code |= LEFT;
		else if (x > xmax) // to the right of clip window
			code |= RIGHT;
		if (y < ymin) // below the clip window
			code |= BOTTOM;
		else if (y > ymax) // above the clip window
			code |= TOP;

		return code;
	}

	// Cohen–Sutherland clipping algorithm clips a line from
	// P0 = (x0, y0) to P1 = (x1, y1) against a rectangle with
	// diagonal from (xmin, ymin) to (xmax, ymax).
	public static double[] CohenSutherlandLineClipAndDraw(double x0, double y0,
			double x1, double y1, double xmin, double ymin, double xmax,
			double ymax) {
		// compute outcodes for P0, P1, and whatever point lies outside the clip
		// rectangle
		int outcode0 = ComputeOutCode(x0, y0, xmin, ymin, xmax, ymax);
		int outcode1 = ComputeOutCode(x1, y1, xmin, ymin, xmax, ymax);
		boolean accept = false;

		while (true) {
			if ((outcode0 | outcode1) == 0) { // Bitwise OR is 0. Trivially
												// accept
												// and get out of loop
				accept = true;
				break;
			} else if ((outcode0 & outcode1) != 0) { // Bitwise AND is not 0.
														// Trivially
														// reject and get out of
														// loop
				break;
			} else {
				// failed both tests, so calculate the line segment to clip
				// from an outside point to an intersection with clip edge
				double x = 0;
				double y = 0;

				// At least one endpoint is outside the clip rectangle; pick it.
				int outcodeOut = outcode0 != 0 ? outcode0 : outcode1;

				// Now find the intersection point;
				// use formulas y = y0 + slope * (x - x0), x = x0 + (1 / slope)
				// * (y - y0)
				if ((outcodeOut & TOP) != 0) { // point is above the clip
												// rectangle
					x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
					y = ymax;
				} else if ((outcodeOut & BOTTOM) != 0) { // point is below the
															// clip
															// rectangle
					x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
					y = ymin;
				} else if ((outcodeOut & RIGHT) != 0) { // point is to the right
														// of
														// clip rectangle
					y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
					x = xmax;
				} else if ((outcodeOut & LEFT) != 0) { // point is to the left
														// of
														// clip
					// rectangle
					y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
					x = xmin;
				}

				// NOTE:*****************************************************************************************

				/*
				 * if you follow this algorithm exactly (at least for c#), then
				 * you will fall into an infinite loop in case a line crosses
				 * more than two segments. to avoid that problem, leave out the
				 * last else if (outcodeOut & LEFT) and just make it else
				 */

				// **********************************************************************************************

				// Now we move outside point to intersection point to clip
				// and get ready for next pass.
				if (outcodeOut == outcode0) {
					x0 = x;
					y0 = y;
					outcode0 = ComputeOutCode(x0, y0, xmin, ymin, xmax, ymax);
				} else {
					x1 = x;
					y1 = y;
					outcode1 = ComputeOutCode(x1, y1, xmin, ymin, xmax, ymax);
				}
			}
		}

		if (accept) {
			return new double[] { x0, y0, x1, y1 };
		} else {
			return null;
		}
	}
}
