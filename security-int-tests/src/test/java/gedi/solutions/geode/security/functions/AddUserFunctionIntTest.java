package gedi.solutions.geode.security.functions;

import static org.junit.Assert.*;

import java.util.Collection;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionService;
import org.junit.BeforeClass;
import org.junit.Test;

import gedi.solutions.geode.client.GeodeClient;
import gedi.solutions.geode.functions.JvmExecution;
import gedi.solutions.geode.functions.JvmExecutionFactory;
import gedi.solutions.geode.functions.JvmRegionFunctionContext;
import gedi.solutions.geode.io.GemFireIO;
import gedi.solutions.geode.security.User;

public class AddUserFunctionIntTest
{
	
	private static GeodeClient geode;
	
	@BeforeClass
	public static void setUp()
	{
		geode = GeodeClient.connect();
	}

	@Test
	public void testExecute()
	throws Exception
	{	
		AddUserFunction func = new AddUserFunction();
		
		Region<String,User> userRegion = geode.getRegion("users");
		
		JvmExecutionFactory factory = new JvmExecutionFactory();
		Execution<?,?,?> exe = factory.onRegion(userRegion);
		
		Collection<String> userIds = GemFireIO.exeWithResults(exe, func);
		
		assertTrue(userIds  != null && !userIds.isEmpty());
	}

}
