package io.pivotal.gedi.geode.security;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.geode.LogWriter;
import org.apache.geode.cache.Region;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.ResourcePermission.Operation;
import org.apache.geode.security.ResourcePermission.Resource;
import org.junit.Test;

import io.pivotal.gedi.geode.security.ConfiguredUserCacheLoader;
import io.pivotal.gedi.geode.security.SecurityCryption;
import io.pivotal.gedi.geode.security.User;
import io.pivotal.gedi.geode.security.UserRegionService;
import io.pivotal.gedi.geode.security.UserSecurityManager;
import io.pivotal.gedi.geode.security.UserService;
import nyla.solutions.core.util.Cryption;

import static org.mockito.Mockito.*;

public class UserSecurityManagerTest
{
	@Test
	public void test_CanAuthorizeCluster()
	throws Exception
	{
		System.setProperty("SECURITY_ENCRYPTION_KEY","0123456789012345");
		User user = new User();
		user.setUserName("testUser");
		user.setEncryptedPassword(SecurityCryption.getInstance().encryptText("admin").getBytes(StandardCharsets.UTF_8));
		user.setPriviledges(Collections.singleton("ALL"));
		
		String password = SecurityCryption.getInstance().encryptText("password");

		System.setProperty("gemfire.security-users.testUser", Cryption.CRYPTION_PREFIX+password+",ALL,[priviledge],[,priviledge]");
		
		UserService userService = new ConfiguredUserCacheLoader();
		UserSecurityManager mgr = new UserSecurityManager(userService);
		
		LogWriter logWriter = mock(LogWriter.class);
		
		mgr.setLogger(logWriter);
		
		
		ResourcePermission none = new ResourcePermission();
		
		assertTrue(mgr.authorize(user, none));
		
		user.setPriviledges(Collections.singleton("NULL"));
		ResourcePermission clusterManager = new ResourcePermission(Resource.CLUSTER,Operation.MANAGE);
		assertTrue(!mgr.authorize(user, clusterManager));
		
		
		user.setPriviledges(Collections.singleton("CLUSTER"));
		assertTrue(mgr.authorize(user, clusterManager));
		
		user.setPriviledges(Collections.singleton("CLUSTER:MANAGE"));
		assertTrue(mgr.authorize(user, clusterManager));
		
		user.setPriviledges(Collections.singleton("CLUSTER:READ"));
		assertTrue(!mgr.authorize(user, clusterManager));
		
		
		user.setPriviledges(Collections.singleton("CLUSTER:READ"));
		assertTrue(!mgr.authorize(user, clusterManager));
		
		
		user.setPriviledges(Arrays.asList("CLUSTER:READ","CLUSTER:MANAGE"));
		assertTrue(mgr.authorize(user, clusterManager));
		
		
		
		ResourcePermission clusterRead  = new ResourcePermission(Resource.CLUSTER,Operation.READ);
		
		user.setPriviledges(Arrays.asList("CLUSTER:MANAGE"));
		assertTrue(!mgr.authorize(user, clusterRead));
		
		
		user.setPriviledges(Arrays.asList("CLUSTER:READ"));
		assertTrue(mgr.authorize(user, clusterRead));
	}//------------------------------------------------
	

