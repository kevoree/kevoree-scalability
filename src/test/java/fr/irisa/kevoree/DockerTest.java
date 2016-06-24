package fr.irisa.kevoree;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.core.DockerClientBuilder;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class DockerTest {

	@Test
	public void test() throws Exception {
		final DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://10.0.0.1:4000").build();

		ListContainersCmd res = dockerClient.listContainersCmd();
		System.out.println(res);
	}

	@Test
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

}
