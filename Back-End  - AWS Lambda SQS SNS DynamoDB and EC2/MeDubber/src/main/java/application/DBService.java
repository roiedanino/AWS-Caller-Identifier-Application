package application;

import entities.Contact;
import entities.RegisterdUser;
import entities.User;

public interface DBService {
	void saveContact(Contact contact);
	
	Contact getContact(String phoneNumber);
	
	void saveRegisterdUser(RegisterdUser registerdUser);
	
	boolean isUserRegisterd(String phoneNumber);
	
	RegisterdUser getRegisterdUser(String phoneNumber);
	
	User getUser(String phone);
}
