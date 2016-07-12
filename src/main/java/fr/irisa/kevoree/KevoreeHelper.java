package fr.irisa.kevoree;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.NetworkInfo;
import org.kevoree.TypeDefinition;
import org.kevoree.Value;
import org.kevoree.api.KevScriptService;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.pmodeling.api.ModelCloner;
import org.kevoree.pmodeling.api.compare.ModelCompare;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;

import fr.braindead.websocket.client.WebSocketClient;

/**
 * KevoreeHelper class contains some methods for KevScript processing
 * 
 * @author Savak
 * @version 1.0
 */

public class KevoreeHelper {
	
	private static final KevoreeFactory factory = new DefaultKevoreeFactory();

	private static final ModelCloner cloner = factory.createModelCloner();

	private static final KevScriptService kevScriptService = new KevScriptEngine();

	public static ContainerRoot currentModel = createEmptyContainerRoot();

	/**
	 * Get the name of the master node
	 * 
	 * @return
	 * 		The name of the master node
	 */
	public static String getMasterNodeName(){
		String masterNodeName = "";

		List<Group> groups = currentModel.getGroups();

		for (Group group : groups) {
			List<Value> groupDictionary = group.getDictionary().getValues();
			for (Value value : groupDictionary) {
				if(value.getName().equals("master")){
					masterNodeName = value.getValue();
				}
			}
		}
		return masterNodeName;
	}
	
	/**
	 * Get the port of the master node. If it is not specified in the KevScript, it take the default value (9000)
	 * 
	 * @return
	 * 		The port of the master node
	 */
	public static String getMasterNodePort(){
		String masterNodePort = null;
		List<Group> groups = currentModel.getGroups();
		for (Group group : groups) {
			try {
				masterNodePort = group.findFragmentDictionaryByID(getMasterNodeName()).findValuesByID("port").getValue();
			} catch (NullPointerException e) {
				masterNodePort = "9000";
			}
		}
		return masterNodePort;
	}
	
	/**
	 * Return a HashMap<String,TypeDefinition> with the names and the TypeDefinition of the nodes within the KevScript
	 * 
	 * @return 
	 * 		HashMap<String,TypeDefinition> with the names and the TypeDefinition of the nodes within the KevScript
	 * 			Keys : Node names
	 * 			Values : TypeDefinition
	 */
	public static Map<String,TypeDefinition> getNodesNameAndTypeDefFromKevScript() {
		List<ContainerNode> nodes = currentModel.getNodes();
		Map<String,TypeDefinition> nodesNameAndTypeDef = new HashMap<String,TypeDefinition>();
		for (ContainerNode node : nodes) {
			nodesNameAndTypeDef.put(node.getName(), node.getTypeDefinition());
		}
		return nodesNameAndTypeDef;
	}

