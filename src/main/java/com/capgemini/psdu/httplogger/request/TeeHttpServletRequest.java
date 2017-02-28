/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package com.capgemini.psdu.httplogger.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.capgemini.psdu.httplogger.io.TeeServletInputStream;

/**
 * Request Wrapper class contains {@link TeeServletInputStream}.
 * 
 * Original code took from Logback-access library to reuse their Tee filter
 * functionality.
 * 
 * @author CG10203
 * 
 */
public class TeeHttpServletRequest extends HttpServletRequestWrapper {

	public static final String X_WWW_FORM_URLECODED = "application/x-www-form-urlencoded";

	private TeeServletInputStream inStream;
	
	private BufferedReader reader;
	
	private boolean postedParametersMode;

	/**
	 * Constructor.
	 * 
	 * @param request original servlet request
	 */
	public TeeHttpServletRequest(HttpServletRequest request) {
		super(request);
		postedParametersMode = false;
		// we can't access the input stream and access the request parameters
		// at the same time
		if (isFormUrlEncoded(request)) {
			postedParametersMode = true;
		} else {
			inStream = new TeeServletInputStream(request);
			reader = new BufferedReader(new InputStreamReader(inStream));
		}

	}

	/**
	 * Returns the buffer.
	 * 
	 * @return bytes of request body.
	 */
	public byte[] getInputBuffer() {
		if (postedParametersMode) {
			throw new IllegalStateException(
					"Call disallowed in postedParametersMode");
		}
		return inStream.getInputBuffer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (!postedParametersMode) {
			return inStream;
		} else {
			return super.getInputStream();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		if (!postedParametersMode) {
			return reader;
		} else {
			return super.getReader();
		}
	}

	/**
	 * Checks if the request content type is form url encoded.
	 * 
	 * @param request servlet request object
	 * @return true if it is form url encoded otherwise false
	 */
	private boolean isFormUrlEncoded(HttpServletRequest request) {

		String contentTypeStr = request.getContentType();
		if ("POST".equalsIgnoreCase(request.getMethod())
				&& contentTypeStr != null
				&& contentTypeStr.startsWith(X_WWW_FORM_URLECODED)) {
			return true;
		} else {
			return false;
		}
	}

}
