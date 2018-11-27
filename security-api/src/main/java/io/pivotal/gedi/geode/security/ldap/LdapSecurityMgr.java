package io.pivotal.gedi.geode.security.ldap;

import java.security.Principal;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.NotAuthorizedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pivotal.gedi.geode.security.AclSecurityPropertiesDirector;
import io.pivotal.gedi.geode.security.exceptions.MissingSecurityProperty;
import nyla.solutions.core.ds.LDAP;
import nyla.solutions.core.security.data.SecurityAcl;
import nyla.solutions.core.util.Config;
import nyla.solutions.core.util.Cryption;
import nyla.solutions.core.util.Debugger;

/**
 * Security Manager for LDAP based authentication and authorization.
 * 
 * @author Gregory Green
 *
 */
public class LdapSecurityMgr implements org.apache.geode.security.SecurityManager
{

	/**
	 * CRYPTION_KEY_PROP = Cryption.getCryptionKey()
	 */
	public static String CRYPTION_KEY = Cryption.getCryptionKey();
	
	public static org.apache.geode.security.SecurityManager create()
	{
		return new LdapSecurityMgr();
	}//------------------------------------------------

	public LdapSecurityMgr()
	{
		this.securityLogger = null;

	}//------------------------------------------------

	/**
	 * @param principal the principal to authorize
	 * @param context the permission to authorize
	 */
	@Override
	public boolean authorize(Object principal, ResourcePermission context)
	{

		try
		{
			if (principal == null)
			{
				securityLogger.warn("Not authorized SecurityManager principal is null for context" + context);
				return false;
			}

			if (context == null)
			{
				securityLogger.warn("Not authorized SecurityManager context is null for principal" + principal);
				return true;
			}

			boolean authorized = acl.checkPermission((Principal) principal, toString(context));

			securityLogger
			.debug("principal:" + principal + " context:" + context + " authorized:" + authorized + " acl:" + acl);

			return authorized;

		}
		catch (AuthenticationFailedException e)
		{
			this.securityLogger.warn(e);
			
			throw e;
		}
		catch (RuntimeException e)
		{

			this.securityLogger.warn(e);

			throw new AuthenticationFailedException(e.getMessage() + " STACK:" + Debugger.stackTrace(e));

		}

	}// --------------------------------------------------------------

	@Override
	public void init(final Properties securityProps)
	throws NotAuthorizedException
	{
		setup(securityProps);
	}// --------------------------------------------------------------

