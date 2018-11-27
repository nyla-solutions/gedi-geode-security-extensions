package io.pivotal.gedi.geode.security;

public interface UserService
{

	/**
	 * 
	 * @param id the user id
	 * @return the user 
	 */
	User findUser(String id);

}