package io.pivotal.gedi.geode.security.functions;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.execute.ResultSender;
import org.junit.Test;

import io.pivotal.gedi.geode.security.functions.AddUserFunction;

import static org.mockito.Mockito.*;

public class AddUserFunctionTest
{

	@SuppressWarnings("unchecked")
	@Test
	public void test_shouldAddUserPassword()
	{
		String userName = "test";
		
		String [] args =  {userName,"secret","ALL"};
		
		Region<Object,Object> userRegion = mock(Region.class);
		ResultSender<Object> resultSender = mock(ResultSender.class);
		
		RegionFunctionContext rfc = mock(RegionFunctionContext.class);
		when(rfc.getArguments()).thenReturn(args);
		when(rfc.getDataSet()).thenReturn(userRegion);
		when(rfc.getResultSender()).thenReturn(resultSender);
	
		
		AddUserFunction func = new AddUserFunction();
		
		func.execute(rfc);
		
		verify(userRegion).put(anyString(), any());
		
	}

}
