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
import org.kevoree.FragmentDictionary;
import org.kevoree.Group;
import org.kevoree.TypeDefinition;
import org.kevoree.Value;
import org.kevoree.annotation.KevoreeInject;
import org.kevoree.api.KevScriptService;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.core.impl.ContextAwareAdapter;
import org.kevoree.core.impl.ContextAwareModelService;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.kevscript.Parser;
import org.kevoree.pmodeling.api.ModelCloner;
import org.kevoree.pmodeling.api.compare.ModelCompare;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;

import fr.braindead.websocket.client.WebSocketClient;

/**
 * Created by Savak on 08/06/16.
 */

public class KevoreeHelper {
	// Init the variables (from inside a component)
	private final KevoreeFactory factory;

	private final ModelCloner cloner;

	private KevScriptService kevScriptService;

	private ContainerRoot currentModel;
	

	public KevoreeHelper(String ks) {
		factory = new DefaultKevoreeFactory();

		cloner = factory.createModelCloner();

		kevScriptService = new KevScriptEngine();
		
		ContainerRoot model = createEmptyContainerRoot();

		updateModelFromKevScript(ks, model);

	}

	public void updateModel(String kevscript) throws Exception {		

		// Clone the model to make it changeable
		ContainerRoot localModel = cloner.clone(currentModel);
		ContainerRoot kevsModel = createEmptyContainerRoot();
		
		// Apply the script on the current model, to get a new configuration
		kevScriptService.execute(kevscript, kevsModel);
		
		ModelCompare compare = new DefaultKevoreeFactory().createModelCompare();
		compare.merge(localModel, kevsModel).applyOn(localModel);
		
		//Ask the platform to apply the new model; register an optional callback to know when the adaptation is finished.
		
		sendModel(localModel);
	}
	
	private void sendModel(ContainerRoot model) throws IOException {
		
		JSONModelSerializer serializer = factory.createJSONSerializer();
		final String modelStr = serializer.serialize(model);
		
		// send with WebSocket to host:port:path
		WebSocketClient wsClient = new WebSocketClient(URI.create("ws://"+getNodesNameAndIpAddressFromKevScript().get(getMasterNodeName())+":"+getMasterNodePort())) {
			
			@Override
			public void onOpen() {
				// t'es connecté au master node sur le fragment du group
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

	/**
	 * Return a HashMap<String,TypeDefinition> with the names and the TypeDefinition of the nodes within the KevScript
	 * 
	 * @return a HashMap<String,TypeDefinition> with the names and the TypeDefinition of the nodes within the KevScript
	 */
	public Map<String,TypeDefinition> getNodesNameAndTypeDefFromKevScript() {
		List<ContainerNode> nodes = currentModel.getNodes();
		Map<String,TypeDefinition> nodesNameAndTypeDef = new HashMap<String,TypeDefinition>();
		for (ContainerNode node : nodes) {
			nodesNameAndTypeDef.put(node.getName(), node.getTypeDefinition());
		}
		return nodesNameAndTypeDef;
	}

	public String getMasterNodeName(){
		String masterNode = "";

		List<Group> groups = currentModel.getGroups();

		for (Group group : groups) {
			List<Value> groupDictionary = group.getDictionary().getValues();
			for (Value value : groupDictionary) {
				if(value.getName().equals("master")){
					masterNode = value.getValue();
				}
			}
		}
		return masterNode;
	}

	public Map<String,String> getNodesNameAndIpAddressFromKevScript(){
		List<ContainerNode> nodes = currentModel.getNodes();
		Map<String,String> nodesNameAndIpAddress = new HashMap<String,String>();
		for (ContainerNode node : nodes) {
			nodesNameAndIpAddress.put(node.getName(), node.getNetworkInformation().get(0).getValues().get(0).getValue());
		}
		return nodesNameAndIpAddress;
	}
	
	public String getMasterNodePort(){
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
	
	public static String getKevscriptFromPath(String PathToKevscript){
		StringBuilder sbBaseKevScript = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(PathToKevscript))) {
			stream.forEach(line -> sbBaseKevScript.append(line+System.lineSeparator()));
		} catch (IOException IOe) {
			IOe.printStackTrace();
		}
		return sbBaseKevScript.toString();
	}

	/**
	 * Update a model from KevScript.
	 * 
	 * @param ks
	 *     The Kevscript
	 * @param model
	 *     The updated model
	 */
	private void updateModelFromKevScript(String ks, ContainerRoot model) {
		try {
			kevScriptService.execute(ks, model);
			setCurrentModel(model);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create a new empty model and attach the factory root to this model
	 * 
	 * @return the empty model
	 */
	private ContainerRoot createEmptyContainerRoot() {
		ContainerRoot model = factory.createContainerRoot();
		factory.root(model);
		return model;
	}
	
	private void setCurrentModel(ContainerRoot model){
		this.currentModel = model;
	}
	

}
