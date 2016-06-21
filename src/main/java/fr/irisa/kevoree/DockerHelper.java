package fr.irisa.kevoree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;

public class DockerHelper{

	private static DockerClientConfig defaultConfig = DockerClientConfig.createDefaultConfigBuilder()
			.withDockerHost("unix:///var/run/docker.sock")
			.withDockerTlsVerify(false)
			.withDockerConfig("/home/user/.docker")
			.withApiVersion("1.23")
			.withRegistryUrl("https://index.docker.io/v1/")
			.withRegistryUsername("savak")
			.withRegistryPassword("puissance51")
			.withRegistryEmail("marvin.billaud@gmail.com")
			.build();

	private static DockerClient dockerClient = DockerClientBuilder.getInstance(defaultConfig).build();

	private static List<String> containerList = new ArrayList<String>();

	private static Volume volumeKsContainerPath = new Volume("/root/model.kevs");

	private static void createNetwork(){
		boolean alreadyExist = false;
		List<Network> networks = dockerClient.listNetworksCmd().exec();
		for (Network network : networks) {
			if(network.getName().equals("kevoreeScalability")){
				alreadyExist = true;
			}
		}

		if(!alreadyExist){
			CreateNetworkResponse createNetworkResponse = dockerClient.createNetworkCmd()
					.withIpam(new Network.Ipam()
							.withConfig(new Network.Ipam.Config()
									.withSubnet("10.100.101.0/24")))
					.withName("kevoreeScalability")
					.exec();
		}
	}


	public static void startContainerJsNode(String nodeName, String ksPath, String ip){
		createNetwork();
		CreateContainerResponse container = dockerClient.createContainerCmd("savak/kevoree-js:snapshot")
				.withNetworkMode("kevoreeScalability")
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
		createNetwork();
		CreateContainerResponse container = dockerClient.createContainerCmd("savak/kevoree-java")
				.withNetworkMode("kevoreeScalability")
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
