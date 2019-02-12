package ec2SavingImageToS3;

import java.util.concurrent.Executors;

public class Main {
 
    public static void main(String[] args) {
        Executors.newSingleThreadExecutor().execute(MeDubberEC2Server.getInstance());
    }
    

}
