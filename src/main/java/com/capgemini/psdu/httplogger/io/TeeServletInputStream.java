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
package com.capgemini.psdu.httplogger.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * Class contains buffer to hold and return request body.
 * 
 * Original code took from Logback-access library to reuse their Tee filter
 * functionality.
 * 
 * @author CG10203
 * 
 */
public class TeeServletInputStream extends ServletInputStream {

	private InputStream in;
	
	private byte[] inputBuffer;

	/**
	 * Constructor.
	 * 
	 * @param request original servlet request
	 */
	public TeeServletInputStream(HttpServletRequest request) {
		duplicateInputStream(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		return in.read();
	}

	/**
	 * Copy input stream into the property level input stream as well as buffer.
	 * 
	 * @param request servlet request
	 */
	private void duplicateInputStream(HttpServletRequest request) {
		ServletInputStream originalSIS = null;
		try {
			originalSIS = request.getInputStream();
			inputBuffer = consumeBufferAndReturnAsByteArray(originalSIS);
			this.in = new ByteArrayInputStream(inputBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStream(originalSIS);
		}
	}

	/**
	 * Read and return input stream content as bytes.
	 * 
	 * @param is input stream
	 * @return bytes
	 * @throws IOException IO exception
	 */
	private byte[] consumeBufferAndReturnAsByteArray(InputStream is) throws IOException {
		int len = 1024;
		byte[] temp = new byte[len];
		int c = -1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((c = is.read(temp, 0, len)) != -1) {
			baos.write(temp, 0, c);
		}
		return baos.toByteArray();
	}

	/**
	 * Close the supplied input stream.
	 * 
	 * @param is input stream
	 */
	private void closeStream(ServletInputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Return the buffer.
	 * 
	 * @return bytes
	 */
	public byte[] getInputBuffer() {
		return inputBuffer;
	}
}
