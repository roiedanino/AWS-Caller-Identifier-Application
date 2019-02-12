package dal;

import java.util.List;
import java.util.NoSuchElementException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

import entities.RegisterdUser;
import entities.User;

public class UsersDynamoDB implements UsersDB {

	private static UsersDynamoDB instance = null;

	private static AmazonDynamoDB amazonDynamoDB;

	private DynamoDBMapper usersMapper;

	private UsersDynamoDB() {

		amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_WEST_2).build();

		usersMapper = new DynamoDBMapper(amazonDynamoDB);
	}

	public static UsersDynamoDB getInstance() {
		if (instance == null)
			instance = new UsersDynamoDB();

		return instance;
	}

	@Override
	public void saveUser(User user) {
		usersMapper.save(user);
	}

	@Override
	public void saveUsers(List<User> users) {
		usersMapper.batchSave(users);
	}

	@Override
	public User getUserByPhone(String phone) {
		return usersMapper.load(User.class, phone);
	}

	@Override
	public int addNickname(User user, String nickname) {
		User actualUser = usersMapper.load(User.class, user.getPhoneNumber());
		actualUser.addNickname(nickname);
		usersMapper.save(actualUser);

		return actualUser.getNicknameValue(nickname);
	}

	@Override
	public void updateMostCommonNickname(User user, String nickname) {
		UpdateItemRequest updateItemRequest = new UpdateItemRequest().withTableName(User.TABLE_NAME)
				.addKeyEntry("phoneNumber", new AttributeValue().withS(user.getPhoneNumber()))
				.addAttributeUpdatesEntry("mostCommonNickname",
						new AttributeValueUpdate().withValue(new AttributeValue().withS(nickname)));

		amazonDynamoDB.updateItem(updateItemRequest);

	}

	@Override
	public String getMostCommonNickname(User user) {
		User loadedUser = usersMapper.load(User.class, user.getPhoneNumber());
		if (loadedUser != null)
			return loadedUser.getMostCommonNickname();

		throw new NoSuchElementException("User " + user.getPhoneNumber() + " was not found in the database");
	}

	@Override
	public void saveRegisterdUser(RegisterdUser regUser) {
		usersMapper.save(regUser);
	}

	@Override
	public RegisterdUser loadRegisterdUser(String phoneNumber) {
		return usersMapper.load(RegisterdUser.class, phoneNumber);
	}

}
