package entities;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="registerds")
public class RegisterdUser implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4239986285261211603L;
	
	private String phoneNumber;
	private String topicArn;
	
	public RegisterdUser() {
		
	}
	
	public RegisterdUser(String phoneNumber, String topicArn) {
		setPhoneNumber(phoneNumber);
		setTopicArn(topicArn);
	}
	
	@DynamoDBHashKey
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@DynamoDBAttribute
	public String getTopicArn() {
		return topicArn;
	}

	public void setTopicArn(String topicArn) {
		this.topicArn = topicArn;
	}

	@Override
	public String toString() {
		return "RegisterdUser {phoneNumber=" + phoneNumber + ", topicArn=" + topicArn + "}";
	}
	
	
}
