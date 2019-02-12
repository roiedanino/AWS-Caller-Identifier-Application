package application;

import java.io.File;
import java.io.InputStream;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3Controller {
	
	private static final String bucketName = "medubberimages-roie";
	private AmazonS3 s3;

	public S3Controller(ProfileCredentialsProvider credentialsProvider) {
        s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("us-west-2")
            .build();
	}
	
	public S3Controller() {
        s3 = AmazonS3ClientBuilder.standard()
            .withRegion("us-west-2")
            .build();
	}
	
	public void saveImage(String phoneNumber, File image) {
		PutObjectRequest request = new PutObjectRequest(bucketName, phoneNumber, image);
		
		s3.putObject(request);
	}
	
	public InputStream getImageInputStream(String phoneNumber) {
		GetObjectRequest request = new GetObjectRequest(bucketName, phoneNumber);
		try {

			S3Object result = s3.getObject(request);
			if(result != null)
				return result.getObjectContent();
			else 
				throw new NullPointerException("No such image");
		}catch(Exception ex) {
			throw new NullPointerException("No such image");
		}
	}
}
