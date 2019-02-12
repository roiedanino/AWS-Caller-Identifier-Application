package entities;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="users")
public class User {
	public static final String TABLE_NAME = "users";
	
	private String phoneNumber;
	private String mostCommonNickname;
	private Map<String, Integer> nicknames = new HashMap<>();

	public User() {
		
	}
	
	public User(Contact contact) {
		phoneNumber = contact.getPhoneNumber();
		mostCommonNickname = contact.getName();
		nicknames.put(contact.getName(), 1);
	}
	
	@DynamoDBHashKey
	public String getPhoneNumber() {	
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@DynamoDBAttribute
	public String getMostCommonNickname() {
		return mostCommonNickname;
	}

	public void setMostCommonNickname(String mostCommonNickname) {
		this.mostCommonNickname = mostCommonNickname;
	}

	@DynamoDBAttribute
	public Map<String, Integer> getNicknames() {
		return nicknames;
	}

	public void setNicknames(Map<String, Integer> nicknames){
		this.nicknames = nicknames;
	}
	
	public void addNickname(String name) {
		if(!nicknames.containsKey(name)) {
			nicknames.put(name, 1);
		}else {
			int counter = nicknames.get(name).intValue() + 1;
			nicknames.put(name, counter);
		}
	}
	
	public Integer getNicknameValue(String name){
		return nicknames.get(name);
	}
	
	@Override
	public String toString() {
		return "User [phoneNumber=" + phoneNumber + ", mostCommonNickname=" + mostCommonNickname + ", nicknames="
				+ nicknames + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		return true;
	}
	
	
}
