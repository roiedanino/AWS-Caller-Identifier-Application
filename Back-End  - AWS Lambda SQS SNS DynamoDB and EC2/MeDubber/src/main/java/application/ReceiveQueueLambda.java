package application;

import java.util.Arrays;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

public class ReceiveQueueLambda implements RequestHandler<SQSEvent, Void> {

	@Override
	public Void handleRequest(SQSEvent event, Context context) {
		try {
			QueueController queueController = new QueueController(QueueData.queueName, QueueData.applicationQueueName);
			for (SQSMessage message : event.getRecords()) {
				context.getLogger().log("ReceiveQueueLambda: " + message.getBody());
				context.getLogger().log("ReceiveQueueLambda:" + message.getMessageAttributes().toString());
				context.getLogger().log("ReceiveQueueLambda: Before execute Task");
				queueController.executeTaskFromQueue(message, context);
				
				context.getLogger().log("ReceiveQueueLambda: After execute Task");
			}
		} catch (Exception e) {
			context.getLogger().log("Execption: \n" + e );
			context.getLogger().log("Execption Message: \n" + e.getMessage() );
			context.getLogger().log("Execption Cause: \n" + e.getCause() );
			context.getLogger().log("Execption StackTrace: \n" + Arrays.deepToString(e.getStackTrace()));
		}
		return null;
	}

}
