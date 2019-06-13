package bot;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import boot.Utils;
import bot.messages.ResponseReceiver;
import bot.states.CollectBalls;
import bot.states.EasyCollect;
import bot.states.ObstacleDrive;
import bot.states.EasyDrive;
import bot.states.RandomDrive;
import bot.states.ScoreGoals;
import bot.states.State;
import camera.Camera;
import camera.CameraFake;
import camera.CameraInterface;
import config.Config;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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

	CameraInterface camera;

	public static boolean test = false;

	public boolean skip = false;

	public Bot(boolean skip) {
		this.skip = skip;
	}

	public Bot() {
	}
	
	LinkedList<State> collectBallsState;

	private ScoreGoals scoreGoalState;

	public static EasyDrive easyDriveState;
	public static ObstacleDrive driveObstacleState;

	public static EasyCollect easyCollectState;

	@FXML
	public void initialize() {

		collectBallsState = new LinkedList<State>();
		// Create Services
		Config c = new Config();

		BallService ballService = new BallService(c.loadBall());
		WallService wallService = new WallService(c.loadWall(), c.loadObstacle());
		CarService carService = new CarService(c.loadCar());


		//Builds collect states
		easyCollectState = new EasyCollect(carService, ballService, wallService);
		
		// Builds drive states
		driveObstacleState = new ObstacleDrive(carService, ballService, wallService);
		easyDriveState = new EasyDrive(carService, ballService, wallService);
		
		// Build hand in balls state
		scoreGoalState = new ScoreGoals(carService, ballService, wallService);
		

		state = easyDriveState;
		
		
		System.out.println("Initializing BOT TEST:" + test);

		if (!test) {
			// Create Connection
			connection = new Connection("172.20.10.5", 4444);

			// Listen for communication from the CAR
			connection.onResponse(this);

			//
			connection.connect();
			camera = new Camera();
			camera.init();

		} else {
			camera = new CameraFake();
			camera.init();
		}

		// Start Image Loop
		Runnable frameGrabber = new Runnable() {

			@Override
			public void run() {
				Mat frame = camera.grabFrame();
				

				Mat f = state.process(frame).getFrame();

				if (state.isDone) {
					System.out.println("OldState: " + (state == null ? "empty" : state.toString()));
					state.isDone = false;
					state.running = null;
					state = nextState();
					System.out.println("NewState: " + (state == null ? "empty" : state.toString()));
				}
				
				if (f != null)
					updateImageView(originalFrame, f);

				updateImageView(maskImage, frame);

			}
		};

		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

	}

	protected State nextState() {
		return state.nextState;
	}

	private void Switch(String stateName) {
		// TODO Auto-generated method stub
		
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
		
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				System.exit(0);
			}
		});
	}

	public void load() {

		System.out.println("Loading BOT Frame SKIP:" + skip);

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch();
	}

	public void updateImageView(ImageView view, Mat image) {

		Utils.onFXThread(view.imageProperty(), Utils.mat2Image(image));
	}

}
