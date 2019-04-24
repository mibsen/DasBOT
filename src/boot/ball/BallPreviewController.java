package boot.ball;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import boot.BaseController;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import models.Ball;
import models.ImageSettings;
import services.BallService;

public class BallPreviewController extends BaseController {

	@FXML
	ImageView ContourImage;

	@FXML
	ImageView TransformedImage;

	@FXML
	public void initialize() {

		if (this.camera.init()) {
			// set a fixed width for all the image to show and preserve image ratio
			this.imageViewProperties(ContourImage, 500);
			this.imageViewProperties(TransformedImage, 500);

			// grab a frame every 33 ms (30 frames/sec)
			Runnable frameGrabber = new Runnable() {

				int count = 0;
				Instant start = Instant.now();
				ExecutorService executor = Executors.newFixedThreadPool(3);

				@Override
				public void run() {

					BallService ballService = new BallService(new ImageSettings(0, 180, 0, 63, 205, 255, 7), 450, 650);

					Mat frame = camera.grabFrame();

					updateImageView(TransformedImage, ballService.getBallFrame(frame));
					List<Ball> balls = ballService.getBalls(frame);
					ballService.drawBalls(frame, balls);
					updateImageView(ContourImage, frame);
				};
			};
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

		} else {

			this.cameraActive = false;

			// stop the timer
			this.stopAcquisition();

		}
	}
}
