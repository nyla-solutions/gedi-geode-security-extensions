package io.pivotal.gemfire.security;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import org.junit.Test;
import nyla.solutions.core.util.Cryption;


public class ConfiguredUserCacheLoaderTest
{

	@Test
	public void testCreate_HasUsers()
	throws Exception
	{
		System.setProperty(SecurityCryption.SECURITY_ENCRYPTION_KEY_PROP, "03232");
		Properties properties = new Properties();
		
		String encryptedPassword = SecurityCryption.getInstance().encryptText("password");
		
		String nylaProperty = Cryption.CRYPTION_PREFIX+encryptedPassword+",admin,read, write ";
		
		properties.setProperty("security-users.nyla", nylaProperty);
		
		ConfiguredUserCacheLoader cacheLoader = new ConfiguredUserCacheLoader(properties);
		
		assertNotNull(cacheLoader);
		User user = cacheLoader.findUser("invalid");
		
		assertNull(user);
		
		user = cacheLoader.findUser("nyla");
		
		assertNotNull(user);
		assertEquals(user.getUserName(),"nyla");
		
		assertTrue(Arrays.equals(user.getEncryptedPassword(), encryptedPassword.getBytes(StandardCharsets.UTF_8)));
		
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("admin")); 
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("read"));
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("write"));
		
		
	}
	
	@Test
	public void testCreate_UnEncryptedPAsswodHasUsers()
	throws Exception
	{
		System.setProperty(SecurityCryption.SECURITY_ENCRYPTION_KEY_PROP, "03232");
		Properties properties = new Properties();
		
		String encryptedPassword = SecurityCryption.getInstance().encryptText("password");
		
		String nylaProperty = Cryption.CRYPTION_PREFIX+encryptedPassword+",admin,read, write ";
		
		properties.setProperty("security-users.nyla", nylaProperty);
		
		ConfiguredUserCacheLoader cacheLoader = new ConfiguredUserCacheLoader(properties);
		
		assertNotNull(cacheLoader);
		User user = cacheLoader.findUser("invalid");
		
		assertNull(user);
		
		user = cacheLoader.findUser("nyla");
		
		assertNotNull(user);
		assertEquals(user.getUserName(),"nyla");
		
		assertTrue(Arrays.equals(user.getEncryptedPassword(), encryptedPassword.getBytes(StandardCharsets.UTF_8)));
		
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("admin")); 
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("read"));
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("write"));
		
		
	}
	@Test
	public void testGemFirePropertiesCreate_HasUsers()
	throws Exception
	{
		System.setProperty(SecurityCryption.SECURITY_ENCRYPTION_KEY_PROP, "03232");
		Properties properties = new Properties();
		
		String encryptedPassword = SecurityCryption.getInstance().encryptText("password");
		
		String nylaProperty = Cryption.CRYPTION_PREFIX+encryptedPassword+",admin,read, write ";
		
		properties.setProperty("gemfire.security-users.nyla", nylaProperty);
		
		ConfiguredUserCacheLoader cacheLoader = new ConfiguredUserCacheLoader(properties);
		
		assertNotNull(cacheLoader);
		User user = cacheLoader.findUser("invalid");
		
		assertNull(user);
		
		user = cacheLoader.findUser("nyla");
		
		assertNotNull(user);
		assertEquals(user.getUserName(),"nyla");
		
		assertTrue(Arrays.equals(user.getEncryptedPassword(), encryptedPassword.getBytes(StandardCharsets.UTF_8)));
		
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("admin")); 
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("read"));
		assertTrue(user.getPriviledges() != null && user.getPriviledges().contains("write"));
		
		
	}
}
