package amazonSQS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import entities.Contact;
import entities.Image;
import entities.UserImage;

public class AmazonSQSClientService {

    private static AmazonSQSClientService instance = null;

    private AmazonSQS sqs;

    private String queueURL;
    private String applicationQueueURL;

    private Gson gson;

    private AmazonSQSClientService(){
        gson = new Gson();
        AWSCredentials credentials = getCredentials();

        sqs = new AmazonSQSClient(credentials);
        sqs.setRegion(Region.getRegion(Regions.US_WEST_2));

        final CreateQueueRequest createQueueRequest =
                new CreateQueueRequest(QueueData.queueName);
        queueURL = sqs.createQueue(createQueueRequest).getQueueUrl();

        final CreateQueueRequest createQueueRequest2 = new CreateQueueRequest(QueueData.applicationQueueName);
        applicationQueueURL = sqs.createQueue(createQueueRequest2).getQueueUrl();
    }

    public void uploadContacts(ArrayList<Contact> contacts){

        SendMessageBatchRequest sendBatchContacts = new SendMessageBatchRequest().withQueueUrl(queueURL);
        //sendBatchContacts.getEntries().add();

        final Map<String, MessageAttributeValue> saveContactAttribute = new HashMap<>();
        saveContactAttribute.put(QueueData.AttributesNames.TaskAttribute.getName(), new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.SaveContactTask.getName()));


