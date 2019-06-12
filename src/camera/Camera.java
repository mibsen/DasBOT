package camera;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

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
		
		/*
		Size sz = new Size(frame.width()/2,frame.height()/2);
		Imgproc.resize( frame, frame, sz );
		 */
		return frame;

	}
}
