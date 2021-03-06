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
package edu.emory.cci.aiw.cvrg.eureka.common.comm;

import java.util.List;

import edu.emory.cci.aiw.cvrg.eureka.common.exception.DataElementHandlingException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Container class for the sequence user-created data element from the UI.
 * Essentially a direct mapping from the sequence element form fields.
 */
public final class Sequence extends DataElement {

	private DataElementField primaryDataElement;
	private List<RelatedDataElementField> relatedDataElements;

	public Sequence () {
		super(Type.SEQUENCE);
	}

	public DataElementField getPrimaryDataElement() {
		return primaryDataElement;
	}

	public void setPrimaryDataElement(DataElementField primaryDataElement) {
		this.primaryDataElement = primaryDataElement;
	}

	public List<RelatedDataElementField> getRelatedDataElements() {
		return relatedDataElements;
	}

	public void setRelatedDataElements(
	        List<RelatedDataElementField> relatedDataElements) {
		this.relatedDataElements = relatedDataElements;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.reflectionToString(this);
	}

	@Override
	public void accept(DataElementVisitor visitor) 
			throws DataElementHandlingException{
		visitor.visit(this);
	}
}