	String toString(ResourcePermission resourcePermission)
	{
		return String.valueOf(resourcePermission);

	}//------------------------------------------------
	/**
	 * Set up the security manager
	 * @param securityProps the security properties
	 * @throws MissingSecurityProperty when a required property does not exist
	 * 
	 */
	protected void setup(Properties securityProps)
	throws MissingSecurityProperty
	{
		securityLogger = LogManager.getLogger(getClass());
		
		this.serviceAccountDn = Config.getPropertyEnv(LdapSecurityConstants.LDAP_PROXY_DN,securityProps);

		securityLogger.debug(LdapSecurityConstants.LDAP_PROXY_DN + " *************" + serviceAccountDn);

		if (serviceAccountDn == null || serviceAccountDn.length() == 0)
		{
			throw new MissingSecurityProperty(LdapSecurityConstants.LDAP_PROXY_DN);
		}

		setupProxyPassword(securityProps);

		this.ldapUrl = Config.getPropertyEnv(LdapSecurityConstants.LDAP_SERVER_URL_PROP,securityProps);
		if (this.ldapUrl == null || this.ldapUrl.length() == 0)
			throw new MissingSecurityProperty(LdapSecurityConstants.LDAP_SERVER_URL_PROP);
			
			
		this.basedn = Config.getPropertyEnv(LdapSecurityConstants.LDAP_BASEDN_NAME_PROP,securityProps);
		if (this.basedn == null || this.basedn.length() == 0)
			throw new MissingSecurityProperty(LdapSecurityConstants.LDAP_BASEDN_NAME_PROP);
		
		this.memberOfAttrNm = Config.getPropertyEnv(LdapSecurityConstants.LDAP_MEMBEROF_ATTRIB_NM_PROP,securityProps);
		if (this.memberOfAttrNm == null || this.memberOfAttrNm.length() == 0)
			throw new MissingSecurityProperty(LdapSecurityConstants.LDAP_MEMBEROF_ATTRIB_NM_PROP);
		
		
		this.groupAttrNm = Config.getPropertyEnv(LdapSecurityConstants.LDAP_GROUP_ATTRIB_NM_PROP,securityProps);
		if (this.groupAttrNm == null || this.groupAttrNm.length() == 0)
			throw new MissingSecurityProperty(LdapSecurityConstants.LDAP_GROUP_ATTRIB_NM_PROP);
		
		this.uidAttribute =Config.getPropertyEnv(LdapSecurityConstants.LDAP_UID_ATTRIBUTE,securityProps);
		if (this.uidAttribute == null || this.uidAttribute.length() == 0)
		{
			this.uidAttribute = "uid";
		}

		// check to LDAP settings
		try (LDAP ldap = this.ldapConnectionFactory.connect(ldapUrl, serviceAccountDn, proxyPassword.toCharArray()))
		{
		}
		catch (NamingException e)
		{
			securityLogger.warn(e);
			throw new AuthenticationFailedException(e.getMessage(), e);
		}

		AclSecurityPropertiesDirector director =  new AclSecurityPropertiesDirector(securityProps,
		LdapSecurityConstants.LDAP_ACL_GROUP_PREFIX,
		LdapSecurityConstants.LDAP_ACL_USER_PREFIX);
		
		LdapAclBuilder builder = new LdapAclBuilder();
		director.construct(builder);

		this.acl = builder.getAcl();

	}//------------------------------------------------
	/**
	 * 
	 * @param securityProps the properties containing the password
	 */
	String setupProxyPassword(Properties securityProps)
	{
		
		this.proxyPassword = Config.getPropertyEnv(LdapSecurityConstants.LDAP_PROXY_PASSWORD,securityProps);
		if (proxyPassword == null || proxyPassword.length() == 0)
			throw new MissingSecurityProperty(LdapSecurityConstants.LDAP_PROXY_PASSWORD);
		
		this.proxyPassword = Cryption.interpret(proxyPassword);
		
		return this.proxyPassword;
		
	}//------------------------------------------------
	@Override
	public Object authenticate(final Properties props)
	throws AuthenticationFailedException
	{
		if(props == null)
			throw new AuthenticationFailedException(
			"Authentication securities properties not provided");

		String userName = props.getProperty(LdapSecurityConstants.USER_NAME_PROP);

		if (userName == null)
		{
			throw new AuthenticationFailedException(
			"property ["+ LdapSecurityConstants.USER_NAME_PROP + "] not provided");
		}

		String passwd = props.getProperty(LdapSecurityConstants.PASSWORD_PROP);

		if (passwd == null || passwd.length() == 0)
		{
			throw new AuthenticationFailedException(
			"property ["+ LdapSecurityConstants.PASSWORD_PROP + "] not provided");
		}

		
		try (LDAP ldap = this.ldapConnectionFactory.connect(this.ldapUrl, this.serviceAccountDn,
		this.proxyPassword.toCharArray()))
		{		
			try
			{
				passwd = Cryption.interpret(passwd);
			}
			catch(Exception e)
			{
				securityLogger.warn("Detected password interpration error. This may be caused by an incorrect password, but you should check that the CRYPTION_KEY environment variable is a minimum of 16 characters, then regenerate any needed passwords.");
				throw new AuthenticationFailedException(e.getMessage());
			
			}
		
			if (ldap == null)
				throw new IllegalArgumentException("ldap is required from factory: "+ldapConnectionFactory.getClass().getName());
			
			Object principal = ldap.authenicate(userName, passwd.toCharArray(), this.basedn, uidAttribute, memberOfAttrNm,
			groupAttrNm, timeout);
			
			securityLogger.debug("AUTHENTICATED:"+principal);
			
			return principal;
		}
		catch(AuthenticationFailedException e)
		{
			securityLogger.warn(e);
			throw e;
		}
		catch (NamingException |RuntimeException e)
		{
			securityLogger.warn(e);
			throw new AuthenticationFailedException(e.getMessage());
		}

	}// --------------------------------------------------------------

	/**
	 * 
	 * @param ldapConnectionFactory
	 *            the ldapConnectionFactory to set
	 * 
	 */
	void setLdapConnectionFactory(LDAPConnectionFactory ldapConnectionFactory)
	{
		this.ldapConnectionFactory = ldapConnectionFactory;
	}
	

	
	private String proxyPassword = null;	
	private SecurityAcl acl = null;
	private String basedn = null;
	private String uidAttribute = null;
	private String memberOfAttrNm = "memberOf"; 
	private String groupAttrNm  = null; //Ex: "CN";
	protected Logger securityLogger;
	private String serviceAccountDn = null;
	private int timeout = Config.getPropertyInteger("LDAP_TIMEOUT", 10).intValue();
	private LDAPConnectionFactory ldapConnectionFactory = new LDAPConnectionFactory();
	private String ldapUrl;

}
