package boot.car;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import application.MainBot;
import boot.BaseController;
import bot.Bot;
import config.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import models.Car;
import models.CarSettings;
import services.CarService;
import services.WallService;

public class CarPreviewController extends BaseController {

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
	Slider backMin;

	@FXML
	Slider backMax;

	@FXML
	Slider frontMin;

	@FXML
	Slider frontMax;

	@FXML
	TextArea bugValues;

	private CarSettings settings;

	private Config config;

	@FXML
	public void initialize() {

		config = new Config();
		settings = config.loadCar();

		saturationStart.setValue(settings.image.saturation.start);
		saturationStop.setValue(settings.image.saturation.stop);

		hueStart.setValue(settings.image.hue.start);
		hueStop.setValue(settings.image.hue.stop);

		valueStart.setValue(settings.image.value.start);
		valueStop.setValue(settings.image.value.stop);

		blur.setValue(settings.image.blur);

		backMin.setValue(settings.back.start);
		backMax.setValue(settings.back.stop);

		frontMin.setValue(settings.front.start);
		frontMax.setValue(settings.front.stop);

		// set front and back

		if (this.camera.init()) {

			// set a fixed width for all the image to show and preserve image ratio
			this.imageViewProperties(ContourImage, 500);
			this.imageViewProperties(TransformedImage, 500);

			
			WallService wallService = new WallService(config.loadWall(), config.loadObstacle());
			
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

					settings.back.start = backMin.getValue();
					settings.back.stop = backMax.getValue();

					settings.front.start = frontMin.getValue();
					settings.front.stop = frontMax.getValue();

					CarService carService = new CarService(settings);
					carService.debug = true;

					Mat frame = camera.grabFrame();

					frame = wallService.locateWallsAndCorrectFrame(frame);
					
					updateImageView(TransformedImage, carService.getCarFrame(frame));

					Car car = carService.getCar(frame);

					String text = "";

					if (car != null) {
						carService.drawCar(frame, car);

						text = "Back: " + car.back.x + ", " + car.back.y + "\n" + "Center: " + car.center.x + ", "
								+ car.center.y + "\n" + "Front: " + car.front.x + ", " + car.front.y;
					}
					updateImageView(ContourImage, frame);

					bugValues.setText(text);
				};
			};
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 200, TimeUnit.MILLISECONDS);

		} else {

			this.cameraActive = false;

			// stop the timer
			this.stopAcquisition();

		}
	}

	@FXML
	private void saveClick() {
		config.saveCar(settings);
		super.save();
	}
}
