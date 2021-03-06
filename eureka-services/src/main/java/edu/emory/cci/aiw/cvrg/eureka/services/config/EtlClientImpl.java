/*
 * #%L
 * Eureka Common
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
package edu.emory.cci.aiw.cvrg.eureka.services.config;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.DestinationType;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.EtlCohortDestination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.EtlDestination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.EtlI2B2Destination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.EtlPatientSetExtractorDestination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.Job;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.JobFilter;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.JobRequest;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.SourceConfig;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.Statistics;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.ValidationRequest;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.EurekaClient;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.protempa.PropositionDefinition;

/**
 * @author hrathod
 */
public class EtlClientImpl extends EurekaClient implements EtlClient {

	private static final GenericType<List<Job>> JobListType = new GenericType<List<Job>>() {
	};
	private static final GenericType<Job> JobType = new GenericType<Job>() {
	};
	private static final GenericType<Statistics> JobStatsType = new GenericType<Statistics>() {
	};
	private static final GenericType<List<SourceConfig>> SourceConfigListType
			= new GenericType<List<SourceConfig>>() {
			};
	private static final GenericType<List<EtlDestination>> DestinationListType
			= new GenericType<List<EtlDestination>>() {
			};
	private static final GenericType<List<EtlCohortDestination>> CohortDestinationListType
			= new GenericType<List<EtlCohortDestination>>() {
			};
	private static final GenericType<List<EtlI2B2Destination>> I2B2DestinationListType
			= new GenericType<List<EtlI2B2Destination>>() {
			};
	private static final GenericType<List<EtlPatientSetExtractorDestination>> PatientSetExtractorDestinationListType
			= new GenericType<List<EtlPatientSetExtractorDestination>>() {
			};
	private static final GenericType<List<PropositionDefinition>> PropositionDefinitionList
			= new GenericType<List<PropositionDefinition>>() {
			};
	private static final GenericType<List<String>> PropositionSearchResultsList
			= new GenericType<List<String>>() {
			};
	private final String resourceUrl;

	@Inject
	public EtlClientImpl(ServiceProperties serviceProperties) {
		this.resourceUrl = serviceProperties.getEtlUrl();
	}

	@Override
	protected String getResourceUrl() {
		return this.resourceUrl;
	}

	@Override
	public List<SourceConfig> getSourceConfigs() throws
			ClientException {
		final String path = "/api/protected/sourceconfigs";
		return doGet(path, SourceConfigListType);
	}

	@Override
	public SourceConfig getSourceConfig(String sourceConfigId) throws
			ClientException {
		String path = UriBuilder.fromPath("/api/protected/sourceconfigs/")
				.segment(sourceConfigId)
				.build().toString();
		return doGet(path, SourceConfig.class);
	}

	@Override
	public List<EtlDestination> getDestinations() throws
			ClientException {
		final String path = "/api/protected/destinations";
		return doGet(path, DestinationListType);
	}

	@Override
	public List<EtlCohortDestination> getCohortDestinations() throws
			ClientException {
		final String path = "/api/protected/destinations/";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("type", DestinationType.COHORT.name());
		return doGet(path, CohortDestinationListType, queryParams);
	}

	@Override
	public List<EtlI2B2Destination> getI2B2Destinations() throws
			ClientException {
		final String path = "/api/protected/destinations/";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("type", DestinationType.I2B2.name());
		return doGet(path, I2B2DestinationListType, queryParams);
	}

	@Override
	public List<EtlPatientSetExtractorDestination> getPatientSetExtractorDestinations() throws ClientException {
		final String path = "/api/protected/destinations/";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("type", DestinationType.PATIENT_SET_EXTRACTOR.name());
		return doGet(path, PatientSetExtractorDestinationListType, queryParams);
	}

	@Override
	public EtlDestination getDestination(String destId) throws
			ClientException {
		String path = UriBuilder.fromPath("/api/protected/destinations/")
				.segment(destId)
				.build().toString();
		return doGet(path, EtlDestination.class);
	}

	@Override
	public void createDestination(EtlDestination etlDest) throws ClientException {
		String path = "/api/protected/destinations";
		doPost(path, etlDest);
	}

	@Override
	public void updateDestination(EtlDestination etlDest) throws ClientException {
		String path = "/api/protected/destinations";
		doPut(path, etlDest);
	}

	@Override
	public void deleteDestination(String etlDestId) throws ClientException {
		String path = UriBuilder.fromPath("/api/protected/destinations/")
				.segment(etlDestId)
				.build().toString();
		doDelete(path);
	}

	@Override
	public Long submitJob(JobRequest inJobRequest) throws ClientException {
		final String path = "/api/protected/jobs";
		URI jobUri = doPostCreate(path, inJobRequest);
		return extractId(jobUri);
	}

