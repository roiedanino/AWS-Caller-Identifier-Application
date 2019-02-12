package dal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.greengrass.model.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import application.DBService;
import application.DBServiceImplementation;
import application.QueueData;
import entities.RegisterdUser;

public class DynamoDBLambda implements RequestHandler<DynamodbEvent, Void> {

	private DBService dbService = new DBServiceImplementation();

	public void sendSMS(String name, String newNickname, String phoneNumber, Context context) {
		AmazonSNS snsClient = AmazonSNSClient.builder().withRegion(Regions.US_WEST_2).build();

		RegisterdUser registerdUser = dbService.getRegisterdUser(phoneNumber);

		if (registerdUser == null)
			return;

		final String topicARN = registerdUser.getTopicArn();
		final String msg = "Hello " + name + "!\nYou got a new nickname on MeDubber:\n" + newNickname;

		/*Map<String, MessageAttributeValue> smsAttributes =
		        new HashMap<String, com.amazonaws.services.sns.model.MessageAttributeValue>();
		smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
		        .withStringValue("MeDubber") //The sender ID shown on the device.
		        .withDataType("String"));
		*/
		PublishRequest publishRequest = new PublishRequest()
				.withTopicArn(topicARN)
				.withMessage(msg)
				.withSubject("MeDubber");

		context.getLogger().log("Publish SMS: " + name + " new nickname:" + " phone number:" + phoneNumber);
		PublishResult publishResult = snsClient.publish(publishRequest);

		context.getLogger().log("MessageId - " + publishResult.getMessageId() + " Message Content: " + msg);

	}

	@Override
	public Void handleRequest(DynamodbEvent event, Context context) {
		try {
			AmazonSQS queue = AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
			String quequeURL = queue.getQueueUrl(QueueData.applicationQueueName).getQueueUrl();

			LambdaLogger logger = context.getLogger();

			for (DynamodbStreamRecord record : event.getRecords()) {
				StreamRecord streamRecord = record.getDynamodb();
				String phoneNumber = streamRecord.getNewImage().get("phoneNumber").getS();
				logger.log("Phone Number: " + phoneNumber);
				String name = streamRecord.getNewImage().get("mostCommonNickname").getS();
				logger.log("Most Common Nickname: " + name);
				logger.log("Old Image: " + streamRecord.getOldImage());
				logger.log("New Image: " + streamRecord.getNewImage());

				Set<String> newNicknames = streamRecord.getNewImage().get("nicknames").getM().keySet();
				Map<String, AttributeValue> oldImage = streamRecord.getOldImage();
				if (oldImage != null) {
					Set<String> oldNicknames = oldImage.get("nicknames").getM().keySet();
					newNicknames.removeAll(oldNicknames);
				}
				logger.log("New nicknames: " + newNicknames);
				for (String nickname : newNicknames) {
					HashMap<String, MessageAttributeValue> attributes = new HashMap<>();
					attributes.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(),
							QueueData.getWhoRequestedAttributeValue(phoneNumber));
					attributes.put(QueueData.AttributesNames.TaskAttribute.getName(),
							QueueData.getNewNicknameAttributeValue());
					SendMessageRequest request = new SendMessageRequest(quequeURL, nickname)
							.withMessageAttributes(attributes);
					queue.sendMessage(request);
					logger.log("Sends SMS: " + name + " new nickname:" + nickname + " phone number:" + phoneNumber);
					sendSMS(name, nickname, phoneNumber, context);
				}
			}

		} catch (Exception ex) {
			context.getLogger().log(ex.toString());
		}
		return null;
	}
}