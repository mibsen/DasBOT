package camera;

import java.io.File;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class CameraFake implements CameraInterface {

	private Mat frame = null;

	public CameraFake() {

	}

	public boolean init() {

		return true;
	}

	public boolean isOpened() {
		return true;
	}

	public void release() {
	}

	public Mat grabFrame() {

		if (frame != null) {
			return frame.clone();
		}

		frame = new Mat();

		String path = this.getClass().getResource("/map9.jpg").getPath();
		path = new File(path).getPath();

		frame = Imgcodecs.imread(path);

		if (frame.empty()) {
			return null;
		}

		// K, D, np.eye(3), K, DIM,

		Mat map1 = new Mat();
		Mat map2 = new Mat();

		int[][] eyeArray = new int[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		Mat eye = new Mat(3, 3, CvType.CV_8UC1);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++)
				eye.put(row, col, eyeArray[row][col]);
		}

		// D = distCoeffs
		double[][] dArray = new double[][] { { 0.4108889201891428 }, { -0.0029018582999649468 },
				{ -0.019218620930577385 }, { -0.2094587331452949 } };
		Mat d = new Mat(4, 1, CvType.CV_8UC1);
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 1; col++)
				d.put(row, col, dArray[row][col]);
		}

		// K = cameraMatric
		// ([[502.26087138645096, 0.0, 358.7882796447911], [0.0, 502.1027412727765,
		// 244.26495016375978], [0.0, 0.0, 1.0]])
		double[][] kArray = new double[][] { { 502.26087138645096, 0.0, 358.7882796447911 },
				{ 0.0, 502.1027412727765, 244.26495016375978 }, { 0.0, 0.0, 1.0 } };
		Mat k = new Mat(3, 3, CvType.CV_8UC1);
		
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++)
				k.put(row, col, kArray[row][col]);
		}

		Calib3d.initUndistortRectifyMap(k, d, eye, k, frame.size(), CvType.CV_16SC2, map1, map2);
		Imgproc.remap(frame, frame, map1, map2, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT);

		/*
		 * Size sz = new Size(frame.width()/2,frame.height()/2); Imgproc.resize( frame,
		 * frame, sz );
		 */

		return frame.clone();

	}
}
