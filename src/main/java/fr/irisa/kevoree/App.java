package fr.irisa.kevoree;

import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

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

		GUI window = new GUI();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				DockerHelper.removeAllContainer();
			}
		});
	}
}
