package ec2SavingImageToS3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MeDubberEC2Server implements Runnable {
	private static final int PORT = 9999;
	private static MeDubberEC2Server instance;

   
	private ServerSocket serverSocket;	
	private Set<MeDubberClientHandler> clients = new HashSet<>();
    private ExecutorService executorService;
   
    private MeDubberEC2Server() throws IOException {
		serverSocket = new ServerSocket(PORT);
        executorService = Executors.newCachedThreadPool();
        
	}

    static {
        try {
            instance = new MeDubberEC2Server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static MeDubberEC2Server getInstance(){
    	if(instance == null){
			try {
				instance = new MeDubberEC2Server();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
        return instance;
    }

	
	
	@Override
	public void run() {
		while(true) {
			 try {
				 System.out.println(LocalDateTime.now() +" Listening on port:" + PORT +" with IP address:" + serverSocket.getInetAddress());
	             Socket clientSocket = serverSocket.accept();
				 System.out.println(LocalDateTime.now() +" Connected to:"+ clientSocket.getInetAddress().getHostAddress()+":" + clientSocket.getPort());

	             DataInputStream objectInputStream = new DataInputStream(clientSocket.getInputStream());
	             DataOutputStream objectOutputStream = new DataOutputStream(clientSocket.getOutputStream());

	             MeDubberClientHandler clientHandler = new MeDubberClientHandler( clientSocket, objectInputStream, objectOutputStream);
	             executorService.execute(clientHandler);
	             clients.add(clientHandler);

	        } catch (IOException e) {
	            e.printStackTrace();
	            closeClients();
	            break;
	        }
		}
		 System.out.println(LocalDateTime.now() +" Closed successfully");

	}

	 private void closeClients(){
	        for(MeDubberClientHandler warClientHandler : clients){
	            warClientHandler.close();
	        }
	    }
}
