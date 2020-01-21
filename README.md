########################  MeDubber - Phone Numbers Recognition ######################## 


#### Instructions ###


Back End

** 1: Create DynamoDB Tables in AWS console: "users" and "registerds"

** 2: Create SQS Queues in AWS console: "MeDubberQueue" and "MeDubberApplicationQueue"

** 3: Upload AWS Lambdas using eclipse or AWS console, they are in the BackEnd project
** in: "application/ReceiveQueueLambda.java" and "dal/DynamoDBLambda.java"

** 4: Set ReceiveQueueLambda to be triggered by the "MeDubberQueue" sqs

** 5: Set DynamoDBLambda to be triggered by the "users" table in the dynamoDB

### Notice: you can replace the ReceiveQueueLambda with the EC2 implementation in:
### MeDubber/src/main/java/ec2/EC2ExecuteFromQueue.java which you can export as executable ### jar, export it with the lib folders of dependencies and send it to your EC2 Instance ### using these instructions:

#1. To connect to your EC2 Instance with SSH from local LINUX machine

ssh -i PathToCredentialFile/credentialFile.pem  user@publicDNS

#2. To copy a file from local LINUX machine to your EC2 Instance with SSH 

scp -i PathToCredentialFile/credentialFile.pem  localFile user@publicDNS:

#3. User name on Amazon Linux AMI is ec2-user. Check what is the user name on your image

* Remove old Java version

sudo yum remove java-1.7.0-openjdk

* Install newer version 

sudo yum install java-1.8.0

* Run your application that you prepared on your local machine and have transferred to EC2

Java -jar HelloWorld.jar


Front End

** 1: Insert your credentials in getCredentials() function in "Front-End - Android Application/MeDubber/app/src/main/java/amazonSQS/AmazonSQSClientService.java"

** 2: Run the application using android studio and install it on your phone



$$$ You can also install the MeDubber.apk to use our setup (which is already working) $$$



