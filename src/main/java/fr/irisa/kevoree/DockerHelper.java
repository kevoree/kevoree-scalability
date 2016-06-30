package fr.irisa.kevoree;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
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

@SuppressWarnings("serial")
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
	private static Volume volumeKsContainerPath = new Volume("/root/model.kevs");

	/**
	 * This Map represent the necessary informations of the machine of the cluster
	 * Pattern : clusterLogin Item = [ IP , [ UserName , UserPassword ] ]
	 */
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

	/**
	 * Create a network for the container communication
	 */
	public static void createNetwork(){
		dockerClient.createNetworkCmd()
		.withIpam(new Network.Ipam()
				.withConfig(new Network.Ipam.Config()
						.withSubnet("100.100.0.0/16")))
		.withName("KevoreeScalabilityNetwork")
		.exec();
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
		CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/js")
				.withNetworkMode("KevoreeScalabilityNetwork")
				.withIpv4Address(ip)
				.withName(nodeName+"Container")
				.withVolumes(volumeKsContainerPath)
				.withBinds(new Bind("/kevoree/"+ksPath.split("/")[ksPath.split("/").length-1], volumeKsContainerPath))
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
		CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/java")
				.withNetworkMode("KevoreeScalabilityNetwork")
				.withIpv4Address(ip)
				.withName(nodeName+"Container")
				.withVolumes(volumeKsContainerPath)
				.withBinds(new Bind("/kevoree/"+ksPath.split("/")[ksPath.split("/").length-1], volumeKsContainerPath))
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
