package com.capgemini.psdu.httplogger.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capgemini.psdu.httplogger.request.TeeHttpServletRequest;
import com.capgemini.psdu.httplogger.response.TeeHttpServletResponse;

/**
 * Servlet filter to log every request and response.
 *
 * @author CG10203
 * @author cg12988
 */
public class HttpRequestResponseFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestResponseFilter.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Do Nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (LOGGER.isDebugEnabled()) {
			TeeHttpServletRequest teeRequest = new TeeHttpServletRequest((HttpServletRequest) request);
			TeeHttpServletResponse teeResponse = new TeeHttpServletResponse((HttpServletResponse) response);

			// Log request
			String correlationId = getCorrelationId((HttpServletRequest) request);
			String requestHeaders = buildRequestHeaderString((HttpServletRequest) request);
			String requestBody = null;
			byte[] bytes = teeRequest.getInputBuffer();
			if (bytes != null && bytes.length != 0) {
				requestBody = new String(bytes);
			}
			Map<String, String> requestMap = getRequestMap(teeRequest);
			LOGGER.debug("CorrelationId: {}, Request headers: {},Request param: {}, Request body: {}", correlationId, requestHeaders, requestMap, requestBody);

			// Inject custom wrappers
			chain.doFilter(teeRequest, teeResponse);

			// Log response
			String responseBody = null;
			bytes = teeResponse.getOutputBuffer();
			if (bytes != null && bytes.length != 0) {
				responseBody = new String(bytes);
			}
			LOGGER.debug("CorrelationId: {}, Response body: {}", correlationId, responseBody);

			teeResponse.finish();
		}
		else {
			chain.doFilter(request, response);
		}
	}

	private static Map<String, String> getRequestMap(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();
		Enumeration<String> it = request.getParameterNames();
		while (it.hasMoreElements()) {
			String key = it.nextElement();
			result.put(key, request.getParameter(key));
		}
		return result;
	}

	private static String buildRequestHeaderString(HttpServletRequest httpRequest) {
		Enumeration<?> enumeration = httpRequest.getHeaderNames();
		StringBuilder headers = new StringBuilder();
		while (enumeration.hasMoreElements()) {
			String headerName = (String) enumeration.nextElement();
			String header = httpRequest.getHeader(headerName);
			headers.append(headerName + " = " + header + " ");
		}
		return headers.toString();
	}

	private static String getCorrelationId(HttpServletRequest httpRequest) {
		Enumeration<?> enumeration = httpRequest.getHeaderNames();
		while (enumeration.hasMoreElements()) {
			String headerName = (String) enumeration.nextElement();
			if (headerName.equals("CorrelationId")) {
				return httpRequest.getHeader(headerName);
			}
		}
		return "[unspecified]";
	}

	@Override
	public void destroy() {
		// Do Nothing
	}

}
