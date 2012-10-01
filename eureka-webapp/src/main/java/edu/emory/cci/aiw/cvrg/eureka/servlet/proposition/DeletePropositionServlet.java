package edu.emory.cci.aiw.cvrg.eureka.servlet.proposition;

import java.io.IOException;
import java.io.PrintWriter;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.CommUtils;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.PropositionWrapper;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.PropositionWrapper.Type;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.User;

public class DeletePropositionServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DeletePropositionServlet.class);


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		LOGGER.debug("DeletePropositionServlet");
		String id = req.getParameter("id");

		String eurekaServicesUrl = req.getSession().getServletContext()
				.getInitParameter("eureka-services-url");


		try {


			Client client = CommUtils.getClient();
			Principal principal = req.getUserPrincipal();
			String userName = principal.getName();

			WebResource webResource = client.resource(eurekaServicesUrl);
			User user = webResource.path("/api/user/byname/" + userName)
					.accept(MediaType.APPLICATION_JSON).get(User.class);


            // user/delete/{userId}/{prodId}
            ClientResponse response =
                        webResource.path("/api/proposition/user/delete/" + user.getId() + "/"+id)
                            .type(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .delete(ClientResponse.class);

            int status = response.getClientResponseStatus().getStatusCode();
		    System.out.println("status: " + status);
            System.out.println("/api/proposition/user/delete/" + user.getId() + "/"+id);
            resp.setContentType("text/html");
            resp.setStatus(status);
            if (status != HttpServletResponse.SC_OK) {
            }




		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}