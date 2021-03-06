/*
 * #%L
 * Eureka Services
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
package edu.emory.cci.aiw.cvrg.eureka.services.resource;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.Destination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.DestinationType;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.EtlDestination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.UserEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.exception.HttpStatusException;
import edu.emory.cci.aiw.cvrg.eureka.services.config.EtlClient;
import edu.emory.cci.aiw.cvrg.eureka.services.conversion.ConversionSupport;
import edu.emory.cci.aiw.cvrg.eureka.services.conversion.DestinationToEtlDestinationVisitor;
import edu.emory.cci.aiw.cvrg.eureka.services.conversion.EtlDestinationToDestinationVisitor;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.UserDao;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 * @author Andrew Post
 */
@Path("/protected/destinations")
@RolesAllowed({"researcher"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DestinationResource {

	private final EtlClient etlClient;
	private final UserDao userDao;
	private final ConversionSupport conversionSupport;

	@Inject
	public DestinationResource(EtlClient inEtlClient, UserDao inUserDao, ConversionSupport inConversionSupport) {
		this.etlClient = inEtlClient;
		this.userDao = inUserDao;
		this.conversionSupport = inConversionSupport;
	}
	
	@POST
	public void create(@Context HttpServletRequest request, Destination inDestination) {
		UserEntity user = this.userDao.getByUsername(request.getRemoteUser());
		DestinationToEtlDestinationVisitor v =
				new DestinationToEtlDestinationVisitor(this.conversionSupport);
		inDestination.accept(v);
		EtlDestination etlDest = v.getEtlDestination();
		try {
			this.etlClient.createDestination(etlDest);
		} catch (ClientException ex) {
			throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
		}
	}
	
	@PUT
	public void update(@Context HttpServletRequest request, Destination inDestination) {
		UserEntity user = this.userDao.getByUsername(request.getRemoteUser());
		DestinationToEtlDestinationVisitor v =
				new DestinationToEtlDestinationVisitor(this.conversionSupport);
		inDestination.accept(v);
		EtlDestination etlDest = v.getEtlDestination();
		try {
			this.etlClient.updateDestination(etlDest);
		} catch (ClientException ex) {
			throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
		}
	}

	/**
	 * Gets all of the destinations for a user
	 *
	 * @return a {@link List} of {@link Destination}s
	 */
	@GET
	public List<Destination> getAll(@Context HttpServletRequest request, @QueryParam("type") DestinationType type) {
		UserEntity user = this.userDao.getByUsername(request.getRemoteUser());
		try {
			List<? extends EtlDestination> destinations;
			if (type == null) {
				destinations = this.etlClient.getDestinations();
			} else {
				switch (type) {
					case I2B2:
						destinations = this.etlClient.getI2B2Destinations();
						break;
					case COHORT:
						destinations = this.etlClient.getCohortDestinations();
						break;
					case PATIENT_SET_EXTRACTOR:
						destinations = this.etlClient.getPatientSetExtractorDestinations();
						break;
					default:
						throw new AssertionError("Unexpected type " + type);
				}
			}
			List<Destination> result = new ArrayList<>(destinations.size());
			EtlDestinationToDestinationVisitor v = 
					new EtlDestinationToDestinationVisitor(this.conversionSupport);
			for (EtlDestination etlDest : destinations) {
				etlDest.accept(v);
				result.add(v.getDestination());
			}
			return result;
		} catch (ClientException ex) {
			throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
		}
	}

	@GET
	@Path("/{id}")
	public Destination get(@Context HttpServletRequest request, @PathParam("id") String inId) {
		UserEntity user = this.userDao.getByUsername(request.getRemoteUser());
		EtlDestinationToDestinationVisitor v = 
					new EtlDestinationToDestinationVisitor(this.conversionSupport);
		try {
			this.etlClient.getDestination(inId).accept(v);
		} catch (ClientException ex) {
			if (ex.getResponseStatus() == ClientResponse.Status.NOT_FOUND) {
				throw new HttpStatusException(Status.NOT_FOUND);
			} else {
				throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
			}
		}
		return v.getDestination();
	}
	
	@DELETE
	@Path("/{id}")
	public void delete(@PathParam("id") String inId) {
		try {
			this.etlClient.deleteDestination(inId);
		} catch (ClientException ex) {
			if (ex.getResponseStatus() == ClientResponse.Status.NOT_FOUND) {
				throw new HttpStatusException(Status.NOT_FOUND);
			} else {
				throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
			}
		}
	}

}
