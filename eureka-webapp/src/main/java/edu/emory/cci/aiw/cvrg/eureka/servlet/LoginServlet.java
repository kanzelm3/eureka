/*
 * #%L
 * Eureka WebApp
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.emory.cci.aiw.cvrg.eureka.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse.Status;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.User;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.HttpStatus;

public class LoginServlet extends HttpServlet {

	private final ServicesClient servicesClient;

	@Inject
	public LoginServlet(ServicesClient inClient) {
		this.servicesClient = inClient;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			User user = (User) req.getAttribute("user");
			user.setLastLogin(new Date());
			this.servicesClient.updateUser(user,user.getId());
			resp.sendRedirect(req.getContextPath() + "/index.jsp");
		} catch (ClientException e) {
			Status responseStatus = e.getResponseStatus();
			if (responseStatus == Status.FORBIDDEN) {
				HttpSession session = req.getSession(false);
				if (session != null) {
					session.invalidate();
				}
				resp.sendError(HttpStatus.SC_FORBIDDEN);
			} else {
				throw new ServletException(e);
			}
		}
	}
}
