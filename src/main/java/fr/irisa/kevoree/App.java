package fr.irisa.kevoree;

/**
 *	Main class
 */
public class App
{
	public static void main( String[] args )
	{
		System.out.println("===========================");
		System.out.println("Running Kevoree-Scalability");
		System.out.println("===========================");
		
		
		System.setProperty("kevoree.registry", "http://" + Config.KEVOREE_REGISTRY_HOST + ":" + Config.KEVOREE_REGISTRY_PORT);

		new GUI();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				DockerHelper.removeAllContainers();
				DockerHelper.removeNetwork();
			}
		});
	}
}
