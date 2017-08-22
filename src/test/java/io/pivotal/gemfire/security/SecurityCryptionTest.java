package io.pivotal.gemfire.security;

import static org.junit.Assert.*;

import org.junit.Test;

import nyla.solutions.core.exception.SetupException;
import nyla.solutions.core.util.Cryption;

public class SecurityCryptionTest
{

	@Test
	public void testGetInstance()
	throws Exception
	{
		try
		{
			//System.setProperty(SecurityCryption.SECURITY_ENCRYPTION_KEY_PROP, "11121");
			//SecurityCryption.getInstance();
		}
		catch (SetupException e)
		{
		}
		
			System.setProperty(SecurityCryption.SECURITY_ENCRYPTION_KEY_PROP, "131");
			Cryption cryption = SecurityCryption.getInstance();
			
			assertEquals(cryption.encryptText("test"), cryption.encryptText("test"));
	
		
	}

}
