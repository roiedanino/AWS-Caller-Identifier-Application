package ec2SavingImageToS3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.google.gson.Gson;

import ec2SavingImageToS3.MeDubberEC2Message.MessageType;

public class TestServer {
	public static void main(String[] args) throws UnknownHostException, IOException {
		Gson gson = new Gson();
		Socket socket = new Socket("10.0.0.17", 9999);
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		File file = new File("/Users/roie/Documents/Xiaomi Redmi 5 Backup/DCIM/Camera/IMG_20180917_165552.jpg");
		System.out.println("Client Socket is open");

		String fakePhoneNumber = "+123456789";
		byte imageData[] = new byte[(int)file.length()];
		byte result[] = new byte[(int)file.length()];
		
		DataInputStream fileStream = new DataInputStream(new FileInputStream(file));
		
		fileStream.readFully(imageData);
		dataOutputStream.writeBytes(gson.toJson(new MeDubberEC2Message(MessageType.UploadProfileImage, imageData, fakePhoneNumber)));
		
		dataOutputStream.writeUTF(gson.toJson(new MeDubberEC2Message(MessageType.RequestProfileImage, fakePhoneNumber)));
		
		dataInputStream.readFully(result);
		
		dataOutputStream.writeUTF(gson.toJson(new MeDubberEC2Message(MessageType.CloseConnection, fakePhoneNumber)));
		
		System.out.println("Is data valid? " + Arrays.equals(imageData, result));
		
		dataOutputStream.close();
		dataInputStream.close();
		socket.close();
		fileStream.close();
	}
}
