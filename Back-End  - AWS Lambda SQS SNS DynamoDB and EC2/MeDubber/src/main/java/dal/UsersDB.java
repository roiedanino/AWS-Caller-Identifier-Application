package dal;
import java.util.List;

import entities.RegisterdUser;
import entities.User;

public interface UsersDB {
	
	void saveUser(User user);
	
	void saveUsers(List<User> users);
	
	User getUserByPhone(String phone);

	int addNickname(User user, String nickname);
	
	void updateMostCommonNickname(User user, String nickname);
	
	String getMostCommonNickname(User user);
	
	void saveRegisterdUser(RegisterdUser regUser);
	
	RegisterdUser loadRegisterdUser(String phoneNumber);
}
