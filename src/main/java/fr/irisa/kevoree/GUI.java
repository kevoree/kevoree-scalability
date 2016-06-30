package fr.irisa.kevoree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.kevoree.TypeDefinition;



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
	
	private int numberOfRunningContainer = 0;

	/**
	 * GUI components
	 */
	private JLabel labelWorkflow = new JLabel("Workflow : ");
	private JTextArea textAreaWorkflow = new JTextArea();
	private JScrollPane scrollPaneWorkflow= new JScrollPane(textAreaWorkflow);
	private JLabel labelNumberOfRunningContainer = new JLabel("Number of running container : ");
	private JLabel labelMasterNodeNameAndIp = new JLabel("Master node name and IP : ");
	private JLabel labelTestResults = new JLabel("Test results:");
	private JTextArea textAreaTestResults = new JTextArea();
	private JScrollPane scrollPaneTestResults= new JScrollPane(textAreaTestResults);
	private JButton buttonImportBaseKs = new JButton("Import a base KevScript model");
	private JButton buttonImportUpdatedKs = new JButton("Import an updated KevScript model");
	private JButton buttonRunPlatforms = new JButton("Run Docker containers according to the base KevScript");
	private JButton buttonPushAdaptations = new JButton("Push updated KevScript on running containers");
	private JLabel labelPathBaseKS = new JLabel("Base KevScript path : ");
	private JLabel labelPathUpdatedKS = new JLabel("Updated KevScript path : ");
	private JTextField textFieldPathBaseKS = new JTextField();
	private JTextField textFieldPathUpdatedKS = new JTextField();
	

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
		scrollPaneWorkflow.setBounds(20, 40, 560, 600);
		labelNumberOfRunningContainer.setBounds(20, 660, labelNumberOfRunningContainer.getPreferredSize().width, labelNumberOfRunningContainer.getPreferredSize().height);
		labelMasterNodeNameAndIp.setBounds(20, 680, labelMasterNodeNameAndIp.getPreferredSize().width, labelMasterNodeNameAndIp.getPreferredSize().height);		
		labelPathBaseKS.setBounds(610, 40, labelPathBaseKS.getPreferredSize().width, labelPathBaseKS.getPreferredSize().height);
		textFieldPathBaseKS.setBounds(630 + labelPathBaseKS.getPreferredSize().width, 40, 550 - labelPathBaseKS.getPreferredSize().width, textFieldPathBaseKS.getPreferredSize().height);
		buttonImportBaseKs.setBounds(610, 80, buttonImportBaseKs.getPreferredSize().width, buttonImportBaseKs.getPreferredSize().height);
		buttonRunPlatforms.setBounds(610, 120, buttonRunPlatforms.getPreferredSize().width, buttonRunPlatforms.getPreferredSize().height);
		labelPathUpdatedKS.setBounds(610, 180, labelPathUpdatedKS.getPreferredSize().width, labelPathUpdatedKS.getPreferredSize().height);
		textFieldPathUpdatedKS.setBounds(630 + labelPathUpdatedKS.getPreferredSize().width, 180, 550 - labelPathUpdatedKS.getPreferredSize().width, textFieldPathUpdatedKS.getPreferredSize().height);
		buttonImportUpdatedKs.setBounds(610, 220, buttonImportUpdatedKs.getPreferredSize().width, buttonImportUpdatedKs.getPreferredSize().height);
		buttonPushAdaptations.setBounds(610, 260, buttonPushAdaptations.getPreferredSize().width, buttonPushAdaptations.getPreferredSize().height);
		labelTestResults.setBounds(610, 320, labelTestResults.getPreferredSize().width, labelTestResults.getPreferredSize().height);
		scrollPaneTestResults.setBounds(610, 340, 570, 400);

		buttonImportBaseKs.addActionListener(this);
		buttonRunPlatforms.addActionListener(this);
		buttonImportUpdatedKs.addActionListener(this);
		buttonPushAdaptations.addActionListener(this);

		buttonRunPlatforms.setEnabled(false);
		buttonImportUpdatedKs.setEnabled(false);
		buttonPushAdaptations.setEnabled(false);

		add(labelWorkflow);
		add(scrollPaneWorkflow);
		add(labelNumberOfRunningContainer);
		add(labelMasterNodeNameAndIp);
		add(labelPathBaseKS);
		add(textFieldPathBaseKS);
		add(buttonImportBaseKs);
		add(buttonRunPlatforms);
		add(labelPathUpdatedKS);
		add(textFieldPathUpdatedKS);
		add(buttonImportUpdatedKs);
		add(buttonPushAdaptations);
		add(labelTestResults);
		add(scrollPaneTestResults);

		setVisible(true);
	}
	
	public JTextArea getTextAreaWorkflow() {
		return textAreaWorkflow;
	}
	
	public JLabel getLabelNumberOfRunningContainer() {
		return labelNumberOfRunningContainer;
	}

	/**
	 * Actions performed on buttons
	 * 
	 * @param e
	 * 		ActionEvent performed
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getSource()==buttonImportBaseKs){
			//Delete this path or put your default target for the JFileChooser
			JFileChooser fileChooserBaseKs = new JFileChooser("/home/Savak/Dev/Models/");
			if(fileChooserBaseKs.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
				baseKevScriptPath=fileChooserBaseKs.getSelectedFile().getPath();
				textFieldPathBaseKS.setText(baseKevScriptPath);
				baseKevScript = KevoreeHelper.getKevscriptFromPath(baseKevScriptPath);
				KevoreeHelper.createModelFromKevScript(baseKevScript);
				
				buttonRunPlatforms.setEnabled(true);
			}			
		}

		if(e.getSource()==buttonRunPlatforms){
			
			// get the name of the master node
			String masterNodeName = KevoreeHelper.getMasterNodeName();

			// Get a Map with nodes name as keys and type definition as value
			Map<String,TypeDefinition> nodesNameAndTypeDef = KevoreeHelper.getNodesNameAndTypeDefFromKevScript();

			// Get a Map with nodes name as keys and IP address as value
			Map<String,String> nodesNameAndIp = KevoreeHelper.getNodesNameAndIpAddressFromKevScript();
			
			// Create the overlay network according to the IP of the master node
			DockerHelper.createNetwork();
			
			labelMasterNodeNameAndIp.setText(labelMasterNodeNameAndIp.getText()+masterNodeName+" --> "+nodesNameAndIp.get(masterNodeName));
			labelMasterNodeNameAndIp.setBounds(20, 680, labelMasterNodeNameAndIp.getPreferredSize().width, labelMasterNodeNameAndIp.getPreferredSize().height);
			
			// Implementation of an ExecutorService
			ExecutorService executorService = Executors.newFixedThreadPool(nodesNameAndIp.keySet().size());

			Map<String, List<String>> clusterLogin = ClusterHelper.clusterLogin;
			for (String login : clusterLogin.keySet()) {
				ClusterHelper.copyKevsciptToAllClusterNode(login, clusterLogin.get(login).get(0), clusterLogin.get(login).get(1), baseKevScriptPath);
			}
			
			
			for (String nodeName : nodesNameAndTypeDef.keySet()) {

				if(nodesNameAndTypeDef.get(nodeName).getName().equals("JavascriptNode")){
					Runnable taskStartContainerJsNode = () -> {
						DockerHelper.startContainerJsNode(nodeName, baseKevScriptPath, nodesNameAndIp.get(nodeName));
						textAreaWorkflow.append("Starting "+nodeName+"Container --> IP : "+nodesNameAndIp.get(nodeName)+"\n");
						numberOfRunningContainer=numberOfRunningContainer+1;
						labelNumberOfRunningContainer.setText("Number of running container : "+numberOfRunningContainer);
						labelNumberOfRunningContainer.setBounds(20, 660, labelNumberOfRunningContainer.getPreferredSize().width, labelNumberOfRunningContainer.getPreferredSize().height);
				
					};
					executorService.execute(taskStartContainerJsNode);
				}
				if(nodesNameAndTypeDef.get(nodeName).getName().equals("JavaNode")){
					Runnable taskStartContainerJavaNode = () -> {
						DockerHelper.startContainerJavaNode(nodeName, baseKevScriptPath, nodesNameAndIp.get(nodeName));
						textAreaWorkflow.append("Starting "+nodeName+"Container --> IP : "+nodesNameAndIp.get(nodeName)+"\n");
						numberOfRunningContainer=numberOfRunningContainer+1;
						labelNumberOfRunningContainer.setText("Number of running container : "+numberOfRunningContainer);
						labelNumberOfRunningContainer.setBounds(20, 660, labelNumberOfRunningContainer.getPreferredSize().width, labelNumberOfRunningContainer.getPreferredSize().height);
						
					};
					executorService.execute(taskStartContainerJavaNode);
				}
			}

			buttonImportUpdatedKs.setEnabled(true);
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

		if(e.getSource()==buttonPushAdaptations){

			try {
				KevoreeHelper.updateModel(updatedKevScript);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
