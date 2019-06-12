package camera;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class CameraFake  implements CameraInterface{

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

		if(frame != null) {
			return frame.clone();
		}
		
		frame = new Mat();

		String path = this.getClass().getResource("/map7.jpg").getPath();
		path = new File(path).getPath();
		
		frame = Imgcodecs.imread(path);

		if (frame.empty()) {
			return null;
		}

		return frame.clone();

	}
}
