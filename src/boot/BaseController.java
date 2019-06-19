package boot;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import camera.Camera;
import camera.CameraFake;
import camera.CameraInterface;
import javafx.scene.image.ImageView;

public class BaseController {

	// a timer for acquiring the video stream
	public ScheduledExecutorService timer;
	// a flag to change the button behavior
	public boolean cameraActive;

	public CameraInterface camera = new Camera();
	//public CameraInterface camera = new CameraFake();

	public Callable onSaveCallable;

	public void imageViewProperties(ImageView image, int dimension) {
		// set a fixed width for the given ImageView
		image.setFitWidth(dimension);
		// preserve the image ratio
		image.setPreserveRatio(true);
	}

	public void updateImageView(ImageView view, Mat image) {

		Utils.onFXThread(view.imageProperty(), Utils.mat2Image(image));
	}

	/**
	 * On application close, stop the acquisition from the camera
	 */
	public void setClosed() {
		this.stopAcquisition();
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	public void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.camera.isOpened()) {
			// release the camera
			this.camera.release();
		}
	}

	public void initialize() {

	}

	public void save() {
		
		camera.release();

		if (onSaveCallable != null) {
			try {
				onSaveCallable.call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void onSave(Callable callable) {
		onSaveCallable = callable;
	}

}
