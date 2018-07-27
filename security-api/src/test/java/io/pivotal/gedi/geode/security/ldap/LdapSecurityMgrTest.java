package io.pivotal.gedi.geode.security.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.junit.Test;

import io.pivotal.gedi.geode.security.exceptions.MissingSecurityProperty;
import nyla.solutions.core.ds.LDAP;
import nyla.solutions.core.ds.security.LdapSecurityGroup;
import nyla.solutions.core.ds.security.LdapSecurityUser;
import nyla.solutions.core.util.Config;
import nyla.solutions.core.util.Cryption;

public class LdapSecurityMgrTest
{

	static
	{
		Config.reLoad();
	}
	
	private static LDAP ldap;
	private static LDAPConnectionFactory ldapConnectionFactory ;
	
	public LdapSecurityMgr init()
	throws NamingException
	{
		LdapSecurityMgr mgr = new LdapSecurityMgr();
		
		ldap = mock(LDAP.class);
		

		ldapConnectionFactory = mock(LDAPConnectionFactory.class);
		
		when(ldapConnectionFactory.connect(any(), any(), any()))
		.thenReturn(ldap).thenReturn(ldap).thenReturn(ldap);
		
		
		mgr.setLdapConnectionFactory(ldapConnectionFactory);
		
		Properties props  = new Properties();
		props.setProperty("security-ldap-proxy-dn", "uid=admin");
		props.setProperty("security-ldap-proxy-password", "password");
		props.setProperty("security-ldap-server-url", "ldap://localhost:389");
		props.setProperty("security-ldap-group-attribute", "CN");
		props.setProperty("security-ldap-base-dn", "basedn");
		props.setProperty(LdapSecurityConstants.LDAP_MEMBEROF_ATTRIB_NM_PROP, "memberOf");
		
		props.setProperty("security-ldap-acl-user-admin", "ALL");
		props.setProperty("security-ldap-acl-user-guest", "NONE");
		props.setProperty("security-ldap-acl-user-readonly", "DATA:READ");
		props.setProperty("security-ldap-acl-group-readonly","DATA:READ");
		
		mgr.setup(props);
		
		System.out.println("setup");
		
		return mgr;
	}
	/**
	 * Testing the setup of the Ldap and making an initial connection
	 * @throws Exception when an unknown error occurs
	 */
	@Test
	public synchronized  void testSetup()
	throws Exception
	{
		LdapSecurityMgr mgr = init();
		LDAPConnectionFactory ldapConnectionFactory = mock(LDAPConnectionFactory.class);
		
		Properties props = new Properties();
		
		mgr.setLdapConnectionFactory(ldapConnectionFactory);
		
		try
		{
			mgr.setup(props);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
	
		props.setProperty("security-ldap-proxy-dn", "admin");

		try
		{
			mgr.setup(props);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
		
		props.setProperty("security-ldap-proxy-password", "password");

		try
		{
			mgr.setup(props);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
		
		props.setProperty("security-ldap-server", "ldap://localhost:389");
		try
		{
			mgr.setup(props);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
		
		
		props.setProperty(LdapSecurityConstants.LDAP_MEMBEROF_ATTRIB_NM_PROP, "memberOf");
		try
		{
			mgr.setup(props);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
		
		
		props.setProperty(LdapSecurityConstants.LDAP_SERVER_URL_PROP, "test");
		try
		{
			mgr.setup(props);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
		
		props.setProperty(LdapSecurityConstants.LDAP_GROUP_ATTRIB_NM_PROP, "test");
		try
		{
			mgr.setup(props);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
		
		props.setProperty("security-ldap-base-dn", "password");
		mgr.setup(props);
		

	}//------------------------------------------------
	@Test
	public void testSetupPassword() throws Exception
	{
		LdapSecurityMgr  mgr = new LdapSecurityMgr();
		try
		{
			mgr.setupProxyPassword(null);
			fail();
		}
		catch(MissingSecurityProperty e)
		{}
		

		Properties props = new Properties();
		
		String evnName = "SECURITY_LDAP_PROXY_PASSWORD";
		System.setProperty(evnName, "default");
		Config.reLoad();
		
		String password = mgr.setupProxyPassword(props);
		
		assertEquals("default", password);
		
		props.setProperty(LdapSecurityConstants.LDAP_PROXY_PASSWORD, "secret");
		
		password = mgr.setupProxyPassword(props);
		
		assertEquals("secret", password);
		
		Cryption cryption = new Cryption();
		
		String encryptedText = Cryption.CRYPTION_PREFIX+cryption.encryptText("secret");

		props.setProperty(LdapSecurityConstants.LDAP_PROXY_PASSWORD, encryptedText);
		
		password = mgr.setupProxyPassword(props);
		
		assertEquals("secret", password);
	}//------------------------------------------------
	@Test
	public synchronized void test_authenticate()
	throws Exception
	{
		LdapSecurityMgr mgr = init();
		
		synchronized (ldapConnectionFactory)
		{

			try
			{
				mgr.authenticate(null);
				fail();
			}
			catch(AuthenticationFailedException e)
			{}
			
			try
			{
				mgr.authenticate(new Properties());
				fail();
			}
			catch(AuthenticationFailedException e)
			{}
			
			Properties props = new Properties();
			props.setProperty("security-username", "admin");
			props.setProperty("security-password", "password");
			mgr.authenticate(props);
			
			
			props.setProperty("security-username", "admin");
			props.setProperty("security-password", "{cryption}cndnirPoK+LecJOcWhnXmg==");
			mgr.authenticate(props);
			
			
			
		}
		
	}//------------------------------------------------
	@Test
	public void test_decrypt()
	throws Exception
	{
		byte[] keyBytes = Arrays.copyOf("GEDI-GEODI".getBytes(StandardCharsets.UTF_8), 16);
		
		Cryption c = new Cryption(keyBytes,"AES");
		String k = "{cryption}A0tyyt5QHch/hIinaQeKmw==";
		assertTrue(Cryption.isEncrypted(k));
		k = k.substring(Cryption.CRYPTION_PREFIX.length());
		assertEquals("A0tyyt5QHch/hIinaQeKmw==",k);
	
		assertEquals("cluster", c.decryptText(k));
		
	}//------------------------------------------------
	@Test
	public void test_authorize_user()
	throws Exception
	{
		System.setProperty("SECURITY_LDAP_ACL_USER_JUNIT", "ALL");
		Config.reLoad();
		
		LdapSecurityMgr mgr = init();
		
		Cryption.interpret("test".toCharArray());
		
		synchronized (ldapConnectionFactory)
		{
			
			ResourcePermission permission  = new ResourcePermission("DATA", "READ");
			
			String dn ="uid=junit";
			LdapSecurityUser user = new LdapSecurityUser("junit",dn);
			
			assertTrue(mgr.authorize(user, permission));
			
			 dn = "uid=admin";
			user = new LdapSecurityUser("admin",dn);
			

			boolean bool = mgr.authorize(user, permission);
			assertTrue(bool);
			user = new LdapSecurityUser("unknown","uid=unknown");
			bool = mgr.authorize(user, permission);
			assertFalse(bool);
			
			 permission  = new ResourcePermission("DATA", "READ");
			 user = new LdapSecurityUser("readonly","uid=readonly");
			  bool = mgr.authorize(user, permission);
			  assertTrue(bool);
		}
		
	}//------------------------------------------------
	@Test
	public void test_authorize_group()
	throws Exception
	{
		LdapSecurityMgr mgr = init();
		
		synchronized (ldapConnectionFactory)
		{
			
			LdapSecurityUser user = new LdapSecurityUser("unknown","uid=unknown");
			user.addGroup(new LdapSecurityGroup("readonly", "uid=readonly"));
			ResourcePermission permission  = new ResourcePermission("DATA", "READ");
			boolean bool = mgr.authorize(user, permission);
			assertTrue(bool);
			user = new LdapSecurityUser("unknown","uid=unknown");
			bool = mgr.authorize(user, permission);
			assertFalse(bool);
			
			 permission  = new ResourcePermission("DATA", "READ");
			 user = new LdapSecurityUser("readonly","uid=readonly");
			  bool = mgr.authorize(user, permission);
			  assertTrue(bool);
		}
		
	}//------------------------------------------------

}
