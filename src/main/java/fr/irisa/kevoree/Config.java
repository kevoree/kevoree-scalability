package fr.irisa.kevoree;

public class Config {
	public final static String KEVOREE_REGISTRY_HOST = "10.0.0.7";
	public final static String KEVOREE_REGISTRY_PORT = "32768";
	
	public final static String MASTER_NODE_PORT = "9000";
	public final static String MASTER_NODE_IP = "100.100.0.2";
	
	public final static String DOCKER_DAEMON_HOST = "tcp://10.0.0.1:4000";
	public final static String DOCKER_NETWORK_NAME = "KevoreeScalabilityNetwork";
	public final static String DOCKER_NETWORK_SUBNET = "100.100.0.0/16";
}
