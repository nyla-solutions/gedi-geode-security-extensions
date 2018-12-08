package io.pivotal.gedi.geode.security;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTest
{

	@Test
	public void testToStringDoesNotHavePassword()
	{
		User user = new User();
		
		byte[] passwords = {1,23,23};
		
		user.setEncryptedPassword(passwords);
		
		assertTrue(!user.toString().contains("encryptedPassword"));
	}

}
