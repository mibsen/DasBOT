package application;
	
import java.awt.Dimension;
import java.awt.Toolkit;

import org.opencv.core.Core;

import boot.ball.BallPreviewController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../boot/ball/BallPreview.fxml"));
			TitledPane root = (TitledPane) loader.load();
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			
			Scene scene = new Scene(root,screenSize.getWidth(), screenSize.getHeight());
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("iRobot");
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setFullScreen(true);

			BallPreviewController controller = loader.getController();
			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we)
				{
					controller.setClosed();
					System.exit(0);
				}
			}));
			
			controller.initialize();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
