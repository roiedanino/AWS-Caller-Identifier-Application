package application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.regions.Regions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;

import entities.*;

public class QueueController {

	private String queueURL;
	private String applicationQueueUrl;

	private AmazonSQS queue;
	private DBService dbService;
	private S3Controller s3Controller;
	private Gson gson;

	private AmazonSNS snsClient;

	public QueueController(ProfileCredentialsProvider credentials, String ourQueueName, String applicationQueueName) {
		queue = AmazonSQSClientBuilder.standard().withCredentials(credentials).withRegion(Regions.US_WEST_2).build();
		queueURL = queue.getQueueUrl(ourQueueName).getQueueUrl();
		applicationQueueUrl = queue.getQueueUrl(applicationQueueName).getQueueUrl();
		dbService = new DBServiceImplementation();
		s3Controller = new S3Controller(credentials);
		gson = new Gson();

		snsClient = AmazonSNSClient.builder().withRegion(Regions.US_WEST_2).withCredentials(credentials).build();
	}

	public QueueController(String ourQueueName, String applicationQueueName) {
		queue = AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
		queueURL = queue.getQueueUrl(ourQueueName).getQueueUrl();
		applicationQueueUrl = queue.getQueueUrl(applicationQueueName).getQueueUrl();
		dbService = new DBServiceImplementation();
		s3Controller = new S3Controller();
		gson = new Gson();

		snsClient = AmazonSNSClient.builder().withRegion(Regions.US_WEST_2).build();
	}

