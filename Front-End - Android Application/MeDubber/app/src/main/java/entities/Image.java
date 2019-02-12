package entities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.util.IOUtils;

public class Image {
	
	private String imageFileType;
	private byte[] imageData;
	
	public Image(String imageFileType, byte[] imageData) {
		this.imageData = imageData;
		this.imageFileType = imageFileType;
	}
	
	public Image(InputStream inputStream) throws IOException {
		imageData = IOUtils.toByteArray(inputStream);
		imageFileType = ".jpg";
	}
	
	
	public File getImage(String directoryPath) throws IOException {
		File file = File.createTempFile(directoryPath + File.separator + "ImageData", imageFileType);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(imageData);
		fileOutputStream.close();
		return file;
	}

	public byte[] getImageData() {
		return imageData;
	}
}
