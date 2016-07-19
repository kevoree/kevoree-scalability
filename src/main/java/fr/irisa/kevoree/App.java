package fr.irisa.kevoree;

import java.util.ArrayList;

/**
 *
 */
public class App
{
	public static void main( String[] args )
	{
		System.out.println("===========================");
		System.out.println("Running Kevoree-Scalability");
		System.out.println("===========================");
		
		
		System.setProperty("kevoree.registry", "http://" + Config.REGISTRY_HOST + ":" + Config.REGISTRY_PORT);

		new GUI();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				DockerHelper.removeAllContainer();
				DockerHelper.removeNetwork();

			}
		});
	}
}