        for(Contact contact : contacts){
            SendMessageBatchRequestEntry saveContactRequest = new SendMessageBatchRequestEntry(queueURL, gson.toJson(contact))
                    .withMessageAttributes(saveContactAttribute);
            sendBatchContacts.getEntries().add(saveContactRequest);

            if(contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty()) {

                SendMessageRequest sendMessageRequest = new SendMessageRequest(queueURL, gson.toJson(contact))
                        .withMessageAttributes(saveContactAttribute);
                SendMessageResult result = sqs.sendMessage(sendMessageRequest);

                Log.i("SEND MESSAGE RESULT::", result.getSequenceNumber() + " " + result.getMessageId() + " " + result.getMD5OfMessageBody());
            }
        }
    }

    public static AmazonSQSClientService getInstance(){
        if(instance == null)
            instance = new AmazonSQSClientService();

        return instance;
    }

    private void requestToFindByPhoneNumber(String phoneNumber, String myPhoneNumber, String whenRequested){
        final Map<String, MessageAttributeValue> getContactAttribute = new HashMap<>();
        getContactAttribute.put(QueueData.AttributesNames.TaskAttribute.getName(), new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.GetContactTask.getName()));
        getContactAttribute.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(), QueueData.getWhoRequestedAttributeValue(myPhoneNumber));
        getContactAttribute.put(QueueData.AttributesNames.RequestTimeStamp.getName(), QueueData.getMessageAttributeValue(whenRequested));
        SendMessageRequest getContactRequest = new SendMessageRequest(queueURL, phoneNumber).withMessageAttributes(getContactAttribute);


        Log.i("Request Result:", sqs.sendMessage(getContactRequest).toString());

    }

    private boolean findByPhoneFilter(Message message, String myPhone, String whenRequested){
        String whoRequestedAttr = QueueData.AttributesNames.WhoRequestedAttribute.getName();
        String timeStamp = QueueData.AttributesNames.RequestTimeStamp.getName();
        String requestType = QueueData.AttributesNames.TaskAttribute.getName();

        if(message.getMessageAttributes().get(whoRequestedAttr) == null || message.getMessageAttributes().get(requestType) == null
                || message.getMessageAttributes().get(timeStamp) == null)
            return false;

        String whoRequested = message.getMessageAttributes().get(whoRequestedAttr).getStringValue();
        String messageWhenRequested = message.getMessageAttributes().get(timeStamp).getStringValue();

        boolean myRequest = whoRequested.equals(myPhone);
        boolean thisRequest = message.getMessageAttributes().get(requestType).equals(QueueData.getGetContactAttributeValue())
               && messageWhenRequested.equals(whenRequested);

        return myRequest && thisRequest;
    }

    private Contact waitForRelevantResults(String phoneNumber, String myPhoneNumber, String whenRequested) {
        List<Message> messageList = null;

        Contact resultContact = null;

        int waitCounter = 0;

        do{
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            messageList = sqs.receiveMessage(new ReceiveMessageRequest()
                    .withQueueUrl(applicationQueueURL)
                    .withMessageAttributeNames("All"))
                    .getMessages();

            Stream<Message> filteredMessages = messageList.stream().filter(message -> findByPhoneFilter(message, myPhoneNumber, whenRequested));

            Optional<Message> resultMessage = filteredMessages.findFirst();

            Contact notFoundContact = new Contact("Not Found In DB", "000000");
            notFoundContact.setImage(new Image(".jpg",null));

            if(resultMessage.isPresent()){
                Message message = resultMessage.get();


                if(message.getBody() != null && !message.getBody().equals("null") && !message.getBody().isEmpty()) {
                    resultContact = gson.fromJson(message.getBody(), Contact.class);
                    String name = resultContact.getName();
                    String phone = resultContact.getPhoneNumber();

                    if ((phone.equals(phoneNumber) && name != null && !name.isEmpty())) {
                        return resultContact;
                    } else {
                        return notFoundContact;
                    }

                }else if(message.getBody().equals("null")){
                    return notFoundContact;
                }
            }else if(waitCounter > 30){
                notFoundContact.setName("Timeout");
                return notFoundContact;
            }

            waitCounter++;

        }while (true);
    }



    public Contact findByPhoneNumber(String phoneNumber, Activity activity){

        if(phoneNumber.isEmpty())
            return new Contact("Enter Phone First", "");

        String myPhoneNumber = getMyPhoneNumber(activity);

        LocalDateTime whenRequested = LocalDateTime.now();
        requestToFindByPhoneNumber(phoneNumber, myPhoneNumber, whenRequested.toString());

        return waitForRelevantResults(phoneNumber, myPhoneNumber, whenRequested.toString());
    }

    public void register(Activity activity){
        final String whoReqAttr = QueueData.AttributesNames.WhoRequestedAttribute.getName();
        final String taskAttr = QueueData.AttributesNames.TaskAttribute.getName();
        String myPhoneNumber = getMyPhoneNumber(activity);

        final Map<String, MessageAttributeValue> registerUserAttribute = new HashMap<>();
        registerUserAttribute.put(taskAttr, new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.RegisterUser.getName()));
        registerUserAttribute.put(whoReqAttr, QueueData.getWhoRequestedAttributeValue(myPhoneNumber));

        SendMessageRequest registerUserRequest = new SendMessageRequest(queueURL, gson.toJson(new Contact("RegisterdUser",myPhoneNumber)))
                .withMessageAttributes(registerUserAttribute);

        Log.i("Request Result:", sqs.sendMessage(registerUserRequest).toString());
    }

    public static String getMyPhoneNumber(Activity activity){
        String myPhoneNumber = null;
        TelephonyManager tel= (TelephonyManager)activity.getSystemService(Context.
                TELEPHONY_SERVICE);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            myPhoneNumber = tel.getLine1Number();
        }
        return myPhoneNumber;
    }

    public Map<String, Integer> getNicknames(Activity activity){
        final String whoReqAttr = QueueData.AttributesNames.WhoRequestedAttribute.getName();
        final String taskAttr = QueueData.AttributesNames.TaskAttribute.getName();
        final String myPhoneNumber = getMyPhoneNumber(activity);

        final Map<String, MessageAttributeValue> registerUserAttribute = new HashMap<>();
        registerUserAttribute.put(taskAttr, new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.GetNicknames.getName()));
        registerUserAttribute.put(whoReqAttr, QueueData.getWhoRequestedAttributeValue(myPhoneNumber));

        SendMessageRequest registerUserRequest = new SendMessageRequest(queueURL, gson.toJson(new Contact("RegisterdUser",myPhoneNumber)))
                .withMessageAttributes(registerUserAttribute);

        SendMessageResult sendResult = sqs.sendMessage(registerUserRequest);

        Log.i("Request Result:", sendResult.toString());

        return waitForNicknamesResult(myPhoneNumber);
    }

    private boolean getContactsFilter(Message message, String myPhone){
        String whoRequestedAttr = QueueData.AttributesNames.WhoRequestedAttribute.getName();
       // String timeStamp = QueueData.AttributesNames.RequestTimeStamp.getName();
        String requestType = QueueData.AttributesNames.TaskAttribute.getName();

        if(message.getMessageAttributes().get(whoRequestedAttr) == null || message.getMessageAttributes().get(requestType) == null)
            return false;

        String whoRequested = message.getMessageAttributes().get(whoRequestedAttr).getStringValue();

        boolean myRequest = whoRequested.equals(myPhone);
        boolean thisRequest = message.getMessageAttributes().get(requestType).equals(QueueData.Tasks.GetNicknames.getName());

        return myRequest && thisRequest;
    }

    private HashMap<String, Integer> waitForNicknamesResult(String myPhoneNumber) {
        List<Message> messageList = null;

        Contact resultContact = null;
        int waitCounter = 0;

        do{
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            messageList = sqs.receiveMessage(new ReceiveMessageRequest()
                    .withQueueUrl(applicationQueueURL)
                    .withMessageAttributeNames("All"))
                    .getMessages();

            messageList.forEach(message -> {
                Log.i("::THE MESSAGE:" , message.getBody() + "  Attr:" + message.getMessageAttributes());
            });

            Stream<Message> filteredMessages = messageList.stream().filter(message -> getContactsFilter(message, myPhoneNumber));

            Optional<Message> resultMessage = filteredMessages.findFirst();

            Log.i("::IS PRESENT:" , resultMessage.isPresent() +"");

//            Executors.newSingleThreadExecutor().submit(() ->
//                    filteredMessages.forEach(message -> sqs.deleteMessage(applicationQueueURL, message.getReceiptHandle())));

            if(resultMessage.isPresent()){
                Message message = resultMessage.get();

                if(message.getBody() != null && !message.getBody().equals("null") && !message.getBody().isEmpty()) {
                    HashMap<String, Integer> nicknames = gson.fromJson(message.getBody(), HashMap.class);

                    if(nicknames != null)
                        return nicknames;
                    else
                        return new HashMap<>();
                }else if(message.getBody().equals("null")){
                    return new HashMap<>();
                }
            }else{
                if(waitCounter > 30) {
                    return new HashMap<>();
                }
                waitCounter++;
            }

        }while (true);
    }

    public ArrayList<Contact> downloadNicknames(){
        final String whoReqAttr = QueueData.AttributesNames.WhoRequestedAttribute.getName();
        final String taskAttr = QueueData.AttributesNames.TaskAttribute.getName();
        ReceiveMessageRequest request = new ReceiveMessageRequest()
                                            .withQueueUrl(applicationQueueURL)
                                            .withMessageAttributeNames(whoReqAttr, taskAttr);

        ReceiveMessageResult result = sqs.receiveMessage(request);
        List<Message> messages = result.getMessages();

       // messages.stream().filter(message -> message.getMessageAttributes().get(whoReqAttr).equals());

        return new ArrayList<>();
    }

    public void uploadImage(UserImage image){
        final Map<String, MessageAttributeValue> saveImageAttribute = new HashMap<>();
        saveImageAttribute.put(QueueData.AttributesNames.TaskAttribute.getName(), new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(QueueData.Tasks.SaveImageTask.getName()));
        saveImageAttribute.put(QueueData.AttributesNames.WhoRequestedAttribute.getName(), QueueData.getWhoRequestedAttributeValue(image.getPhoneNumber()));

        SendMessageRequest saveImageRequest = new SendMessageRequest(queueURL, gson.toJson(image)).withMessageAttributes(saveImageAttribute);
        sqs.sendMessage(saveImageRequest);
    }

    private static AWSCredentials getCredentials() {
        AWSCredentials credentials = null;
        try {

            credentials = new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return ""; //Return your own AWS Access Key Id
                }

                @Override
                public String getAWSSecretKey() {
                    return ""; //Return your own AWS Secret Key
                }
            };


        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/roie/.aws/credentials), and is in valid format.", e);
        }
        return credentials;
    }

}
