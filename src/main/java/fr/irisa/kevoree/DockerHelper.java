package fr.irisa.kevoree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;

public class DockerHelper{

	private static DockerClient dockerClient = DockerClientBuilder.getInstance().build();

	private static List<String> containerList = new ArrayList<String>();

	private static Volume volumeKsContainerPath = new Volume("/root/model.kevs");
	
	private static String networkName = "";

	private static synchronized  void createNetwork(String ip){
		String[] splittedArray = ip.split("\\.");
		String firstThreeSegments = splittedArray [0] + "." + splittedArray [1] + "." + splittedArray [2] + ".";
		networkName = firstThreeSegments+"kevoreeScalability";

		boolean alreadyExist = false;
		List<Network> networks = dockerClient.listNetworksCmd().exec();
		for (Network network : networks) {
			if(network.getName().equals(networkName)){
				alreadyExist = true;
			}
		}

		if(!alreadyExist){
			dockerClient.createNetworkCmd()
			.withIpam(new Network.Ipam()
					.withConfig(new Network.Ipam.Config()
							.withSubnet(firstThreeSegments+"0/24")))
			.withName(firstThreeSegments+"kevoreeScalability")
			.exec();
		}
	}


	public static void startContainerJsNode(String nodeName, String ksPath, String ip){
		createNetwork(ip);
		CreateContainerResponse container = dockerClient.createContainerCmd("savak/kevoree-js:snapshot")
				.withNetworkMode(networkName)
				.withIpv4Address(ip)
				.withName(nodeName+"Container")
				.withVolumes(volumeKsContainerPath)
				.withBinds(new Bind(ksPath, volumeKsContainerPath))
				.withCmd("-n", nodeName, "--kevscript="+volumeKsContainerPath)
				.exec();

		containerList.add(container.getId());
		dockerClient.startContainerCmd(container.getId())
		.exec();
	}

	public static void startContainerJavaNode(String nodeName, String ksPath, String ip){
		createNetwork(ip);
		CreateContainerResponse container = dockerClient.createContainerCmd("savak/kevoree-java")
				.withNetworkMode(networkName)
				.withIpv4Address(ip)
				.withName(nodeName+"Container")
				.withVolumes(volumeKsContainerPath)
				.withBinds(new Bind(ksPath, volumeKsContainerPath))
				.withCmd("-Dnode.name="+nodeName, "-Dnode.bootstrap="+volumeKsContainerPath)
				.exec();

		containerList.add(container.getId());
		dockerClient.startContainerCmd(container.getId())
		.exec();
	}

	public static void removeAllContainer(){
		for (String containerId : containerList) {
			dockerClient.removeContainerCmd(containerId)
			.withForce(true)
			.exec();
		}
	}
	
	public static void removeNetwork(){
		dockerClient.removeNetworkCmd(networkName)
		.exec();
	}

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
