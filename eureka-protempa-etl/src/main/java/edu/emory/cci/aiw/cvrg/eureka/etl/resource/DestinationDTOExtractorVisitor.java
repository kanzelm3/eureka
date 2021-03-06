package edu.emory.cci.aiw.cvrg.eureka.etl.resource;

/*
 * #%L
 * Eureka Protempa ETL
 * %%
 * Copyright (C) 2012 - 2014 Emory University
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

import edu.emory.cci.aiw.cvrg.eureka.common.comm.EtlDestination;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.CohortDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.DestinationEntityVisitor;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.AuthorizedUserEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.I2B2DestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.Neo4jDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.PatientSetExtractorDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.etl.config.EtlProperties;
import edu.emory.cci.aiw.cvrg.eureka.etl.dao.EtlGroupDao;

/**
 *
 * @author Andrew Post
 */
public class DestinationDTOExtractorVisitor implements ConfigDTOExtractorVisitor, DestinationEntityVisitor {
	private final CohortDestinationsDTOExtractor cohortExtractor;
	private final I2B2DestinationsDTOExtractor i2b2Extractor;
	private final Neo4jDestinationsDTOExtractor neo4jExtractor;
	private final PatientSetExtractorDestinationsDTOExtractor patientSetSenderExtractor;
	private EtlDestination destDTO;

	public DestinationDTOExtractorVisitor(EtlProperties inEtlProperties, AuthorizedUserEntity user, EtlGroupDao inGroupDao) {
		this.cohortExtractor = new CohortDestinationsDTOExtractor(user, inGroupDao);
		this.i2b2Extractor = new I2B2DestinationsDTOExtractor(inEtlProperties, user, inGroupDao);
		this.neo4jExtractor = new Neo4jDestinationsDTOExtractor(inEtlProperties, user, inGroupDao);
		this.patientSetSenderExtractor = new PatientSetExtractorDestinationsDTOExtractor(user, inGroupDao);
	}
	
	@Override
	public void visit(CohortDestinationEntity cohortDestination) {
		this.destDTO = cohortExtractor.extractDTO(cohortDestination);
	}

	@Override
	public void visit(I2B2DestinationEntity i2b2Destination) {
		this.destDTO = i2b2Extractor.extractDTO(i2b2Destination);
	}

	@Override
	public void visit(Neo4jDestinationEntity neo4jDestination) {
		this.destDTO = this.neo4jExtractor.extractDTO(neo4jDestination);
	}
	
	@Override
	public void visit(PatientSetExtractorDestinationEntity patientSetSenderDestination) {
		this.destDTO = this.patientSetSenderExtractor.extractDTO(patientSetSenderDestination);
	}
	
	public EtlDestination getEtlDestination() {
		return this.destDTO;
	}

}