	public void executeTaskFromQueue() throws NoSuchElementException {
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueURL);
		List<Message> messages = queue.receiveMessage(request.withMessageAttributeNames("All")).getMessages();
		if (!messages.isEmpty()) {
			Message message = messages.get(0);
			System.err.println("MESSAGE:: " + message);

			executeTaskFromQueue(message);
		}
	}

	public void executeTaskFromQueue(Message message) {
		String task = message.getMessageAttributes().get(QueueData.AttributesNames.TaskAttribute.getName())
				.getStringValue();
	
		System.err.println("MESSAGE:: " + message);
		
		if (task.equals(QueueData.Tasks.SaveContactTask.getName())) {
			dbService.saveContact(gson.fromJson(message.getBody(), Contact.class));

		} else if (task.equals(QueueData.Tasks.SaveImageTask.getName())) {
			saveImageToS3(message);
		} else if (task.equals(QueueData.Tasks.GetContactTask.getName())) {
			String whoRequested = message.getMessageAttributes()
					.get(QueueData.AttributesNames.WhoRequestedAttribute.getName()).getStringValue();
			String whenRequested = message.getMessageAttributes().get(QueueData.AttributesNames.RequestTimeStamp.getName()).getStringValue();
			sendContact(message, whoRequested, whenRequested);
		} else if (task.equals(QueueData.Tasks.RegisterUser.getName())) {
			String phoneNumber = message.getMessageAttributes()
					.get(QueueData.AttributesNames.WhoRequestedAttribute.getName()).getStringValue();
			if (!dbService.isUserRegisterd(phoneNumber)) {
				String arn = createTopic(phoneNumber);
				RegisterdUser registerdUser = new RegisterdUser(phoneNumber, arn);
				dbService.saveRegisterdUser(registerdUser);
				subscribeToTopic(registerdUser);
			}
		} else if (task.equals(QueueData.Tasks.GetNicknames.getName())) {
			String whoRequested = message.getMessageAttributes()
					.get(QueueData.AttributesNames.WhoRequestedAttribute.getName()).getStringValue();

			User user = dbService.getUser(whoRequested);
			sendNicknamesToApplicationQueue(user);
		}

		queue.deleteMessage(queueURL, message.getReceiptHandle());
	}

	public void executeTaskFromQueue(SQSMessage message, Context context) {
		String task = message.getMessageAttributes().get(QueueData.AttributesNames.TaskAttribute.getName())
				.getStringValue();
		
		if (task.equals(QueueData.Tasks.SaveContactTask.getName())) {
			dbService.saveContact(gson.fromJson(message.getBody(), Contact.class));

		} else if (task.equals(QueueData.Tasks.SaveImageTask.getName())) {
			saveImageToS3(message);
		} else if (task.equals(QueueData.Tasks.GetContactTask.getName())) {
			String whoRequested = message.getMessageAttributes()
					.get(QueueData.AttributesNames.WhoRequestedAttribute.getName()).getStringValue();
			
			String whenRequested = message.getMessageAttributes().get(QueueData.AttributesNames.RequestTimeStamp.getName()).getStringValue();
			sendContact(message, whoRequested, whenRequested);
		} else if (task.equals(QueueData.Tasks.RegisterUser.getName())) {
			String phoneNumber = message.getMessageAttributes()
					.get(QueueData.AttributesNames.WhoRequestedAttribute.getName()).getStringValue();
			if (!dbService.isUserRegisterd(phoneNumber)) {
				String arn = createTopic(phoneNumber);
				RegisterdUser registerdUser = new RegisterdUser(phoneNumber, arn);
				dbService.saveRegisterdUser(registerdUser);
				subscribeToTopic(registerdUser);
			}
		} else if (task.equals(QueueData.Tasks.GetNicknames.getName())) {
			String whoRequested = message.getMessageAttributes()
					.get(QueueData.AttributesNames.WhoRequestedAttribute.getName()).getStringValue();

			User user = dbService.getUser(whoRequested);
			sendNicknamesToApplicationQueue(user);
		}

		queue.deleteMessage(queueURL, message.getReceiptHandle());
	}

	private void sendContact(SQSMessage message, String whoRequested, String whenRequested) {
		String phoneNumber = message.getBody();
		sendContact(phoneNumber, whoRequested, whenRequested);
	}
	
	private void sendContact(Message message, String whoRequested, String whenRequested) {
		String phoneNumber = message.getBody();
		sendContact(phoneNumber, whoRequested, whenRequested);
	}
	
	private void sendContact(String phoneNumber, String whoRequested, String whenRequested) {
		Contact contact = dbService.getContact(phoneNumber);
		if (contact != null) {
			try {
				InputStream stream = s3Controller.getImageInputStream(phoneNumber);
				Image image = new Image(stream);
				contact.setImage(image);
			} catch (Exception e) {
				
			}
		}
		sendContactToApplicationQueue(contact, whoRequested, whenRequested);
	}

	private void saveImageToS3(SQSMessage message) {
		saveImageToS3(message.getBody());
	}

	private void saveImageToS3(Message message) {
		saveImageToS3(message.getBody());
	}
	
	private void saveImageToS3(String imageEncoding) {
		UserImage imageObject = gson.fromJson(imageEncoding, UserImage.class);
		try {
			File image = imageObject.getImage();
			s3Controller.saveImage(imageObject.getPhoneNumber(), image);
			image.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isQueueEmpty() {
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueURL).withWaitTimeSeconds(1)
				.withVisibilityTimeout(1);
		return queue.receiveMessage(request).getMessages().isEmpty();
	}

	private void sendContactToApplicationQueue(Contact contact, String whoRequested, String whenRequested) {
		HashMap<String, MessageAttributeValue> attributes = new HashMap<>();
		attributes.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(),
				QueueData.getWhoRequestedAttributeValue(whoRequested));
		attributes.put(QueueData.AttributesNames.TaskAttribute.getName(), QueueData.getGetContactAttributeValue());
		attributes.put(QueueData.AttributesNames.RequestTimeStamp.getName(), QueueData.getMessageAttributeValue(whenRequested));
		SendMessageRequest request = new SendMessageRequest(applicationQueueUrl, gson.toJson(contact))
				.withMessageAttributes(attributes);
		queue.sendMessage(request);
	}

	private void sendNicknamesToApplicationQueue(User user) {
		HashMap<String, MessageAttributeValue> attributes = new HashMap<>();
		attributes.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(),
				QueueData.getWhoRequestedAttributeValue(user.getPhoneNumber()));
		attributes.put(QueueData.AttributesNames.TaskAttribute.getName(), QueueData.getNicknamesAttributeValue());

		SendMessageRequest request = new SendMessageRequest(applicationQueueUrl, gson.toJson(user.getNicknames()))
				.withMessageAttributes(attributes);

		queue.sendMessage(request);
	}
	
	private String createTopic(String phoneNumber) {

		String topicName = "MeDubber" + phoneNumber.replace("+", "-");

		CreateTopicResult createTopicResult = snsClient.createTopic(topicName);

		return createTopicResult.getTopicArn();
	}

	private void subscribeToTopic(RegisterdUser registerdUser) {
		SubscribeRequest subRequest = new SubscribeRequest(registerdUser.getTopicArn(), "sms",
				registerdUser.getPhoneNumber());
		snsClient.subscribe(subRequest);
		// get request id for SubscribeRequest from SNS metadata
	}

}
