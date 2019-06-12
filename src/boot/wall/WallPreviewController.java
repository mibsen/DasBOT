package boot.wall;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import boot.BaseController;
import config.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import models.ObstacleSettings;
import models.Wall;
import models.WallSettings;
import services.WallService;

public class WallPreviewController extends BaseController {

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
	Slider threshold1;

	@FXML
	Slider threshold2;

	@FXML
	TextArea bugValues;

	@FXML
	Slider minWallArea;

	@FXML
	Slider minObstacleArea;

	@FXML
	Slider maxObstacleArea;

	@FXML
	TextField height;

	private WallSettings wallSettings;
	private ObstacleSettings obstacleSettings;

	private Config config;

	@SuppressWarnings("restriction")
	@FXML
	public void initialize() {

		config = new Config();
		wallSettings = config.loadWall();
		obstacleSettings = config.loadObstacle();

		saturationStart.setValue(wallSettings.image.saturation.start);
		saturationStop.setValue(wallSettings.image.saturation.stop);

		hueStart.setValue(wallSettings.image.hue.start);
		hueStop.setValue(wallSettings.image.hue.stop);

		valueStart.setValue(wallSettings.image.value.start);
		valueStop.setValue(wallSettings.image.value.stop);

		blur.setValue(wallSettings.image.blur);

		threshold1.setValue(wallSettings.threshold1);
		threshold2.setValue(wallSettings.threshold2);

		minWallArea.setValue(wallSettings.minArea);

		minObstacleArea.setValue(obstacleSettings.minArea);
		maxObstacleArea.setValue(obstacleSettings.maxArea);

		height.setText("" + wallSettings.camHeight);

		if (this.camera.init()) {
			// set a fixed width for all the image to show and preserve image ratio
			this.imageViewProperties(ContourImage, 500);
			this.imageViewProperties(TransformedImage, 500);

			// grab a frame every 33 ms (30 frames/sec)
			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {

					setSettings();

					WallService wallService = new WallService(wallSettings, obstacleSettings);
					// ObstacleService obstacleService = new ObstacleService(obstacleSettings);

					Mat frame = camera.grabFrame();

					updateImageView(TransformedImage, wallService.getWallFrame(frame));

					frame  = wallService.locateWallsAndCorrectFrame(frame);
					Wall wall = wallService.getWall();

					Wall obstacle = wallService.getObstacle();
					// Obstacle obstacle = obstacleService.getObstacle(frame);

					if (wall != null) {
						wallService.drawWall(frame, wall);
						Imgproc.drawMarker(frame, WallService.imageCenter, new Scalar(255, 255, 0),10);
						Imgproc.drawMarker(frame, wall.center, new Scalar(255, 0, 0),10);
					}

					if (obstacle != null)
						wallService.drawWall(frame, obstacle);

					/*
					 * if(obstacle != null) { obstacleService.drawObstacle(frame, obstacle); }
					 */
					updateImageView(ContourImage, frame);

					/*
					 * String text = ""; for (Ball ball : balls) { text += ball.point.x + ", "+
					 * ball.point.y + ": " + ball.area+"\n"; }
					 */
					bugValues.setText(minWallArea.getValue() + "");
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

		config.saveWall(wallSettings);
		config.saveObstacle(obstacleSettings);
		super.save();
	}

	@SuppressWarnings("restriction")
	private void setSettings() {

		// wall settings
		wallSettings.image.hue.start = hueStart.getValue();
		wallSettings.image.hue.stop = hueStop.getValue();

		wallSettings.image.saturation.start = saturationStart.getValue();
		wallSettings.image.saturation.stop = saturationStop.getValue();

		wallSettings.image.value.start = valueStart.getValue();
		wallSettings.image.value.stop = valueStop.getValue();

		wallSettings.image.blur = blur.getValue();

		wallSettings.threshold1 = threshold1.getValue();
		wallSettings.threshold2 = threshold2.getValue();

		wallSettings.minArea = minWallArea.getValue();

		wallSettings.camHeight = Double.parseDouble(height.getText());

		// Obstacle settings
		obstacleSettings.image.hue.start = hueStart.getValue();
		obstacleSettings.image.hue.stop = hueStop.getValue();

		obstacleSettings.image.saturation.start = saturationStart.getValue();
		obstacleSettings.image.saturation.stop = saturationStop.getValue();

		obstacleSettings.image.value.start = valueStart.getValue();
		obstacleSettings.image.value.stop = valueStop.getValue();

		obstacleSettings.image.blur = blur.getValue();

		obstacleSettings.threshold1 = threshold1.getValue();
		obstacleSettings.threshold2 = threshold2.getValue();

		obstacleSettings.minArea = minObstacleArea.getValue();
		obstacleSettings.maxArea = maxObstacleArea.getValue();

	}
}
