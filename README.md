# kevoree-scalability

The goal of this project is to evaluate the scalability of Kevoree for deploying applications to large sizes. 

To do this we study on docker platforms the propagation time configuration scripts and the dynamic reconfiguration time.


## TODO

  * Update Kevoree platform to 5.3.2
  * Deployment optimization of Docker containers
  * Integration of kevoree-model-generator
  * Kevscript push optimization (work with WebSocket atm)
  * Improved GUI


## Usage and limitations

This is a first version of the tool.

For now, it allow users to take a kevscript as input, analyse it and create one docker container per nodes. Those containers can communicate with each other.

The user can also upload an updated KevScript and push it to the running container (work in progress for this point).


## Helpers

This project contains two static class for help the development with Kevoree and Docker-java API

### KevoreeHelper

public class Application {

    	public static void main(String[] args) {
    
		// Get the KevScript from his file path 
		String kevscript = KevoreeHelper.getKevscriptFromPath(kevscriptPath);
		
		// Create the model according to the kevscript in param
		KevoreeHelper.createModelFromKevScript(kevscript);
		
		// Get the name of the master node
		String masterNodeName = KevoreeHelper.getMasterNodeName();
		
		// Get a Map with nodes name as keys and type definition as value
		Map<String,TypeDefinition> nodesNameAndTypeDef = KevoreeHelper.getNodesNameAndTypeDefFromKevScript();
		
		// Get a Map with nodes name as keys and IP address as value
		Map<String,String> nodesNameAndIp = KevoreeHelper.getNodesNameAndIpAddressFromKevScript();
	}
}
	
### DockerHelper

public class Application {
	
	public static void main(String[] args) {
	
    		// Create the overlay network according to a particular IP
    		DockerHelper.createNetwork(ip);
    		
    		// Start a container from image kevoree/js with a specific JS node
    		DockerHelper.startContainerJsNode(nodeName, kevscriptPath, nodeIP);
    		
    		// Start a container from image kevoree/java with a specific Java node
    		DockerHelper.startContainerJavaNode(nodeName, kevscriptPath, nodeIP);
    		
    		// Remove the overlay network previously created
    		DockerHelper.removeNetwork();
    	}
}
