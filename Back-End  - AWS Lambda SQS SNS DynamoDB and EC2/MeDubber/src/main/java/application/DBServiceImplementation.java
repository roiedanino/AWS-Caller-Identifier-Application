package application;

import dal.UsersDB;
import dal.UsersDynamoDB;
import entities.Contact;
import entities.RegisterdUser;
import entities.User;

public class DBServiceImplementation implements DBService{

	private UsersDB usersDB;
	
	public DBServiceImplementation() {
		usersDB = UsersDynamoDB.getInstance();
	}

	@Override
	public void saveContact(Contact contact) {
		User user = usersDB.getUserByPhone(contact.getPhoneNumber());
		if(user == null) {
			user = new User(contact);
			usersDB.saveUser(user);
		}else {
			int nickNameCounter = usersDB.addNickname(user,contact.getName());
			updateMostCommonNickname(contact, user, nickNameCounter);
		}
	}
	
	@Override
	public Contact getContact(String phoneNumber) {
		Contact contact = null;
		User user = usersDB.getUserByPhone(phoneNumber);
		
		if(user != null) {
			contact = new Contact(user.getMostCommonNickname(), user.getPhoneNumber());
		}
		return contact;
	}

	private void updateMostCommonNickname(Contact contact, User user, int nickNameCounter) {
		if(user.getNicknames().containsKey(contact.getName()) && nickNameCounter > user.getNicknameValue(contact.getName())) {
			usersDB.updateMostCommonNickname(user, contact.getName());
		}
	}

	public boolean isUserRegisterd(String phoneNumber) {
		return usersDB.loadRegisterdUser(phoneNumber) != null;
	}

	@Override
	public void saveRegisterdUser(RegisterdUser registerdUser) {
		usersDB.saveRegisterdUser(registerdUser);
	}

	@Override
	public RegisterdUser getRegisterdUser(String phoneNumber) {
		return usersDB.loadRegisterdUser(phoneNumber);
	}
	
	@Override
	public User getUser(String phone) {
		return usersDB.getUserByPhone(phone);
	}

	
}
