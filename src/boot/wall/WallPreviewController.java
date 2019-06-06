package boot.wall;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import boot.BaseController;
import config.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import models.Wall;
import models.WallSettings;
import services.WallService;

public class WallPreviewController extends BaseController{


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
	Slider minArea;

	private WallSettings settings;

	private Config config;
	
	
	@FXML
	public void initialize() {

		config = new Config();
		settings = config.loadWall();

		
		saturationStart.setValue(settings.image.saturation.start);
		saturationStop.setValue(settings.image.saturation.stop);

		hueStart.setValue(settings.image.hue.start);
		hueStop.setValue(settings.image.hue.stop);

		valueStart.setValue(settings.image.value.start);
		valueStop.setValue(settings.image.value.stop);

		blur.setValue(settings.image.blur);

		threshold1.setValue(settings.threshold1);
		threshold2.setValue(settings.threshold2);
		
		minArea.setValue(settings.minArea);

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
					
					settings.threshold1 = threshold1.getValue();
					settings.threshold2 = threshold2.getValue();

					settings.minArea = minArea.getValue();
					
					WallService wallService = new WallService(settings);
				
					Mat frame = camera.grabFrame();
					
					updateImageView(TransformedImage, wallService.getWallFrame(frame));
					
					
					Wall wall = wallService.getWall(frame);
				
					if(wall != null) {
						wallService.drawWall(frame, wall);
					}
					updateImageView(ContourImage, frame);
					
					/*String text = "";
					for (Ball ball : balls) {
						text += ball.point.x + ", "+ ball.point.y + ": " + ball.area+"\n";
					}*/
					bugValues.setText(minArea.getValue() + "");
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
		
		config.saveWall(settings);
		super.save();
	}
}