	@Override
	public List<Job> getJobStatus(JobFilter inFilter) throws ClientException {
		final String path = "/api/protected/jobs/status";
		return doGet(path, JobListType);
	}

	@Override
	public List<Job> getJobs() throws ClientException {
		final String path = "/api/protected/jobs";
		return doGet(path, JobListType);
	}

	@Override
	public List<Job> getJobsDesc() throws ClientException {
		final String path = "/api/protected/jobs";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("order", "desc");
		return doGet(path, JobListType, queryParams);
	}

	@Override
	public Job getJob(Long inJobId) throws ClientException {
		final String path = "/api/protected/jobs/" + inJobId;
		return doGet(path, JobType);
	}

	@Override
	public Statistics getJobStats(Long inJobId, String inPropId) throws ClientException {
		UriBuilder uriBuilder = UriBuilder.fromPath("/api/protected/jobs/{arg1}/stats/");
		if (inPropId != null) {
			uriBuilder = uriBuilder.segment(inPropId);
		}
		return doGet(uriBuilder.build(inJobId).toString(), JobStatsType);
	}

	@Override
	public void validatePropositions(ValidationRequest inRequest) throws
			ClientException {
		final String path = "/api/protected/validate";
		doPost(path, inRequest);
	}

	/**
	 * Gets a proposition definition with a specified id for a specified user.
	 *
	 * @param sourceConfigId the source config id of interest.
	 * @param inKey the proposition id of interest.
	 * @return the proposition id, if found, or <code>null</code> if not.
	 *
	 * @throws ClientException if an error occurred looking for the proposition
	 * definition.
	 */
	@Override
	public PropositionDefinition getPropositionDefinition(
			String sourceConfigId, String inKey) throws ClientException {
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		formParams.add("key", inKey);
		String path = UriBuilder.fromPath("/api/protected/proposition/")
				.segment(sourceConfigId)
				.build().toString();
		List<PropositionDefinition> propDefs = doPost(path, PropositionDefinitionList, formParams);
		if (propDefs.isEmpty()) {
			throw new ClientException(ClientResponse.Status.NOT_FOUND, null);
		} else {
			return propDefs.get(0);
		}
	}

	/**
	 * Gets all of the proposition definitions given by the key IDs for the
	 * given user.
	 *
	 * @param sourceConfigId the ID of the source configuration to use
	 * @param inKeys the keys (IDs) of the proposition definitions to get
	 * @param withChildren whether to get the children of specified proposition
	 * definitions as well
	 * @return a {@link List} of {@link PropositionDefinition}s
	 *
	 * @throws ClientException if an error occurred looking for the proposition
	 * definitions
	 */
	@Override
	public List<PropositionDefinition> getPropositionDefinitions(
			String sourceConfigId, List<String> inKeys, Boolean withChildren) throws ClientException {
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		for (String key : inKeys) {
			formParams.add("key", key);
		}
		formParams.add("withChildren", withChildren.toString());
		String path = UriBuilder.fromPath("/api/protected/proposition/")
				.segment(sourceConfigId)
				.build().toString();
		return doPost(path, PropositionDefinitionList, formParams);
	}

	@Override
	public void upload(String fileName, String sourceId,
			String fileTypeId, InputStream inputStream)
			throws ClientException {
		String path = UriBuilder
				.fromPath("/api/protected/file/upload/")
				.segment(sourceId)
				.segment(fileTypeId)
				.build().toString();
		doPostMultipart(path, fileName, inputStream);
	}

	@Override
	public List<String> getPropositionSearchResults(String sourceConfigId,
			String inSearchKey) throws ClientException {

		String path = UriBuilder.fromPath("/api/protected/proposition/search/")
				.segment(sourceConfigId)
				.segment(inSearchKey)
				.build().toString();
		return doGet(path, PropositionSearchResultsList);
	}

	@Override
	public List<PropositionDefinition> getPropositionSearchResultsBySearchKey(String sourceConfigId,
			String inSearchKey) throws ClientException {

		String path = UriBuilder.fromPath("/api/protected/proposition/propsearch/")
				.segment(sourceConfigId)
				.segment(inSearchKey)
				.build().toString();
		return doGet(path, PropositionDefinitionList);
	}

	@Override
	public ClientResponse getOutput(String destinationId) throws ClientException {
		String path = UriBuilder.fromPath("/api/protected/output/")
				.segment(destinationId)
				.build().toString();
		return doGetResponse(path);
	}

	@Override
	public void deleteOutput(String destinationId) throws ClientException {
		String path = UriBuilder.fromPath("/api/protected/output/")
				.segment(destinationId)
				.build().toString();
		doDelete(path);
	}
	
	

}
