package gedi.solutions.geode.security;

public interface UserService
{

	/**
	 * 
	 * @param id the user id
	 * @return the user 
	 */
	User findUser(String id);

}