package entities;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Contact implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6618753647845993843L;
	
	private String name;
	private String phoneNumber;
	private Image image;
	
	
	public Contact(String name, String phoneNumber) {
		setName(name);
		setPhoneNumber(phoneNumber);
		image = null;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	@Override
	public String toString() {
		return "Name: " + name + ", Phone Number: " + phoneNumber;
	}
	
	public void setImage(Image image) {
		this.image = image;
	}
	
	public File getImage() throws IOException, NullPointerException {
		if(image != null) {
			return image.getImage();
		}else
			throw new NullPointerException("No Image To Contact");
	}
	
	
	
}
