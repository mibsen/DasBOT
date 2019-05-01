package camera;

import org.opencv.core.Mat;

public interface CameraInterface {

	public boolean isOpened();
	
	public void release();
	
	public Mat grabFrame();

	public boolean init();
	
}
