package models;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import services.WallService;

public class Wall {

	public Point[] corners;
	public MatOfPoint contour;
	public Point[] points;
	public Point center;
	private double borderHeight = 6.5D;

	public Wall(MatOfPoint c) {

	
		//c = substractBorder(c);

		locateCorners(c);
		locateCenter(c);

		this.contour = c;
		this.points = c.toArray();

	}

	public Wall substractBorder(Wall w) {
		
		return new Wall(substractBorder(w.contour));
	}
	
	
	
	
	private MatOfPoint substractBorder(MatOfPoint c) {

		double factor = borderHeight / WallService.camHeight;		
		Point[] tmp = c.toArray();
		Point[] cor = new Point[tmp.length];
		
		for (int i = 0; i < tmp.length; i++) {


			Point wp = tmp[i];

			double x = wp.x - WallService.imageCenter.x;
			double y = wp.y - WallService.imageCenter.y;

			double nx = x * factor;
			double ny = y * factor;

			Point corrected = new Point((wp.x) - nx, (wp.y) - ny);

			// Imgproc.drawMarker(frame, corrected, new Scalar(0,0,0));

			cor[i] = corrected;
		}

		return new MatOfPoint(cor);
	}

	private void locateCorners(MatOfPoint c) {

		// https://www.programcreek.com/java-api-examples/?class=org.opencv.imgproc.Imgproc&method=approxPolyDP

		MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
		double ap = Imgproc.arcLength(c2f, true) * 0.02;

		MatOfPoint2f approxCurve = new MatOfPoint2f();
		Imgproc.approxPolyDP(c2f, approxCurve, ap, true);

		MatOfPoint points = new MatOfPoint(approxCurve.toArray());

		this.corners = points.toArray();

	}

	private void locateCenter(MatOfPoint c) {

		Moments m = Imgproc.moments(c);
		center = new Point();
		center.x = m.get_m10() / m.get_m00();
		center.y = m.get_m01() / m.get_m00();

	}

	public static Wall copyWithOrigo(Wall wall, Point origo) {

		Point[] ca = new Point[wall.points.length];

		for (int i = 0; i < wall.points.length; i++) {
			Point pp = wall.points[i];
			ca[i] = new Point(pp.x - origo.x, pp.y - origo.y);
		}

		Wall w = new Wall(new MatOfPoint(ca));

		return w;
	}

}
