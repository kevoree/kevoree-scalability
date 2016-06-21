package fr.irisa.kevoree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.kevoree.TypeDefinition;

import com.github.dockerjava.api.model.Node;



/**
 * Created by Savak on 26/05/16.
 */

public class GUI extends JFrame implements ActionListener{

	private String baseKevScriptPath;
	private String baseKevScript;
	private String updatedKevScriptPath;
	private String updatedKevScript;

	private KevoreeHelper kh;

	//private Process processJS;
	//private Process processJava;

	private JLabel labelKevoreeJSPlatform = new JLabel("Kevoree JS Docker containers : ");
	private JLabel labelKevoreeJavaPlatform = new JLabel("Kevoree Java Docker containers : ");
	private JLabel labelTestResults = new JLabel("Test results:");

	private Map<String,JTextArea> textAreaJsOutpoutList = new HashMap<String,JTextArea>();
	private Map<String,JTextArea> textAreaJavaOutpoutList = new HashMap<String,JTextArea>();

	private Map<String,JScrollPane> scrollPaneJsOutpoutList = new HashMap<String,JScrollPane>();
	private Map<String,JScrollPane> scrollPaneJavaOutpoutList = new HashMap<String,JScrollPane>();

	//	private JTextArea textAreaKevoreeJSOutput = new JTextArea();
	//	private JScrollPane scrollPaneJSOutput = new JScrollPane(textAreaKevoreeJSOutput);
	//	private JTextArea textAreaKevoreeJavaOutput = new JTextArea();
	//	private JScrollPane scrollPaneJavaOutput = new JScrollPane(textAreaKevoreeJavaOutput);

	private JTextArea textAreaTestResults = new JTextArea();
	private JScrollPane scrollPaneTestResults= new JScrollPane(textAreaTestResults);
	private JButton buttonImportBaseKs = new JButton("Import a base KevScript model");
	private JButton buttonImportUpdatedKs = new JButton("Import an updated KevScript model");
	private JButton buttonRunPlatforms = new JButton("Run Docker containers");
	private JButton buttonPushAdaptations = new JButton("Push updated KevScript");
	private JLabel labelPathBaseKS = new JLabel("Base KevScript path : ");
	private JLabel labelPathUpdatedKS = new JLabel("Updated KevScript path : ");
	private JTextField textFieldPathBaseKS = new JTextField();
	private JTextField textFieldPathUpdatedKS = new JTextField();
	private JTabbedPane tabbedPaneJsOutpout = new JTabbedPane();
	private JTabbedPane tabbedPaneJavaOutpout = new JTabbedPane();

	public GUI() {
		setTitle("Kevoree-Scalability");
		setSize(1200, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(null);

		labelKevoreeJSPlatform.setBounds(20, 20, labelKevoreeJSPlatform.getPreferredSize().width, labelKevoreeJSPlatform.getPreferredSize().height);
		tabbedPaneJsOutpout.setBounds(20, 40, 570, 320);
		labelKevoreeJavaPlatform.setBounds(20, 400, labelKevoreeJavaPlatform.getPreferredSize().width, labelKevoreeJavaPlatform.getPreferredSize().height);
		tabbedPaneJavaOutpout.setBounds(20, 420, 570, 320);
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

		add(labelKevoreeJSPlatform);
		add(labelKevoreeJavaPlatform);
		add(tabbedPaneJsOutpout);
		add(tabbedPaneJavaOutpout);
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

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getSource()==buttonImportBaseKs){
			JFileChooser fileChooserBaseKs = new JFileChooser("/home/Savak/Dev/Models/");
			if(fileChooserBaseKs.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
				baseKevScriptPath=fileChooserBaseKs.getSelectedFile().getPath();
			}else{

			}
			textFieldPathBaseKS.setText(baseKevScriptPath);
			baseKevScript = KevoreeHelper.getKevscriptFromPath(baseKevScriptPath);
			kh = new KevoreeHelper(baseKevScript);
			buttonRunPlatforms.setEnabled(true);
		}

		if(e.getSource()==buttonRunPlatforms){

			Map<String,TypeDefinition> nodesNameAndTypeDef = kh.getNodesNameAndTypeDefFromKevScript();
			Map<String,String> nodesNameAndIp = kh.getNodesNameAndIpAddressFromKevScript();

			for (String nodeName : nodesNameAndTypeDef.keySet()) {

				if(nodesNameAndTypeDef.get(nodeName).getName().equals("JavascriptNode")){
					Runnable taskStartContainerJsNode = () -> {
						DockerHelper.startContainerJsNode(nodeName, baseKevScriptPath, nodesNameAndIp.get(nodeName));
						textAreaJsOutpoutList.put(nodeName, new JTextArea());
						scrollPaneJsOutpoutList.put(nodeName, new JScrollPane(textAreaJsOutpoutList.get(nodeName)));
						tabbedPaneJsOutpout.addTab(nodeName+" container", scrollPaneJsOutpoutList.get(nodeName));
						
						try {
							String s;
							Process processJS = Runtime.getRuntime().exec("docker logs --follow "+nodeName+"Container");
							BufferedReader br = new BufferedReader(new InputStreamReader(processJS.getInputStream()));
							while ((s = br.readLine()) != null) {
								textAreaJsOutpoutList.get(nodeName).append(s+"\n");
							}
							processJS.waitFor();
							System.out.println("exit : " + processJS.exitValue());
							processJS.destroy();
						}catch (Exception eJS) {
							eJS.printStackTrace();
						}
					};
					new Thread(taskStartContainerJsNode).start();
				}
				if(nodesNameAndTypeDef.get(nodeName).getName().equals("JavaNode")){
					Runnable taskStartContainerJavaNode = () -> {
						DockerHelper.startContainerJavaNode(nodeName, baseKevScriptPath, nodesNameAndIp.get(nodeName));		
						textAreaJavaOutpoutList.put(nodeName, new JTextArea());
						scrollPaneJavaOutpoutList.put(nodeName, new JScrollPane(textAreaJavaOutpoutList.get(nodeName)));
						tabbedPaneJavaOutpout.addTab(nodeName+" container", scrollPaneJavaOutpoutList.get(nodeName));
						
						try {							
							Process processJava = Runtime.getRuntime().exec("docker logs --follow "+nodeName+"Container");
							BufferedReader br = new BufferedReader(new InputStreamReader(processJava.getInputStream()));
							String s = null;
							while ((s = br.readLine()) != null) {
								//System.out.println("KevoreeJS line : " + s);
								textAreaJavaOutpoutList.get(nodeName).append(s+"\n");
							}
							processJava.waitFor();
							System.out.println("exit : " + processJava.exitValue());
							processJava.destroy();
						} catch (Exception eJava) {
							eJava.printStackTrace();
						}
					};
					new Thread(taskStartContainerJavaNode).start();
				}
			}
			
			buttonImportUpdatedKs.setEnabled(true);
		}

		if(e.getSource()==buttonImportUpdatedKs){
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
				kh.updateModel(updatedKevScript);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
