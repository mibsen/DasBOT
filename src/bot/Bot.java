package bot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import boot.Utils;
import bot.messages.ResponseReceiver;
import bot.states.RandomDrive;
import bot.states.State;
import camera.CameraFake;
import camera.CameraInterface;
import config.Config;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import services.BallService;
import services.CarService;
import services.WallService;

public class Bot extends Application implements ResponseReceiver {

	@FXML
	ImageView originalFrame;

	@FXML
	ImageView maskImage;

	private State state;
	private Connection connection;

	@FXML
	public void initialize() {

		// Create Services
		Config c = new Config();

		BallService ballService = new BallService(c.loadBall());
		WallService wallService = new WallService(c.loadWall());
		CarService carService = new CarService(c.loadCar());

		// Build first State
		state = new RandomDrive(carService, ballService, wallService);

		// Create Connection
		connection = new Connection("192.168.43.142", 4444);

		// Listen for communication from the CAR
		connection.onResponse(this);

		//
		connection.connect();

		// Start Image Loop
		final CameraInterface camera = new CameraFake();

		camera.init();

		Runnable frameGrabber = new Runnable() {

			@Override
			public void run() {

				Mat frame = camera.grabFrame();

				Mat f = state.process(frame).getFrame();

				updateImageView(originalFrame, f);
				updateImageView(maskImage, frame);

			}
		};

		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

	}

	@Override
	public void receive(String message) {
		System.out.println("Received message: " + message);
		state.handle(message);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("DATBOT 0.1");
		FXMLLoader loader = new FXMLLoader(getClass().getResource("bot.fxml"));
		TitledPane root = (TitledPane) loader.load();
		Scene scene = new Scene(root, 1000, 800);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public void load() {

		try {
			start(new Stage());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * System.loadLibrary(Core.NATIVE_LIBRARY_NAME); launch();
		 */
	}

	public void updateImageView(ImageView view, Mat image) {

		Utils.onFXThread(view.imageProperty(), Utils.mat2Image(image));
	}

}
