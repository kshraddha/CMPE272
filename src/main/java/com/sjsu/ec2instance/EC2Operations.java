package com.sjsu.ec2instance;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;

/**
 * Creates an EC2 instance
 */
public class EC2Operations {
	private static final AWSCredentials AWS_CREDENTIALS;
	private static final AmazonEC2 ec2Client;

	static {
		// Your accesskey and secretkey
		AWS_CREDENTIALS = new BasicAWSCredentials("AXXXXXXXXXX", "8bKKKKKKKKKKKKKKKKKKKKKKKKKKK");
		ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(AWS_CREDENTIALS))
				.withRegion(Regions.US_WEST_1).build();
	}

	public void createInstance(String name) {

		// Launch an Amazon EC2 Instance
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId("ami-$$$$$")
				.withInstanceType("t2.micro") // https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-types.html
				.withMinCount(1).withMaxCount(1);

		RunInstancesResult runInstancesResult = ec2Client.runInstances(runInstancesRequest);

		Instance instance = runInstancesResult.getReservation().getInstances().get(0);
		String instanceId = instance.getInstanceId();
		System.out.println("EC2 Instance Id: " + instanceId);

		// Setting up the tags for the instance
		CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(instance.getInstanceId())
				.withTags(new Tag("Name", name));
		ec2Client.createTags(createTagsRequest);

		startInstance(instance.getInstanceId());
	}

	public List<Instance> getInstancesUsingTagName(String tagName) {

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		List<String> valuesT1 = new ArrayList<String>();
		valuesT1.add(tagName);
		Filter filter = new Filter("tag:Name", valuesT1);

		DescribeInstancesResult result = ec2Client.describeInstances(request.withFilters(filter));
		List<Reservation> reservations = result.getReservations();
		List<Instance> instances = new ArrayList();
		for (Reservation reservation : reservations) {
			for (Instance instance : reservation.getInstances()) {
				instances.add(instance);
			}
		}
		return instances;
	}

	public void updateInstance(String name) {
		List<Instance> instances = getInstancesUsingTagName(name);
		for (Instance instance : instances) {
			stopInstance(instance.getInstanceId());
			Integer instanceState = -1;
			while(instanceState != 80) { //Loop until the instance is stopped.
			    instanceState = getInstanceStatus(instance.getInstanceId());
			    try {
			        Thread.sleep(10000);
			    } catch(InterruptedException e) {}
			}
			ModifyInstanceAttributeRequest modReq = new ModifyInstanceAttributeRequest()
					.withInstanceType(InstanceType.T2Micro.toString()).withInstanceId(instance.getInstanceId());

			ec2Client.modifyInstanceAttribute(modReq);
			System.out.println(instance.getState().getName());
			
			startInstance(instance.getInstanceId());
			
		}
	}
	
	public Integer getInstanceStatus(String instanceId) {
	    DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
	    DescribeInstancesResult describeInstanceResult = ec2Client.describeInstances(describeInstanceRequest);
	    InstanceState state = describeInstanceResult.getReservations().get(0).getInstances().get(0).getState();
	    return state.getCode();
	}
	
	public void startInstance(String instanceId){
		// Starting the Instance
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(instanceId);
		ec2Client.startInstances(startInstancesRequest);
	}
	
	public void stopInstance(String instanceId){
		// Stop instance
		StopInstancesRequest stp = new StopInstancesRequest().withInstanceIds(instanceId);
	    ec2Client.stopInstances(stp);
	}
}
