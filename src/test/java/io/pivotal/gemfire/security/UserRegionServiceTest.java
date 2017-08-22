package io.pivotal.gemfire.security;

import static org.junit.Assert.*;

import org.apache.geode.cache.Region;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class UserRegionServiceTest 
{
	@Before
	@SuppressWarnings("unchecked")
	public void init()
	{
		
		Region<String,User> region = mock(Region.class);
		
		userRegionService = new UserRegionService(region);
		when(userRegionService.findUser("admin")).thenReturn(new User());
		
	}
	@Test
	public void testFindUser()
	{
		assertNull(userRegionService.findUser(null));
		
		assertNull(userRegionService.findUser("invalid"));
		assertNotNull(userRegionService.findUser("admin"));
	}

	private UserService userRegionService;
}