	@Test
	public void test_CanAuthorizeData()
	throws Exception
	{
		System.setProperty("SECURITY_ENCRYPTION_KEY","0123456789012345");
		User user = new User();
		user.setUserName("testUser");
		user.setEncryptedPassword(SecurityCryption.getInstance().encryptText("admin").getBytes(StandardCharsets.UTF_8));
		user.setPriviledges(Collections.singleton("ALL"));
		
		String password = SecurityCryption.getInstance().encryptText("password");

		System.setProperty("gemfire.security-users.testUser", Cryption.CRYPTION_PREFIX+password+",ALL,[priviledge],[,priviledge]");
		
		UserService userService = new ConfiguredUserCacheLoader();
		UserSecurityManager mgr = new UserSecurityManager(userService);
		
		LogWriter logWriter = mock(LogWriter.class);
		mgr.setLogger(logWriter);
		

		ResourcePermission dataManager = new ResourcePermission(Resource.DATA,Operation.MANAGE);
		assertTrue(mgr.authorize(user, dataManager));
		
		user.setPriviledges(Collections.singleton("DATA"));
		assertTrue(mgr.authorize(user, dataManager));
		
		user.setPriviledges(Collections.singleton("DATA:MANAGE"));
		assertTrue(mgr.authorize(user, dataManager));
		
		user.setPriviledges(Collections.singleton("DATA:READ"));
		assertTrue(!mgr.authorize(user, dataManager));
		
		
		user.setPriviledges(Arrays.asList("DATA:READ","DATA:MANAGE"));
		assertTrue(mgr.authorize(user, dataManager));
		
		
		ResourcePermission clusterRead  = new ResourcePermission(Resource.DATA,Operation.READ);
		
		user.setPriviledges(Arrays.asList("DATA:MANAGE"));
		assertTrue(!mgr.authorize(user, clusterRead));
		
		
		user.setPriviledges(Arrays.asList("DATA:READ"));
		assertTrue(mgr.authorize(user, clusterRead));
	}//------------------------------------------------

	@SuppressWarnings("unchecked")
	@Test
	public void test_UserCanAuthenticate()
	throws Exception
	{
		System.setProperty("SECURITY_ENCRYPTION_KEY","0123456789012345");
		
		User user = new User();
		user.setUserName("admin");
		user.setEncryptedPassword(SecurityCryption.getInstance().encryptText("admin").getBytes(StandardCharsets.UTF_8));
		
		Region<String, User> region = mock(Region.class);
		when(region.get("admin")).thenReturn(user);
		
		UserService userService = new UserRegionService(region);
		UserSecurityManager mgr = new UserSecurityManager(userService);
		
		try
		{ 
			mgr.authenticate(null);
			fail();
		}
		catch(AuthenticationFailedException e)
		{
		}
		
		Properties credentails = new Properties();
		
		try
		{ 
			mgr.authenticate(credentails);
			fail();
		}
		catch(AuthenticationFailedException e)
		{
		}
		
		 credentails.setProperty("security-username", "invalid");
		 
			try
			{ 
				mgr.authenticate(credentails);
				fail();
			}
			catch(AuthenticationFailedException e)
			{
			}
			
			
			credentails.setProperty("security-username", "admin");
			
			try
			{ 
				mgr.authenticate(credentails);
				fail();
			}
			catch(AuthenticationFailedException e)
			{
			}
			
			
			
			credentails.setProperty("security-password", SecurityCryption.getInstance().encryptText("invalid"));
			
			try
			{ 
				mgr.authenticate(credentails);
				fail();
			}
			catch(AuthenticationFailedException e)
			{
			}
			
			credentails.setProperty("security-password", SecurityCryption.getInstance().encryptText("admin"));
			
			Object principal = mgr.authenticate(credentails);
			
			assertNotNull(principal);
			
			
			credentails.setProperty("security-password", Cryption.CRYPTION_PREFIX+SecurityCryption.getInstance().encryptText("admin"));
			
			principal = mgr.authenticate(credentails);
			
			assertNotNull(principal);
			
			credentails.setProperty("security-password", " "+Cryption.CRYPTION_PREFIX+SecurityCryption.getInstance().encryptText("admin"));
			
			principal = mgr.authenticate(credentails);
			
			assertNotNull(principal);
			
			
			credentails.setProperty("security-password", "invalid");
			
			try
			{ 
				mgr.authenticate(credentails);
				fail();
			}
			catch(AuthenticationFailedException e)
			{
				
			}
			credentails.setProperty("security-password", "admin");
			
			principal = mgr.authenticate(credentails);
			
			assertNotNull(principal);
	}

}
