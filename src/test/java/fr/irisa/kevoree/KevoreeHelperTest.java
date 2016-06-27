package fr.irisa.kevoree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class KevoreeHelperTest {

	@Test
	public void test1() {
		
		String ks = "add javaNode : JavaNode\n" + "add jsNode : JavascriptNode\n" + "add group : WSGroup\n"
				+ "set group.master = 'jsNode'\n" + "attach javaNode, jsNode group";
	
		KevoreeHelper.createModelFromKevScript(ks);
		String master = KevoreeHelper.getMasterNodeName();

		String trueMaster = "jsNode";

		Assert.assertEquals(master, trueMaster);
	}

	@Test
	public void test2() {
		String ksPath = "/home/Savak/Dev/Models/bigModel.kevs";
		
		//read file into stream, try-with-resources
		StringBuilder sbKevScript = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(ksPath))) {
			stream.forEach(line -> sbKevScript.append(line+System.lineSeparator()));
		} catch (IOException IOe) {
			IOe.printStackTrace();
		}
		String ks = sbKevScript.toString();
		
		KevoreeHelper.createModelFromKevScript(ks);
		Map<String,String> nodesAndIp = KevoreeHelper.getNodesNameAndIpAddressFromKevScript();
		
		for (String nodeName : nodesAndIp.keySet()) {
			System.out.println(nodesAndIp.get(nodeName));
		}
		
		Assert.assertTrue(true);
	}
	
	@Test
	public void test3(){
		String ksPath = "/home/Savak/Dev/Models/bigModel.kevs";
		
		//read file into stream, try-with-resources
		StringBuilder sbKevScript = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(ksPath))) {
			stream.forEach(line -> sbKevScript.append(line+System.lineSeparator()));
		} catch (IOException IOe) {
			IOe.printStackTrace();
		}
		String ks = sbKevScript.toString();

		KevoreeHelper.createModelFromKevScript(ks);
		String nodeMesterPort = KevoreeHelper.getMasterNodePort();
		
		System.out.println("Node master port = "+nodeMesterPort);
		
		Assert.assertEquals(nodeMesterPort, "9000");
	}
}
