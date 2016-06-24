package fr.irisa.kevoree;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class DockerHelper{

	/**
	 * Initialize docker client
	 */
	private static DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://10.0.0.1:4000").build();
	
	/**
	 * String list of container name for remove all the container started with startContainerJavaNode() and startContainerJsNode()
	 */
	private static List<String> containerList = new ArrayList<String>();

	/**
	 * Volume to bind within the container
	 * 
	 * @param
	 * 		Path of the KevScript within the container
	 */
	private static Volume volumeKsContainerPath;
	
	/**
	 * Network name created according to the IPs specified in the Kevscript.
	 * You MUST provide a way to connect to this node from outside like that in the KevScript :
	 * network jsNode.ip.lo 10.100.101.2
	 */
	private static String networkName = "";
	
	public static final Map<String, List<String>> clusterLogin;
	static
    {
		clusterLogin = new HashMap<String, List<String>>();
		clusterLogin.put("10.0.0.1", new ArrayList<String>() {{
		    add("oem");
		    add("ubuntu");
		}});
		clusterLogin.put("10.0.0.3", new ArrayList<String>() {{
		    add("ubuntu");
		    add("ubuntu");
		}});
		clusterLogin.put("10.0.0.4", new ArrayList<String>() {{
		    add("oem");
		    add("ubuntu");
		}});
		clusterLogin.put("10.0.0.5", new ArrayList<String>() {{
		    add("ubuntu");
		    add("ubuntu");
		}});
		clusterLogin.put("10.0.0.6", new ArrayList<String>() {{
		    add("ubuntu");
		    add("ubuntu");
		}});
		clusterLogin.put("10.0.0.7", new ArrayList<String>() {{
		    add("ubuntu");
		    add("ubuntu");
		}});
		
		
    }

	private static synchronized void createNetwork(String ip){
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

	/**
	 * Start a JavaScript node within a Docker container
	 * 
	 * @param nodeName
	 * 		The name of your node specified in the KevScript
	 * @param ksPath
	 * 		The KevScript path
	 * @param ip
	 * 		The IP address of the node
	 */
	public static void startContainerJsNode(String nodeName, String ksPath, String ip){
		volumeKsContainerPath = new Volume("/kevoree/"+ksPath.split("/")[ksPath.split("/").length-1]);
		createNetwork(ip);
		CreateContainerResponse container = dockerClient.createContainerCmd("savak/kevoree-js")
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
	public static void startContainerJavaNode(String nodeName, String ksPath, String ip){
		volumeKsContainerPath = new Volume("/kevoree/"+ksPath.split("/")[ksPath.split("/").length-1]);
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
	
	public static void copyKevsciptToAllClusterNode(String host, String user, String password, String kevscriptPath){
		String sftpHost = host;
		//int SFTPPORT = 4000;
		String sftpUser = user;
		String sftpPass = password;
		String sftpWorkingDir = "/kevoree";

		Session     session     = null;
		Channel     channel     = null;
		ChannelSftp channelSftp = null;

		try{
			JSch jsch = new JSch();
			session = jsch.getSession(sftpUser,sftpHost);
			session.setPassword(sftpPass);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp)channel;
			channelSftp.cd(sftpWorkingDir);
			File f = new File(kevscriptPath);
			channelSftp.put(new FileInputStream(f), f.getName());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * Remove all the container started with startContainerJavaNode() and startContainerJsNode()
	 */
	public static void removeAllContainer(){
		for (String containerId : containerList) {
			dockerClient.removeContainerCmd(containerId)
			.withForce(true)
			.exec();
		}
	}
	
	/**
	 * Remove the network created by createNetwork(String ip)
	 */
	public static void removeNetwork(){
		dockerClient.removeNetworkCmd(networkName)
		.exec();
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
