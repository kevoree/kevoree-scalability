package fr.irisa.kevoree;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DockerClientBuilder;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class DockerTest {

	@Test
	@Ignore
	public void test2(){
		String SFTPHOST = "10.0.0.1";
		//int SFTPPORT = 4000;
		String SFTPUSER = "oem";
		String SFTPPASS = "ubuntu";
		String SFTPWORKINGDIR = "/kevoree";

		Session     session     = null;
		Channel     channel     = null;
		ChannelSftp channelSftp = null;

		try{
			JSch jsch = new JSch();
			session = jsch.getSession(SFTPUSER,SFTPHOST);
			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp)channel;
			channelSftp.cd(SFTPWORKINGDIR);
			File f = new File("/home/Savak/Dev/Models/bigModel.kevs");
			channelSftp.put(new FileInputStream(f), f.getName());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	@Test
	@Ignore
	public void createContainerWithCustomIp() throws DockerException {
		DockerClient dockerClient = DockerClientBuilder.getInstance().build();

		CreateNetworkResponse createNetworkResponse = dockerClient.createNetworkCmd()
				.withIpam(new Network.Ipam()
						.withConfig(new Network.Ipam.Config()
								.withSubnet("100.100.101.0/24")))
				
				.withName("customIpNet")
				.exec();

		assertNotNull(createNetworkResponse.getId());

		CreateContainerResponse container = dockerClient.createContainerCmd("kevoree/js")
				.withNetworkMode("customIpNet")
				.withCmd("sleep", "9999")
				.withName("container")
				.withIpv4Address("100.100.101.100")
				.exec();

		dockerClient.startContainerCmd(container.getId()).exec();

		InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId())
				.exec();

		ContainerNetwork customIpNet = inspectContainerResponse.getNetworkSettings().getNetworks().get("customIpNet");
		assertNotNull(customIpNet);
		System.out.println(customIpNet.getGateway());
		System.out.println(customIpNet.getIpAddress());
		assertEquals(customIpNet.getGateway(), "100.100.101.1");
		assertEquals(customIpNet.getIpAddress(), "100.100.101.100");
	}

}
