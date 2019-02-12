

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;

import application.QueueData;
import entities.Contact;
import entities.UserImage;

public class MainApplication {
	
	public static void main(String[] args) {
		Gson gson = new Gson();
		AWSCredentials credentials = getCredentials();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		credentialsProvider.getCredentials();
       
		try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/Users/orgivati/.aws/credentials), and is in valid format.",
                    e);
        }
		
        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_WEST_2)
                .build();
        
        final CreateQueueRequest createQueueRequest =
                new CreateQueueRequest(QueueData.queueName);
        String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
        final CreateQueueRequest createQueueRequest2 = new CreateQueueRequest(QueueData.applicationQueueName);
        String applicationQueueURL = sqs.createQueue(createQueueRequest2).getQueueUrl();
        
        final Map<String, MessageAttributeValue> saveContactAttribute = new HashMap<>();
        saveContactAttribute.put(QueueData.AttributesNames.TaskAttribute.getName(), new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.SaveContactTask.getName()));
        
        saveContactAttribute.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(), QueueData.getWhoRequestedAttributeValue("+972549983002"));
       
        final Map<String, MessageAttributeValue> saveImageAttribute = new HashMap<>();
        saveImageAttribute.put(QueueData.AttributesNames.TaskAttribute.getName(), new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.SaveImageTask.getName()));
        saveImageAttribute.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(), QueueData.getWhoRequestedAttributeValue("+972549983002"));
        File file = new File("/Users/roie/Downloads/Active-Components-STM32F070CBT6-ARM-based-32-bit.jpg_50x50.jpg");
        byte imageData[] = new byte[(int)file.length()];
		byte result[] = new byte[(int)file.length()];
		
		DataInputStream fileStream;
		try {
			fileStream = new DataInputStream(new FileInputStream(file));
			fileStream.readFully(imageData);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
        UserImage imageObject= new UserImage("+972549983002", ".jpg",imageData);
        Contact contact = new Contact("Alis", "+972549983002");
        Contact contact2 = new Contact("Roie", "+972525798315");
                
        SendMessageRequest saveImageRequest = new SendMessageRequest(myQueueUrl, gson.toJson(imageObject)).withMessageAttributes(saveImageAttribute);
        SendMessageRequest saveContactRequest = new SendMessageRequest(myQueueUrl, gson.toJson(contact)).withMessageAttributes(saveContactAttribute);
        SendMessageRequest saveContactRequest2 = new SendMessageRequest(myQueueUrl, gson.toJson(contact2)).withMessageAttributes(saveContactAttribute);
        
        sqs.sendMessage(saveContactRequest2);
        sqs.sendMessage(saveContactRequest);
        sqs.sendMessage(saveImageRequest);
   /*     final Map<String, MessageAttributeValue> getContactAttribute = new HashMap<>();
        getContactAttribute.put(QueueData.AttributesNames.TaskAttribute.getName(), new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.GetContactTask.getName()));
        getContactAttribute.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(), QueueData.getWhoRequestedAttributeValue("0547777"));
        
        SendMessageRequest getContactRequest = new SendMessageRequest(myQueueUrl, "0549983002").withMessageAttributes(getContactAttribute);
        sqs.sendMessage(getContactRequest);
     */
	}
	
	public static AWSCredentials getCredentials() {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/roie/.aws/credentials), and is in valid format.", e);
		}
		return credentials;
	}

}
