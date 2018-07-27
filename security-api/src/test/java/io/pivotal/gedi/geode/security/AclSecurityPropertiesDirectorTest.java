package io.pivotal.gedi.geode.security;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import nyla.solutions.core.util.Config;

public class AclSecurityPropertiesDirectorTest
{

	static boolean builtUser= false;
	static boolean builtGroups= false;
	
	@Test
	public void testConstruct()
	{
		String group_prefix = "security-groups";
		String user_prefix = "security-users-";
		Properties props = new Properties();
		
		String systemUserPropertName = Config.sanitizeEnvVarNAme(user_prefix)+"JUNIT";
		String systemGroupPropertName = Config.sanitizeEnvVarNAme(group_prefix)+"JUNITGroups";
		
		System.setProperty(systemUserPropertName, "ALL");
		System.setProperty(systemGroupPropertName, "ALL");
		Config.reLoad();
		
		assertNotNull(Config.getProperties().get(systemUserPropertName));
		
		SecurityAclBuilder builder = new SecurityAclBuilder()
		{
			@Override
			public void buildUserPermission(String user, String permission)
			{
				builtUser = true; 
				
			}
			@Override
			public void buildGroupPermission(String group, String permission)
			{
				builtGroups = true;
			}
		};
		
		AclSecurityPropertiesDirector d = new AclSecurityPropertiesDirector
		(props, group_prefix, user_prefix);
		
		d.construct(builder);
		
		assertTrue(builtUser);
		assertTrue(builtGroups);
		
	}
	
	

}
