package fr.irisa.kevoree;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@SuppressWarnings("serial")
public class ClusterHelper {
	
	/**
	 * This Map represent the necessary informations of the machine of the cluster
	 * Pattern : clusterLogin Item = [ IP , [ UserName , UserPassword ] ]
	 */
	public static final Map<String, List<String>> clusterLogins;
	static
	{
		clusterLogins = new HashMap<String, List<String>>();

		clusterLogins.put("10.0.0.1", new ArrayList<String>() {{
			add("oem");
			add("ubuntu");
		}});
		clusterLogins.put("10.0.0.3", new ArrayList<String>() {{
			add("ubuntu");
			add("ubuntu");
		}});
		clusterLogins.put("10.0.0.4", new ArrayList<String>() {{
			add("oem");
			add("ubuntu");
		}});
		clusterLogins.put("10.0.0.5", new ArrayList<String>() {{
			add("ubuntu");
			add("ubuntu");
		}});
		clusterLogins.put("10.0.0.6", new ArrayList<String>() {{
			add("ubuntu");
			add("ubuntu");
		}});
		clusterLogins.put("10.0.0.7", new ArrayList<String>() {{
			add("ubuntu");
			add("ubuntu");
		}});
	}
	
	/**
	 * Copy the JSON model on all cluster machine
	 * 
	 * @param host
	 * 		The host of the machine
	 * @param user
	 * 		The machine user
	 * @param password
	 * 		His password
	 * @param jsonModel
	 * 		The path of the model to copy
	 */
	public static void copyJsonModelToAllClusterNode(String host, String user, String password, String jsonModel){
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
			File f = new File(jsonModel);
			channelSftp.put(new FileInputStream(f), "model.json");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
