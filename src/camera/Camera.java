package camera;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Camera implements CameraInterface {

	// the OpenCV object that performs the video capture
	private VideoCapture capture = new VideoCapture();

	public Camera() {

	}

	public boolean init() {

		this.capture.open(4);

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

		return frame;

	}
}
