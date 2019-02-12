package ec2SavingImageToS3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.time.LocalDateTime;

import com.google.gson.Gson;

import application.S3Controller;

public class MeDubberClientHandler implements Runnable {
	
	private Socket clientSocket;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private Gson gson = new Gson();
	private S3Controller s3Controller;

	private boolean closed;

	public MeDubberClientHandler(Socket clientSocket, DataInputStream objectInputStream,
			DataOutputStream objectOutputStream) {
		setClientSocket(clientSocket);
		setDataInputStream(objectInputStream);
		setDataOutputStream(objectOutputStream);
		s3Controller = new S3Controller();
	}

	@Override
	public void run() {
		String request = "";
		MeDubberEC2Message message = null;
		while (!isClosed()) {
			try {
				request = dataInputStream.readUTF();
				message = gson.fromJson(request, MeDubberEC2Message.class);
				
				System.out.println(LocalDateTime.now() + " " + request);
				
				handleRequest(message);

			} catch (Exception ex) {
				System.err.println(request + "\n" + ex.getMessage());
				break;
			}
		}
		close();
	}

	private void handleRequest(MeDubberEC2Message request) throws IOException {
		MeDubberEC2Message.MessageType type = request.getType();
		String phoneNumber = request.getPhoneNumber();

		switch (type) {
			case CloseConnection:
				close();
				break;

			case RequestProfileImage:
				sendProfileImageToUser(phoneNumber);
				break;
				
			case UploadProfileImage:
				byte imageData[] = request.getData();
				saveImageToS3(phoneNumber, imageData);
				break;
				
			default:
				break;

		}
	}

	public void sendProfileImageToUser(String phoneNumber) throws IOException {
		try {

			byte imageData[] = new byte[MeDubberEC2Message.IMAGE_MAX_SIZE];
			InputStream inputStream = s3Controller.getImageInputStream(phoneNumber);
			DataInputStream fileReader = new DataInputStream(inputStream);
			fileReader.readFully(imageData);
			dataOutputStream.write(imageData);
			dataOutputStream.flush();
		}catch(NullPointerException nullPointerException) {
			System.out.println(nullPointerException.getMessage());
			dataOutputStream.write(new byte[0]);
		}
	}
	
	public void saveImageToS3(String phoneNumber, byte imageData[]) throws IOException {
		File file = File.createTempFile("ImageData", ".bin");
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(imageData);
		fileOutputStream.close();
		s3Controller.saveImage(phoneNumber, file);
		file.delete();
	}
	
	public void close() {
		try {
			clientSocket.close();
			dataInputStream.close();
			dataOutputStream.close();
			closed = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	private void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public DataInputStream getDataInputStream() {
		return dataInputStream;
	}

	private void setDataInputStream(DataInputStream dataInputStream) {
		this.dataInputStream = dataInputStream;
	}

	public DataOutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	private void setDataOutputStream(DataOutputStream dataOutputStream) {
		this.dataOutputStream = dataOutputStream;
	}

	public boolean isClosed() {
		return closed;
	}

}
