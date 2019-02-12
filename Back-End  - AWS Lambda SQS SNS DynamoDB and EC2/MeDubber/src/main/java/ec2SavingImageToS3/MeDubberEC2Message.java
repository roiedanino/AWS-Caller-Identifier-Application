package ec2SavingImageToS3;

import java.io.Serializable;


public class MeDubberEC2Message implements Serializable{
	enum MessageType{RequestProfileImage, UploadProfileImage, CloseConnection};

	private static final long serialVersionUID = -7649167792530895406L;
	public static final int IMAGE_MAX_SIZE = Integer.MAX_VALUE / 1024;

	
	private MessageType type;
	private String phoneNumber;
	private byte[] data;
	
	
	
	public MeDubberEC2Message(MessageType type, byte[] data) {
		setType(type);
		setData(data);
	}
	public MeDubberEC2Message(MessageType type, String phoneNumber) {
		setType(type);
		setData(new byte[0]);
		setPhoneNumber(phoneNumber);
	}
	public MeDubberEC2Message(MessageType type, byte[] data, String phoneNumber) {
		setType(type);
		setData(data);
		setPhoneNumber(phoneNumber);
	}

	public MessageType getType() {
		return type;
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
}
