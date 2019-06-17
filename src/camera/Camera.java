package camera;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.calib3d.Calib3d;

public class Camera implements CameraInterface {

	// the OpenCV object that performs the video capture
	private VideoCapture capture = new VideoCapture();

	public Camera() {

	}

	public boolean init() {

		this.capture.open(0);

		return this.capture.isOpened();

	}

	public boolean isOpened() {
		return capture.isOpened();
	}

	public void release() {
		capture.release();
	}

	public Mat grabFrame() {

		Mat frame = new Mat();

		capture.read(frame);

		if (frame.empty()) {
			return null;
		}

		// K, D, np.eye(3), K, DIM,

		/*
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

		*/
		
		Size sz = new Size(frame.width() / 2, frame.height() / 2);
		Imgproc.resize(frame, frame, sz);

		return frame;

	}
}
