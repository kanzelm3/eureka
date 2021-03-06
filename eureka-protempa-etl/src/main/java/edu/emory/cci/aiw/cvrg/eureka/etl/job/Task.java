/*
 * #%L
 * Eureka Protempa ETL
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
package edu.emory.cci.aiw.cvrg.eureka.etl.job;

import java.util.List;

import org.protempa.PropositionDefinition;
import org.protempa.backend.dsb.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.common.entity.JobEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.JobEvent;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.JobStatus;
import edu.emory.cci.aiw.cvrg.eureka.etl.dao.JobDao;
import edu.emory.cci.aiw.cvrg.eureka.etl.dao.JobEventDao;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import org.protempa.backend.Configuration;
import java.util.Date;

public final class Task implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);
	private final JobDao jobDao;
	private final JobEventDao jobEventDao;
	private final ETL etl;
	private Long jobId;
	private List<PropositionDefinition> propositionDefinitions;
	private List<String> propIdsToShow;
	private Filter filter;
	private boolean updateData;
	private Configuration prompts;

	@Inject
	Task(JobDao inJobDao, JobEventDao inJobEventDao, ETL inEtl) {
		this.jobDao = inJobDao;
		this.jobEventDao = inJobEventDao;
		this.etl = inEtl;
		this.propIdsToShow = Collections.emptyList();
		this.propositionDefinitions = Collections.emptyList();
	}

	Long getJobId() {
		return jobId;
	}

	void setJobId(Long inJobId) {
		jobId = inJobId;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	List<String> getPropositionIdsToShow() {
		return new ArrayList<>(this.propIdsToShow);
	}

	void setPropositionIdsToShow(List<String> propIdsToShow) {
		if (propIdsToShow == null) {
			this.propIdsToShow = Collections.emptyList();
		} else {
			this.propIdsToShow = new ArrayList<>(propIdsToShow);
		}
	}

	List<PropositionDefinition> getPropositionDefinitions() {
		return propositionDefinitions;
	}

	void setPropositionDefinitions(List<PropositionDefinition> inPropositionDefinitions) {
		if (inPropositionDefinitions != null) {
			this.propositionDefinitions = inPropositionDefinitions;
		} else {
			propositionDefinitions = Collections.emptyList();
		}
	}

	public boolean isUpdateData() {
		return updateData;
	}

	public void setUpdateData(boolean updateData) {
		this.updateData = updateData;
	}

	public Configuration getPrompts() {
		return prompts;
	}

	public void setPrompts(Configuration prompts) {
		this.prompts = prompts;
	}

	@Override
	public void run() {
		JobEntity myJob = null;
		try {
			myJob = this.jobDao.retrieve(this.jobId);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Just got job {} from user {}",
						new Object[]{myJob.getId(),
							myJob.getUser().getUsername()});
			}
			JobEvent startedJobEvent = new JobEvent();
			startedJobEvent.setJob(myJob);
			startedJobEvent.setTimeStamp(new Date());
			startedJobEvent.setStatus(JobStatus.STARTED);
			startedJobEvent.setMessage("Processing started");
			this.jobEventDao.create(startedJobEvent);
			this.jobDao.refresh(myJob);

			PropositionDefinition[] propDefArray
					= new PropositionDefinition[this.getPropositionDefinitions()
					.size()];
			this.propositionDefinitions.toArray(propDefArray);

			String[] propIdsToShowArray
					= this.propIdsToShow.toArray(
							new String[this.propIdsToShow.size()]);

			this.etl.run(myJob, propDefArray, propIdsToShowArray, this.filter, this.updateData, this.prompts);
			this.etl.close();
			JobEvent completedJobEvent = new JobEvent();
			completedJobEvent.setJob(myJob);
			completedJobEvent.setTimeStamp(new Date());
			completedJobEvent.setStatus(JobStatus.COMPLETED);
			completedJobEvent.setMessage("Processing completed without error");
			this.jobEventDao.create(completedJobEvent);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Completed job {} for user {} without errors.",
						new Object[]{
							myJob.getId(), myJob.getUser().getUsername()});
			}
			myJob = null;
		} catch (EtlException | Error | RuntimeException e) {
			handleError(myJob, e);
		} finally {
			if (myJob != null) {
				try {
					JobEvent failedJobEvent = new JobEvent();
					failedJobEvent.setJob(myJob);
					failedJobEvent.setTimeStamp(new Date());
					failedJobEvent.setStatus(JobStatus.FAILED);
					failedJobEvent.setMessage("Processing failed");
					LOGGER.error("Finished job {} for user {} with errors.",
							new Object[]{
								myJob.getId(), myJob.getUser().getUsername()});
					this.jobEventDao.create(failedJobEvent);
				} catch (Throwable ignore) {
				}
			}
			if (this.etl != null) {
				try {
					this.etl.close();
				} catch (Throwable ignore) {
				}
			}
		}

	}

	private void handleError(JobEntity job, Throwable e) {
		if (job != null) {
			LOGGER.error("Job " + job.getId() + " for user "
					+ job.getUser().getUsername() + " failed: " + e.getMessage(), e);

			StringWriter sw = new StringWriter();
			try (PrintWriter ps = new PrintWriter(sw)) {
				e.printStackTrace(ps);
			}
			String msg = e.getMessage();
			if (msg == null) {
				msg = e.getClass().getName();
			}
			JobEvent errorJobEvent = new JobEvent();
			errorJobEvent.setJob(job);
			errorJobEvent.setTimeStamp(new Date());
			errorJobEvent.setStatus(JobStatus.ERROR);
			errorJobEvent.setMessage(msg);
			errorJobEvent.setExceptionStackTrace(sw.toString());
			this.jobEventDao.create(errorJobEvent);
		} else {
			LOGGER.error("Could not create job: " + e.getMessage(), e);
		}

	}
}
