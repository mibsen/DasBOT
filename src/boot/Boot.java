package boot;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Boot extends Application {

	Queue<String> steps;
	private static Callable after;

	public void load() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch();
	}

	public void setScene(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(steps.poll()));
		TitledPane root = (TitledPane) loader.load();
		Scene scene = new Scene(root, 1200, 800);
		primaryStage.setScene(scene);
		primaryStage.show();

		BaseController controller = loader.getController();

		controller.onSave(new Callable() {

			@Override
			public Object call() throws Exception {

				// We are done!
				if (steps.peek() == null) {
					controller.setClosed();
					primaryStage.close();
					
					if(after != null) {
						after.call();
					}

					return null;
				}

				setScene(primaryStage);

				return null;
			}
		});

		primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				controller.setClosed();
				System.exit(0);
			}
		}));
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		if (steps == null) {
			System.out.println("CALLED!");
			steps = new LinkedList<>();
			//steps.add("./ball/BallPreview.fxml");
			steps.add("./wall/WallPreview.fxml");
			steps.add("./car/CarPreview.fxml");
		}

		try {

			primaryStage.setTitle("boot");

			setScene(primaryStage);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void after(Callable callable) {

		after = callable;

	}

}
