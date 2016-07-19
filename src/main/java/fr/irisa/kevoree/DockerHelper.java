package fr.irisa.kevoree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

public class DockerHelper{

	/**
	 * Initialize docker client
	 */
	private static DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://10.0.0.1:4000").build();

	/**
	 * String list of container name used for remove all the container started with startContainerJavaNode() and startContainerJsNode()
	 * 
	 * @see
	 * 		startContainerJavaNode()
	 * 		startContainerJsNode()
	 */
	private static List<String> containerList = new ArrayList<String>();
	private static int containerListSize = 0;

	/**
	 * Volume to bind within the container
	 * 
	 * @param
	 * 		Path of the KevScript within the container
	 */
	private static Volume volumeKsContainerPath = new Volume("/root/model.json");



	/**
	 * Create a network for the container communication
	 */
	public static void createNetwork(){
		String networkName = "KevoreeScalabilityNetwork";
        Network.Ipam ipam = new Network.Ipam()
        		.withConfig(new Network.Ipam.Config().withSubnet("100.100.0.0/16"));
        
        dockerClient.createNetworkCmd().withName(networkName).withIpam(ipam).exec();
	}

	/**
	 * Start a JavaScript node within a Docker container
	 * 
	 * @param nodeName
	 * 		The name of your node specified in the KevScript
	 * @param ksPath
	 * 		The KevScript pathd
	 * @param ip
	 * 		The IP address of the node
	 */
	public static void startContainerJsNode(String nodeName, String ip){
		if(nodeName.equals(KevoreeHelper.getMasterNodeName())){

			ExposedPort tcp9000 = ExposedPort.tcp(9000);
			Ports portBindings = new Ports();
			portBindings.bind(tcp9000, Binding.bindPort(9000));
			
			CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/js")
					.withNetworkMode("KevoreeScalabilityNetwork")
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withEnv("KEVOREE_REGISTRY_HOST=10.0.0.7", "KEVOREE_REGISTRY_PORT=32768")
					.withBinds(new Bind("/kevoree/model.json", volumeKsContainerPath))
					.withExposedPorts(tcp9000)
					.withPortBindings(portBindings)
					.withCmd("--model=" + volumeKsContainerPath.getPath(), "--nodeName="+ nodeName)
					.exec();

			containerList.add(container.getId());
			dockerClient.startContainerCmd(container.getId())
			.exec();
		}else{
			CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/js")
					.withNetworkMode("KevoreeScalabilityNetwork")
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withEnv("KEVOREE_REGISTRY_HOST=10.0.0.7", "KEVOREE_REGISTRY_PORT=32768")
					.withBinds(new Bind("/kevoree/model.json", volumeKsContainerPath))
					.withCmd("--model=" + volumeKsContainerPath.getPath(), "--nodeName="+ nodeName)
					.exec();

			containerList.add(container.getId());
			dockerClient.startContainerCmd(container.getId())
			.exec();
		}
	}

	/**
	 * Start a Java node within a Docker container
	 * 
	 * @param nodeName
	 * 		The name of your node specified in the KevScript
	 * @param ksPath
	 * 		The KevScript path
	 * @param ip
	 * 		The IP address of the node
	 */
	public static void startContainerJavaNode(String nodeName, String ip){
		if(nodeName.equals(KevoreeHelper.getMasterNodeName())){
			
			ExposedPort tcp9000 = ExposedPort.tcp(9000);
			Ports portBindings = new Ports();
			portBindings.bind(tcp9000, Binding.bindPort(9000));
			
			//volumeKsContainerPath = new Volume("/kevoree/"+ksPath.split("/")[ksPath.split("/").length-1]);
			CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/java")
					.withNetworkMode("KevoreeScalabilityNetwork")
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withBinds(new Bind("/kevoree/model.json", volumeKsContainerPath))
					.withExposedPorts(tcp9000)
					.withPortBindings(portBindings)
					.withCmd("-Dkevoree.registry=http://10.0.0.7:32768 -Dnode.name=" + nodeName + " -Dnode.bootstrap="+volumeKsContainerPath.getPath())
					.exec();

			containerList.add(container.getId());
			dockerClient.startContainerCmd(container.getId())
			.exec();
		}else{
			//volumeKsContainerPath = new Volume("/kevoree/"+ksPath.split("/")[ksPath.split("/").length-1]);
			CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/java")
					.withNetworkMode("KevoreeScalabilityNetwork")
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withBinds(new Bind("/kevoree/model.json", volumeKsContainerPath))
					.withCmd("-Dkevoree.registry=http://10.0.0.7:32768 -Dnode.name=" + nodeName + " -Dnode.bootstrap="+volumeKsContainerPath.getPath())
					.exec();

			containerList.add(container.getId());
			dockerClient.startContainerCmd(container.getId())
			.exec();
		}
		
	}



	/**
	 * Remove all the container started with startContainerJavaNode() and startContainerJsNode()
	 */
	public static void removeAllContainer(){
		try {
			containerListSize=containerList.size();
			ExecutorService executor = Executors.newFixedThreadPool(containerListSize);
			Collection<Callable<Void>> taskslist = new ArrayList<Callable<Void>>();
			for (String containerId : containerList) {
				Callable<Void> taskRemoveContainer = () -> {
					containerListSize = containerListSize-1;					
					return dockerClient.removeContainerCmd(containerId)
							.withForce(true)
							.exec();

				};
				taskslist.add(taskRemoveContainer);
			}
			List<Future<Void>> futures = executor.invokeAll(taskslist);

			boolean allContainerRemoved = false;
			while (!allContainerRemoved) {
				for(Future<Void> future : futures){
					if (future.isDone()){
						allContainerRemoved=true;
					}else{
						allContainerRemoved=false;
						break;
					}
				}
			}

		} catch (IllegalArgumentException e) {
			System.out.println("Neither container to remove");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}

	/**
	 * Remove the network created by createNetwork(String ip)
	 */
	public static void removeNetwork(){
		try {
			dockerClient.removeNetworkCmd("KevoreeScalabilityNetwork")
			.exec();
		} catch (NotFoundException e) {
			System.out.println("Neither network to remove");
		}

	}

	/**
	 * Get the logs of a container
	 * 
	 * @param containerId
	 * @return 
	 * 		The logs of the container specified in the parameter
	 * @throws Exception
	 * 
	 * FIX ME : return logs while container is running
	 */
	public static String getLogs(String containerId) throws Exception {
		LogContainerTestCallback  loggingCallback = new LogContainerTestCallback(true); // borrowed from AbstractDockerClientTest
		dockerClient.logContainerCmd(containerId)
		.withStdErr(true)
		.withStdOut(true)
		.withFollowStream(true)
		.withTailAll()
		.exec(loggingCallback);

		loggingCallback.awaitCompletion(10, TimeUnit.SECONDS);
		return loggingCallback.toString();
	}

	public static class LogContainerTestCallback extends LogContainerResultCallback {
		protected final StringBuffer log = new StringBuffer();

		List<Frame> collectedFrames = new ArrayList<Frame>();

		boolean collectFrames = false;

		public LogContainerTestCallback() {
			this(false);
		}

		public LogContainerTestCallback(boolean collectFrames) {
			this.collectFrames = collectFrames;
		}

		@Override
		public void onNext(Frame frame) {
			if(collectFrames) collectedFrames.add(frame);
			log.append(new String(frame.getPayload()));
		}

		@Override
		public String toString() {
			return log.toString();
		}


		public List<Frame> getCollectedFrames() {
			return collectedFrames;
		}
	}
}
