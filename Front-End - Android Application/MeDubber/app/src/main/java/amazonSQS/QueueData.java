package amazonSQS;

import com.amazonaws.services.sqs.model.MessageAttributeValue;

public class QueueData {

	public static final String queueName = "MeDubberQueue";
	public static final String applicationQueueName = "MeDubberApplicationQueue";
	
	public enum AttributesNames{
		TaskAttribute("Task"),
		ApplicationTaskAttribute("ApplicationTask"),
		WhoRequestedAttribute("WhoRequested"),
		NicknamesAttribute("Nicknames"),
		RequestTimeStamp("TimeStamp");
		
		private String name;
		
		private AttributesNames(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public enum Tasks{
		NewNickNameTask("NewNicknameTask"),
		SaveImageTask("SaveImage"),
		SaveContactTask("SaveContact"),
		GetContactTask("GetContact"),
		RegisterUser("RegisterUser"),
		GetNicknames("GetNicknames");
		
		private String name;
		
		private Tasks(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public static MessageAttributeValue getWhoRequestedAttributeValue(String whoRequested) {
		return new MessageAttributeValue().withDataType("String").withStringValue(whoRequested);
	}
	
	public static MessageAttributeValue getGetContactAttributeValue() {
		return new MessageAttributeValue().withDataType("String").withStringValue(Tasks.GetContactTask.getName());
	}
	
	public static MessageAttributeValue getNewNicknameAttributeValue() {
		return new MessageAttributeValue().withDataType("String").withStringValue(Tasks.NewNickNameTask.getName());
	}
	
	public static MessageAttributeValue getNicknamesAttributeValue() {
		return new MessageAttributeValue().withStringListValues(Tasks.GetNicknames.getName());
	}
	
	public static MessageAttributeValue getMessageAttributeValue(String input) {
		return new MessageAttributeValue().withDataType("String").withStringValue(input);

	}
	
}
