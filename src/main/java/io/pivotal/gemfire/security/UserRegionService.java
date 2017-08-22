package io.pivotal.gemfire.security;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;


public class UserRegionService implements UserService
{
	public UserRegionService(Region<String,User> region)
	{
		this.region = region;
	}//------------------------------------------------
	/* (non-Javadoc)
	 * @see io.pivotal.gemfire.security.UserService#findUser(java.lang.String)
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
