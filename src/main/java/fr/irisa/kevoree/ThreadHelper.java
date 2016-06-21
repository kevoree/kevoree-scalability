package fr.irisa.kevoree;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

// Source : http://nadeausoftware.com/articles/2008/04/java_tip_how_list_and_find_threads_and_thread_groups#GettingThreadGroups
public class ThreadHelper {
	
	/**
	 * Getting the root thread group
	 */
	public static ThreadGroup getRootThreadGroup( ) {
	    ThreadGroup threadGroup = Thread.currentThread( ).getThreadGroup( );
	    ThreadGroup threadGroupParent;
	    while ( (threadGroupParent = threadGroup.getParent( )) != null )
	        threadGroup = threadGroupParent;
	    return threadGroup;
	}
	
	/**
	 * Getting a list of all thread groups
	 */
	public static ThreadGroup[] getAllThreadGroups( ) {
	    final ThreadGroup root = getRootThreadGroup( );
	    int nAlloc = root.activeGroupCount( );
	    int n = 0;
	    ThreadGroup[] groups;
	    do {
	        nAlloc *= 2;
	        groups = new ThreadGroup[ nAlloc ];
	        n = root.enumerate( groups, true );
	    } while ( n == nAlloc );
	 
	    ThreadGroup[] allGroups = new ThreadGroup[n+1];
	    allGroups[0] = root;
	    System.arraycopy( groups, 0, allGroups, 1, n );
	    return allGroups;
	}
	
	/**
	 * Getting a thread group by name
	 * 
	 * @param name
	 *     The name of the thread group to return
	 */
	public static ThreadGroup getThreadGroup( final String name ) {
	    if ( name == null )
	        throw new NullPointerException( "Null name" );
	    final ThreadGroup[] groups = getAllThreadGroups( );
	    for ( ThreadGroup group : groups )
	        if ( group.getName( ).equals( name ) )
	            return group;
	    return null;
	}
	
	/**
	 * Getting a list of all threads
	 */
	public static Thread[] getAllThreads( ) {
	    final ThreadGroup root = getRootThreadGroup( );
	    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
	    int nAlloc = thbean.getThreadCount( );
	    int n = 0;
	    Thread[] threads;
	    do {
	        nAlloc *= 2;
	        threads = new Thread[ nAlloc ];
	        n = root.enumerate( threads, true );
	    } while ( n == nAlloc );
	    return java.util.Arrays.copyOf( threads, n );
	}
	
	/**
	 * Getting a list of all threads in a thread group
	 * 
	 * @param group
	 *     The threads which are returned provide to this group
	 */
	public static Thread[] getGroupThreads( final ThreadGroup group ) {
	    if ( group == null )
	        throw new NullPointerException( "Null thread group" );
	    int nAlloc = group.activeCount( );
	    int n = 0;
	    Thread[] threads;
	    do {
	        nAlloc *= 2;
	        threads = new Thread[ nAlloc ];
	        n = group.enumerate( threads );
	    } while ( n == nAlloc );
	    return java.util.Arrays.copyOf( threads, n );
	}
	
	/**
	 * Getting a list of all threads in a specific state
	 * 
	 * @param state
	 *     The state of the thread we want
	 */
	public static Thread[] getAllThreads( final Thread.State state ) {
	    final Thread[] allThreads = getAllThreads( );
	    final Thread[] found = new Thread[allThreads.length];
	    int nFound = 0;
	    for ( Thread thread : allThreads )
	        if ( thread.getState( ) == state )
	            found[nFound++] = thread; 
	    return java.util.Arrays.copyOf( found, nFound );
	}
	
	/**
	 * Getting a thread by name
	 * 
	 * @param group
	 *     The name of the thread we want
	 */
	public static Thread getThread( final String name ) {
	    if ( name == null )
	        throw new NullPointerException( "Null name" );
	    final Thread[] threads = getAllThreads( );
	    for ( Thread thread : threads )
	        if ( thread.getName( ).equals( name ) )
	            return thread;
	    return null;
	}

}
