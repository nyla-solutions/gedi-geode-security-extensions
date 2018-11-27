package io.pivotal.gedi.geode.security;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;

/**
 * Manages user access 
 * @author Gregory Green
 *
 */
public class UserRegionService implements UserService
{
	public UserRegionService(Region<String,User> region)
	{
		this.region = region;
	}//------------------------------------------------
	/**
	 * @param id the user id to find
	 * @return the user
	 */
	@Override
	public User findUser(String id)
	{
		if(region == null)
			region = CacheFactory.getAnyInstance().getRegion("users");
		
		return region.get(id);
	}

	private Region<String,User> region;
}
