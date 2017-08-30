package gedi.solutions.geode.security.functions;

import static org.junit.Assert.*;

import java.util.Collection;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Execution;
import org.junit.BeforeClass;
import org.junit.Test;

import gedi.solutions.geode.client.GeodeClient;
import gedi.solutions.geode.functions.JvmExecutionFactory;
import gedi.solutions.geode.io.GemFireIO;
import io.pivotal.gedi.geode.security.GediGeodeSecurityConstants;
import io.pivotal.gedi.geode.security.User;
import io.pivotal.gedi.geode.security.functions.AddUserFunction;
import nyla.solutions.core.util.Config;

public class AddUserFunctionIntTest
{
	
	static final String user = Config.getProperty(GediGeodeSecurityConstants.SECURITY_USERNAME_PROP,"admin");
	static final char[] password = Config.getProperty(GediGeodeSecurityConstants.SECURITY_USERNAME_PROP,"admin").toCharArray();
	
	private static GeodeClient geode;
	
	@BeforeClass
	public static void setUp()
	{
		
		System.out.println("user:"+user);
		
		System.setProperty(GediGeodeSecurityConstants.SECURITY_USERNAME_PROP, user);
		System.setProperty(GediGeodeSecurityConstants.SECURITY_PASSWORD_PROP, String.valueOf(password));
		
		geode = GeodeClient.connect();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecute()
	throws Exception
	{	
		AddUserFunction func = new AddUserFunction();
		
		Region<String,User> userRegion = geode.getRegion("users");
		
		String [] args = {"appUser","password","ALL" };
		JvmExecutionFactory factory = new JvmExecutionFactory();
		Execution exe = factory.onRegion(userRegion).withArgs(args);
		
		Collection<String> userIds = GemFireIO.exeWithResults(exe, func);
		
		assertTrue(userIds  != null && !userIds.isEmpty());
	}

}
