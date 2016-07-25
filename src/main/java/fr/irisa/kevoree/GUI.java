package fr.irisa.kevoree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;



/**
 * Graphical User Interface class and actionPerformed by buttons
 * 
 * @author Savak
 * @version 1.0
 */

public class GUI extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	/**
	 * Kevscript path and script
	 */
	private String baseKevScriptPath;
	private String baseKevScript;
	private String updatedKevScriptPath;
	private String updatedKevScript;

	private File jsonModel = null;
	
	private int numberOfRunningContainer = 0;

	/**
	 * GUI components
	 */
	JLabel labelWorkflow = new JLabel("Workflow : ");
	JTextArea textAreaWorkflow = new JTextArea();
	JScrollPane scrollPaneWorkflow= new JScrollPane(textAreaWorkflow);
	JLabel labelNumberOfRunningContainer = new JLabel("Number of running container : ");
	JLabel labelMasterNodeNameAndIp = new JLabel("Master node name and IP : ");
	JButton buttonImportBaseKs = new JButton("Import a base KevScript model");
	JButton buttonImportUpdatedKs = new JButton("Import an updated KevScript model");
	JButton buttonRunPlatforms = new JButton("Run Docker containers according to the base KevScript");
	JButton buttonPushAdaptations = new JButton("Push updated KevScript on running containers");
	JLabel labelPathBaseKS = new JLabel("Base KevScript path : ");
	JLabel labelPathUpdatedKS = new JLabel("Updated KevScript path : ");
	JTextField textFieldPathBaseKS = new JTextField();
	JTextField textFieldPathUpdatedKS = new JTextField();
	JTextField textFieldNumberOfNodes= new JTextField();
	JTextField textFieldNumberOfComponents= new JTextField();
	JTextField textFieldNumberOfChannels= new JTextField();
	JButton buttonImportRandomBaseKs = new JButton("Import a random KevScript model according to these previous properties");
	JLabel labelModelGenerator = new JLabel("<html><h2>Random model generator</h2></html>");
	JLabel labelNumberOfNodes = new JLabel("Number of nodes : ");
	JLabel labelNumberOfComponents = new JLabel("Number of components : ");
	JLabel labelNumberOfChannels = new JLabel("Number of channels : ");
	JLabel labelLogs = new JLabel("Logs : ");
	JTextArea textAreaLogs = new JTextArea();
	JScrollPane scrollPaneLogs= new JScrollPane(textAreaLogs);
	JButton buttonGetLogs = new JButton("Get logs of the following container :");
	JComboBox<String> comboBoxRunningContainer = new JComboBox<String>();
	
	/**
	 * GUI constructor
	 */
	public GUI() {
		setTitle("Kevoree-Scalability");
		setSize(1200, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(null);

		labelWorkflow.setBounds(20, 20, labelWorkflow.getPreferredSize().width, labelWorkflow.getPreferredSize().height);
		scrollPaneWorkflow.setBounds(20, 40, 560, 280);
		labelLogs.setBounds(20, 340, labelLogs.getPreferredSize().width, labelLogs.getPreferredSize().height);
		scrollPaneLogs.setBounds(20, 360, 560, 280);
		buttonGetLogs.setBounds(20, 660, buttonGetLogs.getPreferredSize().width, buttonGetLogs.getPreferredSize().height);
		comboBoxRunningContainer.setBounds(40+buttonGetLogs.getPreferredSize().width, 660, 200, comboBoxRunningContainer.getPreferredSize().height);
		labelNumberOfRunningContainer.setBounds(20, 700, labelNumberOfRunningContainer.getPreferredSize().width, labelNumberOfRunningContainer.getPreferredSize().height);
		labelMasterNodeNameAndIp.setBounds(20, 720, labelMasterNodeNameAndIp.getPreferredSize().width, labelMasterNodeNameAndIp.getPreferredSize().height);		
		labelPathBaseKS.setBounds(610, 40, labelPathBaseKS.getPreferredSize().width, labelPathBaseKS.getPreferredSize().height);
		textFieldPathBaseKS.setBounds(630 + labelPathBaseKS.getPreferredSize().width, 40, 550 - labelPathBaseKS.getPreferredSize().width, textFieldPathBaseKS.getPreferredSize().height);
		buttonImportBaseKs.setBounds(610, 80, buttonImportBaseKs.getPreferredSize().width, buttonImportBaseKs.getPreferredSize().height);
		buttonRunPlatforms.setBounds(610, 120, buttonRunPlatforms.getPreferredSize().width, buttonRunPlatforms.getPreferredSize().height);
		labelPathUpdatedKS.setBounds(610, 180, labelPathUpdatedKS.getPreferredSize().width, labelPathUpdatedKS.getPreferredSize().height);
		textFieldPathUpdatedKS.setBounds(630 + labelPathUpdatedKS.getPreferredSize().width, 180, 550 - labelPathUpdatedKS.getPreferredSize().width, textFieldPathUpdatedKS.getPreferredSize().height);
		buttonImportUpdatedKs.setBounds(610, 220, buttonImportUpdatedKs.getPreferredSize().width, buttonImportUpdatedKs.getPreferredSize().height);
		buttonPushAdaptations.setBounds(610, 260, buttonPushAdaptations.getPreferredSize().width, buttonPushAdaptations.getPreferredSize().height);
		labelModelGenerator.setBounds(610, 320, labelModelGenerator.getPreferredSize().width, labelModelGenerator.getPreferredSize().height);
		labelNumberOfNodes.setBounds(610, 370, labelNumberOfNodes.getPreferredSize().width, labelNumberOfNodes.getPreferredSize().height);
		labelNumberOfComponents.setBounds(610, 410, labelNumberOfComponents.getPreferredSize().width, labelNumberOfComponents.getPreferredSize().height);
		labelNumberOfChannels.setBounds(610, 450, labelNumberOfChannels.getPreferredSize().width, labelNumberOfChannels.getPreferredSize().height);
		textFieldNumberOfNodes.setBounds(620+labelNumberOfNodes.getPreferredSize().width, 370, 50, textFieldNumberOfNodes.getPreferredSize().height);
		textFieldNumberOfComponents.setBounds(620+labelNumberOfComponents.getPreferredSize().width, 410, 50, textFieldNumberOfComponents.getPreferredSize().height);
		textFieldNumberOfChannels.setBounds(620+labelNumberOfChannels.getPreferredSize().width, 450, 50, textFieldNumberOfChannels.getPreferredSize().height);
		buttonImportRandomBaseKs.setBounds(610, 490, buttonImportRandomBaseKs.getPreferredSize().width, buttonImportRandomBaseKs.getPreferredSize().height);

		buttonImportBaseKs.addActionListener(this);
		buttonRunPlatforms.addActionListener(this);
		buttonImportUpdatedKs.addActionListener(this);
		buttonPushAdaptations.addActionListener(this);
		buttonImportRandomBaseKs.addActionListener(this);
		buttonGetLogs.addActionListener(this);

		buttonRunPlatforms.setEnabled(false);
		buttonImportUpdatedKs.setEnabled(false);
		buttonPushAdaptations.setEnabled(false);
		buttonGetLogs.setEnabled(false);

		add(labelWorkflow);
		add(scrollPaneWorkflow);
		add(labelLogs);
		add(scrollPaneLogs);
		add(comboBoxRunningContainer);
		add(buttonGetLogs);
		add(labelNumberOfRunningContainer);
		add(labelMasterNodeNameAndIp);
		add(labelPathBaseKS);
		add(textFieldPathBaseKS);
		add(buttonImportBaseKs);
		add(buttonImportRandomBaseKs);
		add(buttonRunPlatforms);
		add(labelPathUpdatedKS);
		add(textFieldPathUpdatedKS);
		add(buttonImportUpdatedKs);
		add(buttonPushAdaptations);
		add(textFieldNumberOfNodes);
		add(textFieldNumberOfComponents);
		add(textFieldNumberOfChannels);
		add(labelModelGenerator);
		add(labelNumberOfNodes);
		add(labelNumberOfComponents);
		add(labelNumberOfChannels);

		setVisible(true);
	}

	/**
	 * Actions performed on buttons
	 * 
	 * @param e
	 * 		ActionEvent performed
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		// If the source of the click is "Import a base KevScript model"
		if(e.getSource()==buttonImportBaseKs){
			
			JFileChooser fileChooserBaseKs = new JFileChooser();
			
			if(fileChooserBaseKs.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
				// Save the path of the selected file
				baseKevScriptPath=fileChooserBaseKs.getSelectedFile().getPath();
				// Display this path on a textfield  
				textFieldPathBaseKS.setText(baseKevScriptPath);
				// Save the file content on a String
				baseKevScript = KevoreeHelper.getKevscriptFromPath(baseKevScriptPath);
				// Create the Kevoree model according to the Kevscript
				KevoreeHelper.createModelFromKevScript(baseKevScript);
				
				buttonRunPlatforms.setEnabled(true);
			}			
		}

		// If the source of the click is "Run Docker containers according to the base KevScript"
		if(e.getSource()==buttonRunPlatforms){

			// Get the name of the master node
			String masterNodeName = KevoreeHelper.getMasterNodeName();

			// Get a Map with nodes name as keys and type definition as value
			Map<String,TypeDefinition> nodesNameAndTypeDef = KevoreeHelper.getNodesNameAndTypeDefFromKevScript();

			// Get a Map with nodes name as keys and IP address as value
			Map<String,String> nodesNameAndIp = KevoreeHelper.getNodesNameAndIpAddressFromKevScript();

			// Create the overlay network according to the IP of the master node
			DockerHelper.createNetwork();

			try {
				// Create the JSON file to send in Docker container
				jsonModel = new File("model.json");
				FileOutputStream fileOutputStream = new FileOutputStream(jsonModel);
				new DefaultKevoreeFactory().createJSONSerializer().serializeToStream(KevoreeHelper.currentModel, fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			// Display the master node name
			labelMasterNodeNameAndIp.setText(labelMasterNodeNameAndIp.getText()+masterNodeName+" --> "+nodesNameAndIp.get(masterNodeName));
			labelMasterNodeNameAndIp.setBounds(20, 700, labelMasterNodeNameAndIp.getPreferredSize().width, labelMasterNodeNameAndIp.getPreferredSize().height);

			// Implementation of an ExecutorService (1 thread per node)
			ExecutorService executorService = Executors.newFixedThreadPool(KevoreeHelper.nodesNumber);

			// Copy the JSON model on all the cluster machine 
			Map<String, List<String>> clusterLogin = ClusterHelper.clusterLogins;
			for (String login : clusterLogin.keySet()) {
				ClusterHelper.copyJsonModelToAllClusterNode(login, clusterLogin.get(login).get(0), clusterLogin.get(login).get(1), jsonModel.getPath());
			}

			
			
			// For all nodes, start a Docker container and run his Kevoree platform
			for (String nodeName : nodesNameAndTypeDef.keySet()) {
				if(nodesNameAndTypeDef.get(nodeName).getName().equals("JavascriptNode")){
					Runnable taskStartContainerJsNode = () -> {
						DockerHelper.startContainerJsNode(nodeName, nodesNameAndIp.get(nodeName));
						textAreaWorkflow.append("Starting "+nodesNameAndTypeDef.get(nodeName).getName()+" "+nodeName+"Container --> IP : "+nodesNameAndIp.get(nodeName)+"\n");
						numberOfRunningContainer=numberOfRunningContainer+1;
						labelNumberOfRunningContainer.setText("Number of running container : "+numberOfRunningContainer);
						labelNumberOfRunningContainer.setBounds(20, 720, labelNumberOfRunningContainer.getPreferredSize().width, labelNumberOfRunningContainer.getPreferredSize().height);
						comboBoxRunningContainer.addItem(nodeName+"Container");

					};
					executorService.execute(taskStartContainerJsNode);
				}
				if(nodesNameAndTypeDef.get(nodeName).getName().equals("JavaNode")){
					Runnable taskStartContainerJavaNode = () -> {
						DockerHelper.startContainerJavaNode(nodeName, nodesNameAndIp.get(nodeName));
						textAreaWorkflow.append("Starting "+nodesNameAndTypeDef.get(nodeName).getName()+" "+nodeName+"Container --> IP : "+nodesNameAndIp.get(nodeName)+"\n");
						numberOfRunningContainer=numberOfRunningContainer+1;
						labelNumberOfRunningContainer.setText("Number of running container : "+numberOfRunningContainer);
						labelNumberOfRunningContainer.setBounds(20, 720, labelNumberOfRunningContainer.getPreferredSize().width, labelNumberOfRunningContainer.getPreferredSize().height);
						comboBoxRunningContainer.addItem(nodeName+"Container");
					};
					executorService.execute(taskStartContainerJavaNode);
				}
			}

			buttonImportUpdatedKs.setEnabled(true);
			buttonGetLogs.setEnabled(true);
		}

		if(e.getSource()==buttonImportUpdatedKs){
			//Delete this path or put your default target for the JFileChooser
			JFileChooser fileChooserUpdatedKs = new JFileChooser("/home/Savak/Dev/Models/");
			if(fileChooserUpdatedKs.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
				updatedKevScriptPath=fileChooserUpdatedKs.getSelectedFile().getPath();
			}else{

			}
			textFieldPathUpdatedKS.setText(updatedKevScriptPath);
			updatedKevScript = KevoreeHelper.getKevscriptFromPath(updatedKevScriptPath);
			buttonPushAdaptations.setEnabled(true);
		}

		// TODO
		if(e.getSource()==buttonPushAdaptations){

			try {
				KevoreeHelper.updateModel(updatedKevScript);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// If the source of the click is "Import a random KevScript model according to these previous properties"
		if(e.getSource()==buttonImportRandomBaseKs){
			try {
				String[] temp = new String[0];

				// Properties of the future model
				int numberOfNodes = Integer.parseInt(textFieldNumberOfNodes.getText());
				int numberOfComponents = Integer.parseInt(textFieldNumberOfNodes.getText());
				int numberOfChannels = Integer.parseInt(textFieldNumberOfNodes.getText());

				// Generate the Kevscript model0.kevs at the root of the project
				ModelGenerator.generateModel(temp, numberOfNodes, numberOfComponents, numberOfChannels);

				File fileKevscript = new File("model0.kevs");

				// Save and display the path of the base Kevscript and create his model
				baseKevScriptPath=fileKevscript.getPath();
				textFieldPathBaseKS.setText(baseKevScriptPath);
				baseKevScript = KevoreeHelper.getKevscriptFromPath(baseKevScriptPath);
				KevoreeHelper.createModelFromKevScript(baseKevScript);

				buttonRunPlatforms.setEnabled(true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(e.getSource()==buttonGetLogs){
			System.out.println("Logs here");
			System.out.println(DockerHelper.getContainerLogs(comboBoxRunningContainer.getSelectedItem().toString()));
			textAreaLogs.setText(DockerHelper.getContainerLogs(comboBoxRunningContainer.getSelectedItem().toString()));
		}
	}
}
