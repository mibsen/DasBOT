package boot.wall;

import boot.BaseController;
import config.Config;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import models.WallSettings;

public class WallPreviewController extends BaseController{

	@FXML
	ImageView ContourImage;

	@FXML
	ImageView TransformedImage;

	private Config config;

	private WallSettings settings;

	@FXML
	public void initialize() {

		config = new Config();
		settings = config.loadWall();

		
	}
	
	
	@FXML
	private void saveClick() {
		config.saveWall(settings);
		super.save();
	}
}
