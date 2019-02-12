package entities;

import java.io.File;
import java.io.IOException;

public class UserImage {
	private String phoneNumber;
	private Image image;
	
	public UserImage(String phoneNumber,String imageFileType, byte[] imageData) {
		image = new Image(imageFileType, imageData);
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public File getImage() throws IOException {
		return image.getImage();
	}
	
}
