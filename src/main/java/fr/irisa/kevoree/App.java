package fr.irisa.kevoree;

/**
 *
 *
 */
public class App
{
	public static void main( String[] args )
	{
		System.out.println("===========================");
		System.out.println("Running Kevoree-Scalability");
		System.out.println("===========================");

		GUI gui = new GUI();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				DockerHelper.removeAllContainer(gui);
				DockerHelper.removeNetwork();
			}
		});
	}
}
