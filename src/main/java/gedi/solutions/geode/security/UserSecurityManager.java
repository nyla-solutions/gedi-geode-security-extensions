package gedi.solutions.geode.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;

import org.apache.geode.LogWriter;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.SecurityManager;

import nyla.solutions.core.util.Cryption;


/**
 * @author Gregory Green
 *
 */
public class UserSecurityManager implements SecurityManager
{
	
	public static final String USERNAME_PROP = "security-username";
	public static final String PASSWORD_PROP = "security-password";
	
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
			userName = credentials.getProperty(USERNAME_PROP);	
			if (userName == null){
				throw new AuthenticationFailedException(USERNAME_PROP+" required");
			}
			
			String password = credentials.getProperty(PASSWORD_PROP);
			
			
			if (password == null)
				throw new AuthenticationFailedException(PASSWORD_PROP+" required");
			
			User user = this.userService.findUser(userName);
			
			if(user == null)
				throw new AuthenticationFailedException("user or password not found");
	
			
			Cryption cryption = SecurityCryption.getInstance();
			
			byte[] userEncryptedPasswordBytes = user.getEncryptedPassword();
			
			if(userEncryptedPasswordBytes == null)
				throw new AuthenticationFailedException("user or password not found");
			
			String userEncryptedPassword =  new String(userEncryptedPasswordBytes,StandardCharsets.UTF_8);
			try
			{
				
				//compare password
				String storedUnEncrypted = cryption.decryptText(userEncryptedPassword);
				
				
				//test without encrypt
				if(storedUnEncrypted.equals(password))
					return user;
				
				int indexOfCryption = password.indexOf(Cryption.CRYPTION_PREFIX);
				if(indexOfCryption > -1)
					password = password.substring(indexOfCryption+Cryption.CRYPTION_PREFIX.length());
				
				String unencryptedPassword = cryption.decryptText(password);
				
				
				
				if(!unencryptedPassword.equals(storedUnEncrypted))
					throw new AuthenticationFailedException("user or password not found");
			}
			catch (NumberFormatException e)
			{
				throw new AuthenticationFailedException("password or user not found");
			}
			catch (Exception e)
			{
				throw new AuthenticationFailedException(e.getMessage(),e);
			}
			
			return user;
			
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
        	getLogger().warning("user:"+user.getUserName()+" DOES NOT HAVE permission:"+textPermission);
    	}
    	
    	return hasPermission;
    }//------------------------------------------------
    private LogWriter getLogger()
    {
    	if(logWriter == null)
    		logWriter = CacheFactory.getAnyInstance().getSecurityLogger();
    	
    	return logWriter;
    }//------------------------------------------------
    protected void setLogger(LogWriter logger)
    {
    	this.logWriter = logger;
    }//------------------------------------------------
    private LogWriter logWriter = null;
    private final UserService userService;

}
