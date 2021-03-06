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
package edu.emory.cci.aiw.cvrg.eureka.common.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author hrathod
 */
@Entity
@Table(name = "relations")
public class Relation {

	@Id
	@SequenceGenerator(name = "RELATION_SEQ_GENERATOR",
		sequenceName = "RELATION_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
		generator = "RELATION_SEQ_GENERATOR")
	private Long id;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(nullable = false)
	private ExtendedDataElement lhsExtendedDataElement;
	
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH,
	        CascadeType.PERSIST })
	@JoinColumn(nullable = false)
	private ExtendedDataElement rhsExtendedDataElement;

	@ManyToOne
	@JoinColumn(name="relationop_id", referencedColumnName = "id", 
			nullable = false)
	private RelationOperator relationOperator;

	private Integer minf1s2;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private TimeUnit minf1s2TimeUnit;

	private Integer maxf1s2;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private TimeUnit maxf1s2TimeUnit;

	private Integer mins1f2;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private TimeUnit mins1f2TimeUnit;

	private Integer maxs1f2;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private TimeUnit maxs1f2TimeUnit;

	public Long getId() {
		return id;
	}

	public void setId(Long inId) {
		id = inId;
	}

	public ExtendedDataElement getLhsExtendedDataElement() {
		return lhsExtendedDataElement;
	}

	public void setLhsExtendedDataElement(ExtendedDataElement
		inLhsExtendedDataElement) {
		lhsExtendedDataElement = inLhsExtendedDataElement;
	}

	public ExtendedDataElement getRhsExtendedDataElement() {
		return rhsExtendedDataElement;
	}

	public void setRhsExtendedDataElement(ExtendedDataElement
		inRhsExtendedDataElement) {
		rhsExtendedDataElement = inRhsExtendedDataElement;
	}

	public RelationOperator getRelationOperator() {
		return relationOperator;
	}

	public void setRelationOperator(RelationOperator inOp) {
		relationOperator = inOp;
	}

	public Integer getMinf1s2() {
		return minf1s2;
	}

	public void setMinf1s2(Integer inMinf1s2) {
		minf1s2 = inMinf1s2;
	}

	public TimeUnit getMinf1s2TimeUnit() {
		return minf1s2TimeUnit;
	}

	public void setMinf1s2TimeUnit(TimeUnit inMinf1s2TimeUnit) {
		minf1s2TimeUnit = inMinf1s2TimeUnit;
	}

	public Integer getMaxf1s2() {
		return maxf1s2;
	}

	public void setMaxf1s2(Integer inMaxf1s2) {
		maxf1s2 = inMaxf1s2;
	}

	public TimeUnit getMaxf1s2TimeUnit() {
		return maxf1s2TimeUnit;
	}

	public void setMaxf1s2TimeUnit(TimeUnit inMaxf1s2TimeUnit) {
		maxf1s2TimeUnit = inMaxf1s2TimeUnit;
	}

	public Integer getMins1f2() {
		return mins1f2;
	}

	public void setMins1f2(Integer inMins1f2) {
		mins1f2 = inMins1f2;
	}

	public TimeUnit getMins1f2TimeUnit() {
		return mins1f2TimeUnit;
	}

	public void setMins1f2TimeUnit(TimeUnit inMins1f2TimeUnit) {
		mins1f2TimeUnit = inMins1f2TimeUnit;
	}

	public Integer getMaxs1f2() {
		return maxs1f2;
	}

	public void setMaxs1f2(Integer inMaxs1f2) {
		maxs1f2 = inMaxs1f2;
	}

	public TimeUnit getMaxs1f2TimeUnit() {
		return maxs1f2TimeUnit;
	}

	public void setMaxs1f2TimeUnit(TimeUnit inMaxs1f2TimeUnit) {
		maxs1f2TimeUnit = inMaxs1f2TimeUnit;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
