package boot.ball;

import org.opencv.core.Mat;

import boot.BaseController;
import camera.Camera;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class BallPreviewController extends BaseController{
	
	@FXML
	ImageView ContourImage;
	
	@FXML
	ImageView TransformedImage;
				
	@FXML
	public void initialize() {
		
		if(this.camera.init()) {
			this.imageViewProperties(ContourImage, 500);
			this.imageViewProperties(TransformedImage, 500);
			
			Mat frame = this.camera.grabFrame();
			this.updateImageView(ContourImage, frame);
			this.updateImageView(TransformedImage, frame);
		}
	}

}
