package io.pivotal.gedi.geode.security;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.geode.security.NotAuthorizedException;
import org.junit.Test;
import gedi.solutions.geode.client.GemFireJmxClient;
import nyla.solutions.core.patterns.jmx.JMX;
import nyla.solutions.core.patterns.jmx.JMXConnectionException;

public class JmxIntTest
{
	private String adminUser = "admin", dataUser = "data";
	private char[] adminPassword = "admin".toCharArray(),
	dataUserPassword = "data".toCharArray();
	private String host = "localhost";
	private int jmxPort = 1099;
	
	@Test
	public void testConnection()
	{
		
		try{ 
			JMX.connect(host, jmxPort);  
			fail("JMX user/pwd required");
		} 
		catch(JMXConnectionException  e) {}
		
		
		//pass with user/password
		try(JMX jmx = JMX.connect("localhost", 1099,adminUser,adminPassword))
		{
			Set<String> hosts = GemFireJmxClient.listLocatorHosts(jmx);
			
			assertTrue(hosts != null && !hosts.isEmpty());
			
			System.out.println("hosts:"+hosts);
		}
	}//------------------------------------------------
	@Test
	public void testDataUserCannotPerformClusterOperations()
	{
		//pass with user/password
				try(JMX jmx = JMX.connect(host, jmxPort,dataUser,dataUserPassword))
				{
					try{GemFireJmxClient.listLocatorHosts(jmx);
					fail();
					}
					catch(NotAuthorizedException e)
					{}
				}
	}//------------------------------------------------

}
