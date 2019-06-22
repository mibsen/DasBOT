package bot;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import boot.Utils;
import bot.messages.Messages;
import bot.messages.ResponseReceiver;
import bot.states.EasyCollect;
import bot.states.EasyDrive;
import bot.states.FinishState;
import bot.states.ObstacleDrive;
import bot.states.CheckState;
import bot.states.CirculateDrive;
import bot.states.ScoreGoals;
import bot.states.StartState;
import bot.states.State;
import bot.states.TurnDegreeTest;
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
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
	
	@FXML
	Label elapsedTime;
	
	@FXML
	Button beginBtn;
	
	@FXML 
	Slider goalSlider;
	
	@FXML
	Label goalLabel;

	public static State state;
	
	private Connection connection;

	CameraInterface camera;

	public static boolean test = false;

	public boolean skip = false;

	private BallService ballService;

	private WallService wallService;

	private CarService carService;
	public static int BALL_COUNTER = 0;
	public static boolean ALL_BALLS_COLLECTED = false;
	
	public static long RUNTIME_IN_MS = 0;
	public static final long SEVEN_MINUTES_RUNTIME = 360000;
	public static int GOAL_POSITION = 0; // 0 = left, 1 = right
	
	public static boolean DONE = false;
	public static boolean hasScored = false;


	public Bot(boolean skip) {
		this.skip = skip;
	}

	public Bot() {
	}
	
	
	
	
	public static EasyCollect easyCollectState;

	@FXML
	public void initialize() {

		// Create Services
		Config c = new Config();

		ballService = new BallService(c.loadBall());
		wallService = new WallService(c.loadWall(), c.loadObstacle());
		carService = new CarService(c.loadCar());

		//Builds collect states
		state = new StartState(carService, ballService, wallService);
		
		System.out.println("Initializing BOT TEST:" + test);

		if (!test) {
			// Create Connection
			// 172.20.10.5
			// 192.168.43.142
			connection = new Connection("192.168.43.142", 44444);

			// Listen for communication from the CAR
			connection.onResponse(this);

			//
			connection.connect();
			camera = new Camera();
			camera.init();

		} else {
			camera = new Camera();
			camera.init();
		}

		// Start Image Loop
		Runnable frameGrabber = new Runnable() {

			@Override
			public void run() {
				Mat frame = camera.grabFrame();
				Mat f = state.process(frame).getFrame();
				
				if (f != null) {
					updateImageView(originalFrame, f);
				}

				updateImageView(maskImage, frame);

			}
		};

		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

	}

	@Override
	public void receive(String message) {

		System.out.println("----- MESSAGE START  ------");
		System.out.println("Received message: " + message);
		System.out.println("----- MESSAGE END  ------");
		
		if (message.equals(Messages.COLLECTED)) {
			Bot.BALL_COUNTER++;
		}
		if (message.equals(Messages.FINISHED)) {
			System.out.println("ALL BALLS COLLECTED!");
			Bot.ALL_BALLS_COLLECTED = true;
		}
		
		
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
		
		if(RUNTIME_IN_MS != 0 && !DONE) {
			String s = new Timestamp(System.currentTimeMillis() - RUNTIME_IN_MS).toLocaleString().substring(14);
			Utils.updateElapsedTime(elapsedTime, s);
		}
		Utils.onFXThread(view.imageProperty(), Utils.mat2Image(image));
	}

	public void beginRobot() {

		RUNTIME_IN_MS = System.currentTimeMillis();
		
		state = new CheckState(carService, ballService, wallService);
		

		System.out.println("GOAL POSITION: " + GOAL_POSITION);
		System.out.println("GOAL POSITION: " + (GOAL_POSITION == 0 ? "left" : "right"));
		
		Utils.changeBtnVisibility(beginBtn, false);
		Utils.changeSliderVisibility(goalSlider, false);
	}
	
	public void handleDrag() {
		System.out.println("SLIDER DRAGGED");
		System.out.println("Slider value: " + goalSlider.getValue());
		GOAL_POSITION = goalSlider.getValue() < 0.5 ? 0 : 1;

		Utils.changeLabelText(goalLabel, (GOAL_POSITION == 0 ? "left" : "right"));
		
		System.out.println("GOAL POSITION: " + GOAL_POSITION);
		System.out.println("GOAL POSITION: " + (GOAL_POSITION == 0 ? "left" : "right"));
	}
	
}
