package io.pivotal.gedi.geode.security.functions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.RegionFunctionContext;

import io.pivotal.gedi.geode.security.User;

/**
 * Add a given user to the user regions
 * @author Gregory Green
 *
 */
public class AddUserFunction implements Function
{

	private static final long serialVersionUID = -2383717885739589513L;

	@Override
	public void execute(FunctionContext functionContext)
	{
		
		if(!RegionFunctionContext.class.isAssignableFrom(functionContext.getClass()))
			throw new FunctionException("Function must be executed onRegion");
		
		RegionFunctionContext rfc = (RegionFunctionContext)functionContext;
		
		String[] args = (String[])rfc.getArguments();
	
		if (args == null)
			throw new IllegalArgumentException("args is required");
		
		String userName = args[0];
		byte[] password = args[1].getBytes(StandardCharsets.UTF_8);
		
		Collection<String> priviledges = null;
		
	    if(args.length  > 2)
	    {
	    		priviledges = new ArrayList<String>(args.length-2);
	    		
	    		for (int i = 2; i < args.length; i++)
			{
	    			priviledges.add(args[i]);
			}
	    }
		
		User user = new User(userName, password, priviledges);
		
		Region<String,User> userRegion = rfc.getDataSet();
		
		userRegion.put(userName, user);
		
		rfc.getResultSender().lastResult(userName);
	
	}//------------------------------------------------
}
