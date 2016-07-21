package fr.irisa.kevoree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;

public class DockerHelper{

	/**
	 * Initialize docker client
	 */
	private static final DockerClient dockerClient = DockerClientBuilder.getInstance(Config.DOCKER_DAEMON_HOST).build();

	/**
	 * String list of container name used for remove all the container started with startContainerJavaNode() and startContainerJsNode()
	 * 
	 * @see
	 * 		startContainerJavaNode()
	 * 		startContainerJsNode()
	 */
	private static final List<String> containerList = new ArrayList<String>();

	/**
	 * Volume to bind within the container
	 * 
	 * @param
	 * 		Path of the KevScript within the container
	 */
	private static final Volume volumeKsContainerPath = new Volume("/root/model.json");



	/**
	 * Create a network for the container communication
	 */
	public static void createNetwork(){
        Network.Ipam ipam = new Network.Ipam()
        		.withConfig(new Network.Ipam.Config().withSubnet(Config.DOCKER_NETWORK_SUBNET));
        
        dockerClient.createNetworkCmd().withName(Config.DOCKER_NETWORK_NAME).withIpam(ipam).exec();
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
					.withNetworkMode(Config.DOCKER_NETWORK_NAME)
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withEnv("KEVOREE_REGISTRY_HOST="+Config.KEVOREE_REGISTRY_HOST, "KEVOREE_REGISTRY_PORT="+Config.KEVOREE_REGISTRY_PORT)
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
					.withNetworkMode(Config.DOCKER_NETWORK_NAME)
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withEnv("KEVOREE_REGISTRY_HOST="+Config.KEVOREE_REGISTRY_HOST, "KEVOREE_REGISTRY_PORT="+Config.KEVOREE_REGISTRY_PORT)
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
					.withNetworkMode(Config.DOCKER_NETWORK_NAME)
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withBinds(new Bind("/kevoree/model.json", volumeKsContainerPath))
					.withExposedPorts(tcp9000)
					.withPortBindings(portBindings)
					.withCmd("-Dkevoree.registry=http://"+Config.KEVOREE_REGISTRY_HOST+":"+Config.KEVOREE_REGISTRY_PORT+" -Dnode.name=" + nodeName + " -Dnode.bootstrap="+volumeKsContainerPath.getPath())
					.exec();

			containerList.add(container.getId());
			dockerClient.startContainerCmd(container.getId())
			.exec();
		}else{
			//volumeKsContainerPath = new Volume("/kevoree/"+ksPath.split("/")[ksPath.split("/").length-1]);
			CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/java")
					.withNetworkMode(Config.DOCKER_NETWORK_NAME)
					.withIpv4Address(ip)
					.withName(nodeName+"Container")
					.withVolumes(volumeKsContainerPath)
					.withBinds(new Bind("/kevoree/model.json", volumeKsContainerPath))
					.withCmd("-Dkevoree.registry=http://"+Config.KEVOREE_REGISTRY_HOST+":"+Config.KEVOREE_REGISTRY_PORT+" -Dnode.name=" + nodeName + " -Dnode.bootstrap="+volumeKsContainerPath.getPath())
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
			ExecutorService executor = Executors.newFixedThreadPool(KevoreeHelper.nodesNumber);
			Collection<Callable<Void>> taskslist = new ArrayList<Callable<Void>>();
			for (String containerId : containerList) {
				Callable<Void> taskRemoveContainer = () -> {
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
	 * Remove the network created by createNetwork()
	 */
	public static void removeNetwork(){
		try {
			dockerClient.removeNetworkCmd(Config.DOCKER_NETWORK_NAME)
			.exec();
		} catch (NotFoundException e) {
			System.out.println("Neither network to remove");
		}

	}
}
