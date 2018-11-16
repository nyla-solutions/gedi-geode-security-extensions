package io.pivotal.gedi.geode.security;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import org.junit.Test;

import io.pivotal.gedi.geode.security.SettingsUserService;
import io.pivotal.gedi.geode.security.User;
import nyla.solutions.core.util.Config;
import nyla.solutions.core.util.Cryption;
import nyla.solutions.core.util.settings.ConfigSettings;
import nyla.solutions.core.util.settings.Settings;


public class SettingsUserServiceTest
{

	@Test
	public void testCreate_HasUsers()
	throws Exception
	{
		Settings settings = mock(Settings.class);
		
		Properties properties = new Properties();
		
		when(settings.getProperties()).thenReturn(properties);
		
		String encryptedPassword = new Cryption().encryptText("password");
		
		String nylaProperty = Cryption.CRYPTION_PREFIX+encryptedPassword+",admin,read, write ";
		
		properties.setProperty("security-users.nyla", nylaProperty);
		
		SettingsUserService cacheLoader = new SettingsUserService(settings);
		
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
		Settings settings = mock(Settings.class);
		
		Properties properties = new Properties();

		when(settings.getProperties()).thenReturn(properties);
		
		String encryptedPassword = new Cryption().encryptText("password");
		
		String nylaProperty = Cryption.CRYPTION_PREFIX+encryptedPassword+",admin,read, write ";
		
		properties.setProperty("security-users.nyla", nylaProperty);
		
		SettingsUserService cacheLoader = new SettingsUserService(settings);
		
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
		Settings settings = mock(Settings.class);
		
		Properties properties = new Properties();

		when(settings.getProperties()).thenReturn(properties);
		
		String encryptedPassword = new Cryption().encryptText("password");
		
		String nylaProperty = Cryption.CRYPTION_PREFIX+encryptedPassword+",admin,read, write ";
		
		properties.setProperty("gemfire.security-users.nyla", nylaProperty);
		
		SettingsUserService cacheLoader = new SettingsUserService(settings);
		
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
		
		
	}//------------------------------------------------
	
	@Test
	public void test_new_user_in_property_file_authenticated() throws Exception
	{
		Properties props = new Properties();
		Cryption cryption = new Cryption();
		props.setProperty("gemfire.security-users.nyla", "{cryption}"+cryption.encryptText("nyla"));
		props.setProperty("CRYPTION_KEY","PIVOTAL");
		
		File file = Paths.get("./target/junit_users.properties").toFile();
		props.store(new FileWriter(file), null);
		
		System.setProperty(Config.SYS_PROPERTY, file.getAbsolutePath());
		ConfigSettings settings = new ConfigSettings();
		
		
		SettingsUserService configedUser = new SettingsUserService(settings);
		assertNotNull(configedUser.findUser("nyla"));
		assertNull(configedUser.findUser("imani"));
		
		props.setProperty("gemfire.security-users.imani", "{cryption}"+cryption.encryptText("imani"));
		props.store(new FileWriter(file), null);
		settings.reLoad();
		Thread.sleep(5000);
		
		assertNotNull(configedUser.findUser("imani"));
		
	}
	
	@Test
	public void test_new_user_in_property_file_privledges() throws Exception
	{
		Properties props = new Properties();
		Cryption cryption = new Cryption();
		props.setProperty("gemfire.security-users.nyla", "{cryption}"+cryption.encryptText("nyla")+",DATA:READ");
		props.setProperty("CRYPTION_KEY","PIVOTAL");
		
		File file = Paths.get("./target/test_new_user_in_property_file_privledges.properties").toFile();
		props.store(new FileWriter(file), null);
		
		System.setProperty(Config.SYS_PROPERTY, file.getAbsolutePath());
		ConfigSettings settings = new ConfigSettings();
		
		
		SettingsUserService configedUser = new SettingsUserService(settings);
		assertTrue(configedUser.findUser("nyla").getPriviledges().contains("DATA:READ"));
		
		
		props.setProperty("gemfire.security-users.nyla", "{cryption}"+cryption.encryptText("nyla")+",DATA:READ,CLUSTER");
		props.store(new FileWriter(file), null);
		settings.reLoad();
		
		Thread.sleep(3000);
		
		User nyla = configedUser.findUser("nyla");
		assertTrue(nyla.getPriviledges().contains("DATA:READ"));
		assertTrue(nyla.getPriviledges().contains("CLUSTER"));
		
	}
}
