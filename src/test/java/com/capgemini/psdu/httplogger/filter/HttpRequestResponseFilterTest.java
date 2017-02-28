package com.capgemini.psdu.httplogger.filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.DelegatingServletInputStream;

import com.capgemini.psdu.httplogger.request.TeeHttpServletRequest;
import com.capgemini.psdu.httplogger.response.TeeHttpServletResponse;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerFactory.class})
public class HttpRequestResponseFilterTest {
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private HttpServletResponse response;
	
	@Mock
	private FilterChain chain;
	
	@Mock
	private Logger logger;
	
	private DelegatingServletInputStream dis;
	
	private Enumeration<String> headerEnumeration;
	
	private Enumeration<String> headerEnumerationWithCorrelationId;
	
	private String body = "{\"prop1\":\"value1\"}";

	@Before
	@SuppressWarnings("unchecked")
	public void prepare(){
		MockitoAnnotations.initMocks(this);
		InputStream is = new ByteArrayInputStream(body.getBytes());
		dis = new DelegatingServletInputStream(is);
		
		String[] headerArray = {"H1", "H2"};
		Iterator<String> headerArrayIterator = Arrays.asList(headerArray).iterator();
		headerEnumeration = new IteratorEnumeration(headerArrayIterator);
		
		String[] arr = {"H1", "H2", "CorrelationId"};
		Iterator<String> headerArrayWithCorrelationIdIterator = Arrays.asList(arr).iterator();
		headerEnumerationWithCorrelationId = new IteratorEnumeration(headerArrayWithCorrelationIdIterator);
	}
	
	@After
	public void tearDown(){
		Mockito.reset(request, response, chain, logger);
	}

    @Test
    public void testDoFilterDebugEnabledWithCorrelationId() throws Exception {
		// Mock    	
    	mockStatic(LoggerFactory.class);
		when(request.getInputStream()).thenReturn(dis);
		when(request.getHeaderNames()).thenReturn(headerEnumerationWithCorrelationId);
		when(request.getHeader("H1")).thenReturn("Value1");
		when(request.getHeader("H2")).thenReturn("Value2");
		when(request.getHeader("CorrelationId")).thenReturn("1234QWER");
        when(LoggerFactory.getLogger(HttpRequestResponseFilter.class)).thenReturn(logger);
        when(logger.isDebugEnabled()).thenReturn(true);
        
        // Invoke
        new HttpRequestResponseFilter().doFilter(request, response, chain);

        // Verify
        verify(chain).doFilter(any(TeeHttpServletRequest.class), any(TeeHttpServletResponse.class));
        verify(logger, Mockito.times(1)).debug(any(String.class), eq("1234QWER"), eq(new String()), eq(body));
        verify(logger, Mockito.times(1)).debug(any(String.class), any(String.class), any(String.class));
    }
    
    @Test
    public void testDoFilterDebugDisabled() throws Exception {
		// Mock
    	mockStatic(LoggerFactory.class);
		when(request.getInputStream()).thenReturn(dis);
		when(request.getHeaderNames()).thenReturn(headerEnumeration);
		when(request.getHeader("H1")).thenReturn("Value1");
		when(request.getHeader("H2")).thenReturn("Value2");
        when(LoggerFactory.getLogger(HttpRequestResponseFilter.class)).thenReturn(logger);
        when(logger.isDebugEnabled()).thenReturn(false);
        
        // Invoke
        new HttpRequestResponseFilter().doFilter(request, response, chain);

        // Verify
        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(logger, Mockito.times(0)).debug(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(logger, Mockito.times(0)).debug(any(String.class), any(String.class), any(String.class));
    }
}