	/**
	 * Return a HashMap<String,String> with the names and the IP address of the nodes according to the master node IP
	 * 
	 * @return 
	 * 		HashMap<String,String> with the names and the IP address of the nodes
	 * 			Keys : Node names
	 * 			Values : IP address
	 */
	public static Map<String,String> getNodesNameAndIpAddressFromKevScript(){
		List<ContainerNode> nodes = currentModel.getNodes();
		Map<String,String> nodesNameAndIpAddress = new HashMap<String,String>();
		String masterNodeIp = "100.100.0.2";
		
		for (ContainerNode node : nodes) {
			if(node.getName().equals(getMasterNodeName())){
				//masterNodeIp = node.getNetworkInformation().get(0).getValues().get(0).getValue();
				Value valueIp = factory.createValue();
				valueIp.setName("lo");
				valueIp.setValue(masterNodeIp);
				List<Value> valueListNetwork = new ArrayList<Value>();
				valueListNetwork.add(valueIp);
				NetworkInfo networkInfo = factory.createNetworkInfo();
				networkInfo.setName("net1");
				networkInfo.addAllValues(valueListNetwork);
				node.addNetworkInformation(networkInfo);
				nodesNameAndIpAddress.put(node.getName(), masterNodeIp);
			}
		}
		
		String newIp = masterNodeIp;
		
		for (ContainerNode node : nodes) {
			if(!node.getName().equals(getMasterNodeName())){
				int ipFragmentFourth = Integer.parseInt(newIp.split("\\.")[3])+1;
				int ipFragmentThird =Integer.parseInt(newIp.split("\\.")[2]);
				if(ipFragmentFourth==255){
					ipFragmentThird = ipFragmentThird+1;
					ipFragmentFourth = 2;
				}
				newIp = masterNodeIp.split("\\.")[0]+"."+masterNodeIp.split("\\.")[1]+"."+ipFragmentThird+"."+ipFragmentFourth;
				Value valueIp = factory.createValue();
				valueIp.setName("lo");
				valueIp.setValue(newIp);
				List<Value> valueListNetwork = new ArrayList<Value>();
				valueListNetwork.add(valueIp);
				NetworkInfo networkInfo = factory.createNetworkInfo();
				networkInfo.setName("net1");
				networkInfo.addAllValues(valueListNetwork);
				node.addNetworkInformation(networkInfo);
				nodesNameAndIpAddress.put(node.getName(), newIp);
			}
		}
		return nodesNameAndIpAddress;
	}
	
	/**
	 * Get the KevScript as String from his path
	 * 
	 * @param PathToKevscript
	 * 		The path of the KevScript
	 * @return
	 * 		The KevScript as String
	 */
	public static String getKevscriptFromPath(String PathToKevscript){
		StringBuilder sbBaseKevScript = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(PathToKevscript))) {
			stream.forEach(line -> sbBaseKevScript.append(line+System.lineSeparator()));
		} catch (IOException ioe) {
			System.out.println("IO exception.");
		} 
		return sbBaseKevScript.toString();
	}

	/**
	 * Create a model from KevScript.
	 * 
	 * @param ks
	 *     The Kevscript
	 * @param model
	 *     The updated model
	 */
	public static void createModelFromKevScript(String ks) {
		try {
			kevScriptService.execute(ks, currentModel);	
		} catch (Exception e) {
			System.out.println("Invalid KevScript");
		}
	}

	/**
	 * Create a new empty model and attach the factory root to this model
	 * 
	 * @return 
	 * 		the empty model
	 */
	private static ContainerRoot createEmptyContainerRoot() {
		ContainerRoot model = factory.createContainerRoot();
		factory.root(model);
		return model;
	}
	
	public static void updateModel(String kevscript) throws Exception {		

		// Clone the current model to make it changeable and initialize an empty container root.
		ContainerRoot initialModel = cloner.clone(currentModel);
		ContainerRoot updatedModel = createEmptyContainerRoot();
		
		// Apply the script on the updated model to get the new configuration
		kevScriptService.execute(kevscript, updatedModel);
		
		// Compare the 2 models and apply differences on initial model
		ModelCompare compare = new DefaultKevoreeFactory().createModelCompare();
		compare.merge(initialModel, updatedModel).applyOn(initialModel);
		
		// Send the new model
		sendModel(initialModel);
	}
	
	/**
	 * Send the model specified in the parameter using a WebSocket
	 * 
	 * @param model
	 * @throws IOException
	 */
	private static void sendModel(ContainerRoot model) throws IOException {
		
		// Serialize the model as JSON
		JSONModelSerializer serializer = factory.createJSONSerializer();
		final String modelStr = serializer.serialize(model);
		
		// send with WebSocket to host:port
		new WebSocketClient(URI.create("ws://"+getNodesNameAndIpAddressFromKevScript().get(getMasterNodeName())+":"+getMasterNodePort())) {
			
			@Override
			public void onOpen() {
				this.send("push/"+modelStr);
				try {
					this.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onMessage(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(Exception arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onClose(int arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}
		};
	}
}
