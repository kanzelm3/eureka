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
import edu.emory.cci.aiw.cvrg.eureka.common.comm.Job;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.JobFilter;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.JobRequest;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.JobSpec;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.UserEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.exception.HttpStatusException;
import edu.emory.cci.aiw.cvrg.eureka.services.config.EtlClient;
import edu.emory.cci.aiw.cvrg.eureka.services.config.ServiceProperties;
import edu.emory.cci.aiw.cvrg.eureka.services.conversion.ConversionSupport;
import edu.emory.cci.aiw.cvrg.eureka.services.conversion.PropositionDefinitionCollector;
import edu.emory.cci.aiw.cvrg.eureka.services.conversion.PropositionDefinitionConverterVisitor;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.DataElementEntityDao;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.UserDao;
import org.protempa.PropositionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST operations related to jobs submitted by the user.
 *
 * @author hrathod
 *
 */
@Path("/protected/jobs")
@RolesAllowed({"researcher"})
public class JobResource {

	/**
	 * The class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JobResource.class);
	/**
	 * The User data access object to retrieve information about the current
	 * user.
	 */
	private final UserDao userDao;
	/**
	 * Used to fetch the user's Propositions, to be sent to the ETL layer when
	 * submitting a new job request.
	 */
	private final DataElementEntityDao propositionDao;
	/**
	 * Used for converting the different types of Eureka data entities to
	 * Protempa proposition definitions.
	 */
	private final PropositionDefinitionConverterVisitor converterVisitor;
	private final EtlClient etlClient;
	private final ConversionSupport conversionSupport;
	private final ServiceProperties properties;

	/**
	 * Construct a new job resource with the given job update thread.
	 *
	 * @param inUserDao The data access object used to fetch information about
	 * users.
	 * @param inVisitor The proposition definition converter visitor that will
	 * be used to determine how to convert proposition definitions
	 * @param inPropositionDao The data access object used to fetch information
	 * about propositions.
	 * @param inEtlClient The ETL client to use to perform ETL operations.
	 */
	@Inject
	public JobResource(UserDao inUserDao,
			PropositionDefinitionConverterVisitor inVisitor,
			DataElementEntityDao inPropositionDao,
			EtlClient inEtlClient,
			ServiceProperties inProperties) {
		this.userDao = inUserDao;
		this.propositionDao = inPropositionDao;
		this.converterVisitor = inVisitor;
		this.etlClient = inEtlClient;
		this.conversionSupport = new ConversionSupport();
		this.properties = inProperties;
	}

