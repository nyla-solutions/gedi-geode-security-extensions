package io.pivotal.gedi.geode.security;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;

import org.apache.geode.cache.LoaderHelper;

import nyla.solutions.core.exception.SetupException;
import nyla.solutions.core.patterns.observer.SubjectObserver;
import nyla.solutions.core.util.Config;
import nyla.solutions.core.util.Cryption;
import nyla.solutions.core.util.settings.Settings;

/**
 * <pre>
 * Set system property
 * 
 * gemfire.security-users.USERNAME=password,[priviledge],[,priviledge]
 * </pre> 
 * @author Gregory Green
 *
 */

public class SettingsUserService implements CacheLoader<String, User>,  UserService, 
SubjectObserver<Settings>
{	
	/**
	 * Property to support users being loaded from the security.properties file
	 */
	public static final String SECURITY_USERS_PROP = "security-users.";
	

	/**
	 * Factory method to create declare user loader from properties
	 */
	public SettingsUserService()
	{
		this(Config.getSettings());
	}// --------------------------------------------------------
	
	/**
	 *
	 *@param properties the security properties
	 */
	public SettingsUserService(Settings properties)
	{
		loadUsersMap(properties);
	}// --------------------------------------------------------

	private void loadUsersMap(Settings settings)
	{
		if (settings == null)
			throw new IllegalArgumentException("properties is required");
		
		this.declaredUsersMap = new HashMap<String,User>();
		
		settings.getProperties().entrySet().stream().filter((e)->e.getKey().toString().contains(SECURITY_USERS_PROP))
		.map((e) -> createUserFromEntry(e)).forEach( u -> declaredUsersMap.put(u.getUserName(),u));
		
		settings.registerObserver(this);
	}//------------------------------------------------
	@Override
	public void close()
	{
	}// --------------------------------------------------------
	@Override
	public User load(LoaderHelper<String, User> helper) throws CacheLoaderException
	{
		return this.findUser(helper.getKey());
	}// --------------------------------------------------------

	/**
	 * 
	 * @param entry the property map entry
	 * @return the create User
	 */
	private User createUserFromEntry(Map.Entry<Object, Object> entry)
	{
		
		String userName = String.valueOf(entry.getKey());
		
		int indexOf = userName.indexOf(SECURITY_USERS_PROP)+SECURITY_USERS_PROP.length();
		
		userName = userName.substring(indexOf,userName.length());
		
		User user = new User();
		user.setUserName(userName);
		

		String properties = String.valueOf(entry.getValue());
		
		if(properties.trim().length() == 0)
			throw new SetupException(entry.getKey()+" property required");
		
		String[] propertyArray = properties.split(",");
		
		String encryptedPassword = propertyArray[0];
		
		if (encryptedPassword == null)
			throw new IllegalArgumentException(entry.getKey()+" password required");
		
		//check if password can be decrypted
		if(!Cryption.isEncrypted(encryptedPassword))
			throw new SetupException(entry.getKey()+" password not encrypted");
		
		encryptedPassword = encryptedPassword.substring(Cryption.CRYPTION_PREFIX.length());
		
		user.setEncryptedPassword(encryptedPassword.getBytes(StandardCharsets.UTF_8));
		
		
		if(propertyArray.length > 1)
		{
			//add privlidge 
			ArrayList<String> priviledges = new ArrayList<String>(propertyArray.length -1);
			
			for(int i=1;i<propertyArray.length;i++)
			{
				priviledges.add(propertyArray[i].trim());
			}
			user.setPriviledges(priviledges);
		}
		return user;
	}//------------------------------------------------
	/**
	 * @param  userName the user name
	 * @return the user with a declared user name
	 */
	public User findUser(String userName)
	{
		if(declaredUsersMap == null)
			return null;
		
		return declaredUsersMap.get(userName);
	}// --------------------------------------------------------
	
	@Override
	public void update(String subjectName, Settings settings)
	{
		this.loadUsersMap(settings);
	}//------------------------------------------------

	private Map<String, User> declaredUsersMap = null;
	//private final UserRegionService userRegionService;
}
