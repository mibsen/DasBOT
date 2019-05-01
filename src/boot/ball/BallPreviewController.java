package boot.ball;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import boot.BaseController;
import config.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import models.Ball;
import models.BallSettings;
import services.BallService;

public class BallPreviewController extends BaseController {

	@FXML
	ImageView ContourImage;

	@FXML
	ImageView TransformedImage;

	@FXML
	Slider saturationStart;

	@FXML
	Slider saturationStop;

	@FXML
	Slider hueStart;

	@FXML
	Slider hueStop;

	@FXML
	Slider valueStart;

	@FXML
	Slider valueStop;

	@FXML
	Slider blur;

	@FXML
	Slider min;

	@FXML
	Slider max;
	
	@FXML
	TextArea bugValues;

	private BallSettings settings;

	private Config config;
	
	@FXML
	public void initialize() {

		config = new Config();
		settings = config.loadBall();

		
		saturationStart.setValue(settings.image.saturation.start);
		saturationStop.setValue(settings.image.saturation.stop);

		hueStart.setValue(settings.image.hue.start);
		hueStop.setValue(settings.image.hue.stop);

		valueStart.setValue(settings.image.value.start);
		valueStop.setValue(settings.image.value.stop);

		blur.setValue(settings.image.blur);

		min.setValue(settings.min);
		max.setValue(settings.max);

		if (this.camera.init()) {

			// set a fixed width for all the image to show and preserve image ratio
			this.imageViewProperties(ContourImage, 500);
			this.imageViewProperties(TransformedImage, 500);

			// grab a frame every 33 ms (30 frames/sec)
			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {

					settings.image.hue.start = hueStart.getValue();
					settings.image.hue.stop = hueStop.getValue();

					settings.image.saturation.start = saturationStart.getValue();
					settings.image.saturation.stop = saturationStop.getValue();
					
					settings.image.value.start = valueStart.getValue();
					settings.image.value.stop = valueStop.getValue();

					settings.image.blur = blur.getValue();
					
					settings.min = min.getValue();
					settings.max = max.getValue();

					BallService ballService = new BallService(settings);

					Mat frame = camera.grabFrame();

					updateImageView(TransformedImage, ballService.getBallFrame(frame));
					
					List<Ball> balls = ballService.getBalls(frame);
					ballService.drawBalls(frame, balls);
					updateImageView(ContourImage, frame);
					
					String text = "";
					for (Ball ball : balls) {
						text += ball.point.x + ", "+ ball.point.y + ": " + ball.area+"\n";
					}
					bugValues.setText(text);
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
	
	
	@FXML
	private void saveClick() {
		
		config.saveBall(settings);
		super.save();
	}
}