	/**
	 * Create a new job (by uploading a new file).
	 *
	 * @param jobSpec The file upload to add.
	 *
	 * @return A {@link javax.ws.rs.core.Response} indicating the result of the
	 * operation.
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public Response submit(@Context HttpServletRequest request, JobSpec jobSpec) {
		LOGGER.debug("Got job submission: {}", jobSpec);
		UserEntity user = this.userDao.getByHttpServletRequest(request);
		JobRequest jobRequest = new JobRequest();
		PropositionDefinitionCollector collector
				= PropositionDefinitionCollector.getInstance(
						this.converterVisitor, this.propositionDao
						.getByUserId(user.getId()));
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sending {} proposition definitions:", collector.getUserPropDefs().size());
			for (PropositionDefinition pd : collector.getUserPropDefs()) {
				LOGGER.trace("PropDef: {}", pd);
			}
		}
		
		jobRequest.setJobSpec(jobSpec);
		jobRequest.setUserPropositions(collector.getUserPropDefs());
		List<String> conceptIds = jobSpec.getPropositionIds();
		List<String> propIds = new ArrayList<>(conceptIds.size());
		for (String conceptId : conceptIds) {
			propIds.add(this.conversionSupport.toPropositionId(conceptId));
		}
		jobRequest.setPropositionIdsToShow(propIds);
		
		Long jobId;
		try {
			jobId = this.etlClient.submitJob(jobRequest);
		} catch (ClientException ex) {
			throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
		}

		return Response.created(URI.create("/" + jobId)).build();
	}

	@Path("/{jobId}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Job getJob(@PathParam("jobId") Long inJobId) {
		try {
			return this.etlClient.getJob(inJobId);
		} catch (ClientException ex) {
			ClientResponse.Status responseStatus = ex.getResponseStatus();
			if (responseStatus == ClientResponse.Status.NOT_FOUND) {
				throw new HttpStatusException(Status.NOT_FOUND);
			} else {
				throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
			}
		}
	}

	@GET
	@Path("/{jobId}/stats/{key}")
	@Produces({MediaType.APPLICATION_JSON})
	public edu.emory.cci.aiw.cvrg.eureka.common.comm.Statistics getJobStats(@Context HttpServletRequest request,
			@PathParam("jobId") Long inJobId, @PathParam("key") String key) {
		try {
			edu.emory.cci.aiw.cvrg.eureka.common.comm.Statistics stats = this.etlClient.getJobStats(inJobId, key != null ? this.conversionSupport.toPropositionId(key) : null);
			edu.emory.cci.aiw.cvrg.eureka.common.comm.Statistics convertedStats = new edu.emory.cci.aiw.cvrg.eureka.common.comm.Statistics();

			Map<String, String> childrenToParents = stats.getChildrenToParents();
			Map<String, String> convertedChildrenToParents = new HashMap<>();
			for (Map.Entry<String, String> me : childrenToParents.entrySet()) {
				String dataElementKey = this.conversionSupport.toDataElementKey(me.getKey());
				String parentKey = me.getValue();
				if (dataElementKey != null) {
					convertedChildrenToParents.put(dataElementKey, parentKey != null ? this.conversionSupport.toDataElementKey(parentKey) : null);
				}
			}
			convertedStats.setChildrenToParents(convertedChildrenToParents);

			Map<String, Integer> counts = stats.getCounts();
			Map<String, Integer> convertedCounts = new HashMap<>();
			for (Map.Entry<String, Integer> me : counts.entrySet()) {
				String dataElementKey = this.conversionSupport.toDataElementKey(me.getKey());
				if (dataElementKey != null) {
					convertedCounts.put(dataElementKey, me.getValue());
				}
			}
			convertedStats.setCounts(convertedCounts);
			return convertedStats;
		} catch (ClientException ex) {
			ClientResponse.Status responseStatus = ex.getResponseStatus();
			if (responseStatus == ClientResponse.Status.NOT_FOUND) {
				throw new HttpStatusException(Status.NOT_FOUND);
			} else {
				throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
			}
		}
	}

	@GET
	@Path("/{jobId}/stats")
	@Produces({MediaType.APPLICATION_JSON})
	public edu.emory.cci.aiw.cvrg.eureka.common.comm.Statistics getJobStatsRoot(@Context HttpServletRequest request,
			@PathParam("jobId") Long inJobId) {
		return getJobStats(request, inJobId, null);
	}

	/**
	 * Get a list of jobs associated with user referred to by the given unique
	 * identifier.
	 *
	 * @param order The order in which to get the user's jobs.
	 *
	 * @return A list of {@link Job} objects associated with the user.
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<Job> getJobsByUser(@QueryParam("order") String order) {
		try {
			if (order == null) {
				return this.etlClient.getJobs();
			} else if (order.equals("desc")) {
				return this.etlClient.getJobsDesc();
			} else {
				throw new HttpStatusException(Status.PRECONDITION_FAILED,
						"Invalid value for the order query parameter: " + order);
			}
		} catch (ClientException ex) {
			throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
		}
	}

	/**
	 * Get the status of the most recent job process for the given user.
	 *
	 * @param inFilter The filter to use when fetching the job statuses.
	 * @return A {@link List} of {@link Job}s containing the status information.
	 */
	@Path("/status")
	@GET
	@RolesAllowed({"admin"})
	@Produces({MediaType.APPLICATION_JSON})
	public List<Job> getStatus(@QueryParam("filter") JobFilter inFilter) {
		try {
			return this.etlClient.getJobStatus(inFilter);
		} catch (ClientException ex) {
			throw new HttpStatusException(Status.INTERNAL_SERVER_ERROR, ex);
		}
	}
}
