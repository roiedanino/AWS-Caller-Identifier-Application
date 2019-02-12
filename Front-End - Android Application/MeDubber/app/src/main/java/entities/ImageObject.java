package entities;

import java.io.File;

public class ImageObject {
	private String phoneNumber;
	private File image;
	
	public ImageObject(String phoneNumber, File image) {
		this.image = image;
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public File getImage() {
		return image;
	}
	
	
}
