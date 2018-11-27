package io.pivotal.gedi.geode.security.ldap;

import javax.naming.NamingException;
import nyla.solutions.core.ds.LDAP;

/**
 * Creates a LDAP object
 * @author Gregory Green
 *
 */
public class LDAPConnectionFactory
{

	public LDAP connect(String ldapUrl, String serviceAccountDn, char[] charArray)
	throws NamingException
	{
		return new LDAP(ldapUrl,serviceAccountDn,charArray);
	}

}
