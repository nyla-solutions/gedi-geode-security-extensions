package io.pivotal.gedi.geode.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;

import javax.transaction.SystemException;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.SecurityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nyla.solutions.core.util.Cryption;
import nyla.solutions.core.util.Debugger;


/**
 * @author Gregory Green
 *
 */
public class UserSecurityManager implements SecurityManager
{
	
	public UserSecurityManager()
	{
		this(new ConfiguredUserCacheLoader());
	}
	
	public UserSecurityManager(UserService userService)
	{		
		this.userService = userService;
	}

	@Override
	public Object authenticate(Properties credentials) throws AuthenticationFailedException
	{
		if(credentials == null )
			throw new AuthenticationFailedException("null properties, properties required");
			
		String userName  = null;
			userName = credentials.getProperty(SecurityConstants.USERNAME_PROP);	
			if (userName == null || userName.length() == 0){
				throw new AuthenticationFailedException(SecurityConstants.USERNAME_PROP+" required");
			}
			
			String password = credentials.getProperty(SecurityConstants.PASSWORD_PROP);
			
			if (password == null)
				throw new AuthenticationFailedException(SecurityConstants.PASSWORD_PROP+" required");
			
			password = password.trim();
			
			User user = this.userService.findUser(userName);
			
			if(user == null)
				throw new AuthenticationFailedException("user \""+userName+"\" not found");
	
			
			byte[] userEncryptedPasswordBytes = user.getEncryptedPassword();
			
			if(userEncryptedPasswordBytes == null || userEncryptedPasswordBytes.length == 0)
				throw new AuthenticationFailedException("password is required");
			
			Cryption cryption = new Cryption();
			String userEncryptedPassword =  new String(userEncryptedPasswordBytes,StandardCharsets.UTF_8);
			try
			{
				
				String storedUnEncrypted = null;
				
				//compare password
				storedUnEncrypted = cryption.decryptText(userEncryptedPassword);
				
				
				//test without encrypt
				if(storedUnEncrypted.equals(password))
					return user;
					
				String unencryptedPassword = Cryption.interpret(password);
				
				if(unencryptedPassword.equals(storedUnEncrypted))
					return user;
				
				try
				{
						unencryptedPassword = cryption.decryptText(unencryptedPassword);
				}
				catch(IllegalArgumentException e)
				{
				}
				
				if(unencryptedPassword.equals(storedUnEncrypted))
					return user;
				
				throw new AuthenticationFailedException("Password user or password not found");
			}
			catch(AuthenticationFailedException e)
			{
				throw e;
			}
			catch (SystemException e)
			{
				throw new AuthenticationFailedException(e.getMessage(),e);
			}
			catch (Exception e)
			{
				throw new AuthenticationFailedException(e.getMessage(),e);
			}
			
			
	}//------------------------------------------------
    public boolean authorize(Object principal, ResourcePermission permission)
    {
    	if(principal == null)
    		return false;
    	
    	if(!User.class.isAssignableFrom(principal.getClass()))
    		return false;
    	User user = (User)principal;
    	

    	
    	//this MUST BE FAST!!!
    	
    	Collection<String> priviledges = user.getPriviledges();
    	
    	if(priviledges == null || priviledges.isEmpty())
    		return false;
    	
    	String textPermission = permission.toString();
    	
    	boolean hasPermission =  priviledges.parallelStream().anyMatch(p -> p.equals("ALL") || textPermission.contains(p));
    	
    	if(!hasPermission)
    	{
        	getLogger().warn("user:"+user.getUserName()+" DOES NOT HAVE permission:"+textPermission);
    	}
    	
    	return hasPermission;
    }//------------------------------------------------
    private Logger getLogger()
    {
    	if(logWriter == null)
    		logWriter = LogManager.getLogger(UserSecurityManager.class);
    	
    	return logWriter;
    }//------------------------------------------------
    protected void setLogger(Logger logger)
    {
    	this.logWriter = logger;
    }//------------------------------------------------
    private Logger logWriter = null;
    private final UserService userService;

}
