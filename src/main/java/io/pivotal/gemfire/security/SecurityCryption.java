package io.pivotal.gemfire.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import nyla.solutions.core.exception.SetupException;
import nyla.solutions.core.util.Config;
import nyla.solutions.core.util.Cryption;

public class SecurityCryption extends Cryption
{
	/**
	 * SECURITY_ENCRYPTION_KEY_PROP = "SECURITY_ENCRYPTION_KEY"
	 */
	public static final String SECURITY_ENCRYPTION_KEY_PROP = "SECURITY_ENCRYPTION_KEY";
	
	private SecurityCryption(byte[] bytes)
	{
		super(bytes,
		Cryption.DEFAULT_ALGORITHM);
	}//------------------------------------------------
	
	public static Cryption getInstance()
	{
		synchronized (SecurityCryption.class)
		{
			try
			{
				String keyText = Config.getProperty(SECURITY_ENCRYPTION_KEY_PROP);
				
				
				byte[] bytes = Arrays.copyOf(keyText.getBytes(StandardCharsets.UTF_8), 16);
				
			
				if(cryption == null)
					cryption = new SecurityCryption(bytes);
			}
			catch (IllegalArgumentException e)
			{
				throw new SetupException("Assert that system or JVM property \""+
				SECURITY_ENCRYPTION_KEY_PROP+"\" is a mininum of 8 bytes");
				
			}
		}
		return cryption;
	}//------------------------------------------------
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			System.out.println("Usage java "+SecurityCryption.class.getName()+" password");
			System.exit(-1);
		}
		try
		{
			String encrypted = SecurityCryption.getInstance().encryptText(args[0]);
			
			System.out.println(Cryption.CRYPTION_PREFIX+encrypted);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private static Cryption cryption = null;
}
