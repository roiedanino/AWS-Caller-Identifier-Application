package ec2;

import java.util.Arrays;


import application.QueueController;
import application.QueueData;

public class EC2ExecuteFromQueue {

	public static void main(String[] args) {
		QueueController queueController = new QueueController(QueueData.queueName, QueueData.applicationQueueName);

		while (true) {
			try {
				queueController.executeTaskFromQueue();

			} catch (Exception e) {
				System.out.println("Execption: \n" + e);
				System.out.println("Execption Message: \n" + e.getMessage());
				System.out.println("Execption Cause: \n" + e.getCause());
				System.out.println("Execption StackTrace: \n" + Arrays.deepToString(e.getStackTrace()));
				e.printStackTrace();
			}
		}
	}

}
