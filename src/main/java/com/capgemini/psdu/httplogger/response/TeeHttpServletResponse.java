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
package com.capgemini.psdu.httplogger.response;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.capgemini.psdu.httplogger.io.TeeServletOutputStream;

/**
 * Request Wrapper class contains {@link TeeServletOutputStream}.
 * 
 * Original code took from Logback-access library to reuse their Tee filter
 * functionality.
 * 
 * @author CG10203
 * 
 */
public class TeeHttpServletResponse extends HttpServletResponseWrapper {

	private TeeServletOutputStream teeServletOutputStream;
	
	private PrintWriter teeWriter;
	
	private int httpStatus;

	/**
	 * Constructor.
	 * 
	 * @param request original servlet response
	 */
	public TeeHttpServletResponse(HttpServletResponse httpServletResponse) {
		super(httpServletResponse);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (teeServletOutputStream == null) {
			teeServletOutputStream = new TeeServletOutputStream(
					this.getResponse());
		}
		return teeServletOutputStream;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrintWriter getWriter() throws IOException {
		if (this.teeWriter == null) {
			this.teeWriter = new PrintWriter(new OutputStreamWriter(
					getOutputStream()), true);
		}
		return this.teeWriter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flushBuffer() {
		if (this.teeWriter != null) {
			this.teeWriter.flush();
		}
	}

	/**
	 * Returns the buffer.
	 * 
	 * @return bytes of response body.
	 */
	public byte[] getOutputBuffer() {
		// teeServletOutputStream can be null if the getOutputStream method is never called.
		if (teeServletOutputStream != null) {
			return teeServletOutputStream.getOutputStreamAsByteArray();
		} else {
			return null;
		}
	}

	/**
	 * Close all streams and writer.
	 * 
	 * @throws IOException IO exception
	 */
	public void finish() throws IOException {
		if (this.teeWriter != null) {
			this.teeWriter.close();
		}
		if (this.teeServletOutputStream != null) {
			this.teeServletOutputStream.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendError(int sc) throws IOException {
		httpStatus = sc;
		super.sendError(sc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendError(int sc, String msg) throws IOException {
		httpStatus = sc;
		super.sendError(sc, msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStatus(int sc) {
		httpStatus = sc;
		super.setStatus(sc);
	}

	/**
	 * Return the HTTP return status.
	 * 
	 * @return status
	 */
	public int getStatus() {
		return httpStatus;
	}

}
