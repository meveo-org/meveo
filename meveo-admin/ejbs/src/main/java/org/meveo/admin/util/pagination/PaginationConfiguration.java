/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.admin.util.pagination;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.QueryParam;

import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.primefaces.model.SortOrder;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author Clément Bareth
 * @author Andrius
 * @author Edward P. Legaspi(edward.legaspi@manaty.net)
 */
public class PaginationConfiguration implements Serializable {

    private static final long serialVersionUID = -2750287256630146681L;
    
    @QueryParam("firstRow")
    private Integer firstRow;

    @QueryParam("numberOfRows")
    private Integer numberOfRows;

    @QueryParam("randomize")
    private boolean randomize;

    /** Full text search filter. Mutually exclusive with filters attribute. fullTextFilter has priority */
    @QueryParam("fullTextFilter")
    private String fullTextFilter;

    /** Search filters (key = field name, value = search pattern or value). */
    private Map<String, Object> filters;
    
    private Map<String, String> sortOrdering;

    /**
     * Fields that needs to be fetched when selecting (like lists or other entities).
     */
    @QueryParam("fetch")
    private List<String> fetchFields;

    @QueryParam("sortBy")
    private String sortField;

    @QueryParam("sortOrder")
    private SortOrder ordering;

    private String graphQlQuery;
    
    /**
     * Super type of the data to query
     */
    @QueryParam("superType")
    private String superType;
    
    /**
     * Fields that belongs to the super type
     */
    @QueryParam("superTypeFields")
    private Set<String> superTypeFields;
    
    /**
     *
     * @param sortField Field to sort by
     * @param sortOrder Sort order
     */
    public PaginationConfiguration(String sortField, SortOrder sortOrder) {
        this(null, null, null, null, null, sortField, sortOrder, null);
    }

    public PaginationConfiguration() {
        this(null, null, null, null, null, null, null, null);
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    /**
     * @param firstRow Number of the first row to retrieve
     * @param numberOfRows Number of rows to retrieve
     * @param filters Search criteria to apply
     * @param fullTextFilter full text filter
     * @param fetchFields Lazy loaded fields to fetch
     * @param sortField Field to sort by
     * @param sortOrder Sort order
     */
    public PaginationConfiguration(Integer firstRow, Integer numberOfRows, Map<String, Object> filters, String fullTextFilter, List<String> fetchFields, String sortField,
                                   SortOrder sortOrder) {
    	
        this(firstRow, numberOfRows, filters, fullTextFilter, fetchFields, sortField, sortOrder, null);
    }

    /**
     * Constructor
     *
     * @param firstRow Number of the first row to retrieve
     * @param numberOfRows Number of rows to retrieve
     * @param filters Search criteria
     * @param fullTextFilter full text filter.
     * @param fetchFields Lazy loaded fields to fetch
     * @param sortField Field to sort by
     * @param sortOrder Sort order
     * @param sortOrdering sort ordering.
     */
    public PaginationConfiguration(Integer firstRow, Integer numberOfRows, Map<String, Object> filters, String fullTextFilter, List<String> fetchFields, String sortField,
                                   SortOrder sortOrder, Map<String, String> sortOrdering) {
        this.firstRow = firstRow;
        this.numberOfRows = numberOfRows;
        this.filters = filters;
        this.fullTextFilter = fullTextFilter;
        this.fetchFields = fetchFields;
        this.sortField = sortField;
        this.ordering = sortOrder;
        this.sortOrdering = sortOrdering;
    }

    /**
     * Constructor
     *
     * @param filters Search criteria
     */
    public PaginationConfiguration(Map<String, Object> filters) {
        this.filters = filters;
    }

    public PaginationConfiguration(PaginationConfiguration paginationConfiguration) {
        if (paginationConfiguration == null) {
            paginationConfiguration = new PaginationConfiguration();
        }

        if (paginationConfiguration.fetchFields != null) {
            this.fetchFields = new ArrayList<>(paginationConfiguration.fetchFields);
        }

        if (paginationConfiguration.filters != null) {
            this.filters = new HashMap<>(paginationConfiguration.filters);
        }

        if (paginationConfiguration.sortOrdering != null) {
            this.sortOrdering = new HashMap<>(paginationConfiguration.sortOrdering);
        }

        this.firstRow = paginationConfiguration.firstRow;
        this.fullTextFilter = paginationConfiguration.fullTextFilter;
        this.graphQlQuery = paginationConfiguration.graphQlQuery;
        this.numberOfRows = paginationConfiguration.numberOfRows;
        this.ordering = paginationConfiguration.ordering;
        this.randomize = paginationConfiguration.randomize;
        this.sortField = paginationConfiguration.sortField;
    }



    /**
     * Constructor
     *
     * @param filters Search criteria
     * @param sortField Field to sort by
     * @param sortOrder Sort order
     */
    public PaginationConfiguration(Map<String, Object> filters, String sortField, SortOrder sortOrder) {
        this.filters = filters;
        this.sortField = sortField;
        this.ordering = sortOrder;
    }

    public String getGraphQlQuery() {
        return graphQlQuery;
    }

    public void setGraphQlQuery(String graphQlQuery) {
        this.graphQlQuery = graphQlQuery;
    }

    public Integer getFirstRow() {
        return firstRow;
    }

    public Integer getNumberOfRows() {
        return numberOfRows;
    }

    public String getSortField() {
        return sortField;
    }

    public Map<String, String> getOrderings() {
        return sortOrdering;
    }

    public SortOrder getOrdering() {
        return ordering;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public String getFullTextFilter() {
        return fullTextFilter;
    }

    public List<String> getFetchFields() {
        return fetchFields;
    }

    public void setFetchFields(List<String> fetchFields) {
        this.fetchFields = fetchFields;
    }

    public boolean isSorted() {
        return ordering != null && sortField != null && sortField.trim().length() != 0;
    }

    public boolean isAscendingSorting() {
        return ordering != null && ordering == SortOrder.ASCENDING;
    }

    public void setFirstRow(Integer firstRow) {
        this.firstRow = firstRow;
    }

    public void setNumberOfRows(Integer numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public void setFullTextFilter(String fullTextFilter) {
        this.fullTextFilter = fullTextFilter;
    }

    public Map<String, String> getSortOrdering() {
        return sortOrdering;
    }

    public void setSortOrdering(Map<String, String> sortOrdering) {
        this.sortOrdering = sortOrdering;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public void setOrdering(SortOrder ordering) {
        this.ordering = ordering;
    }

    @Override
    public String toString() {
        return String.format("PaginationConfiguration [firstRow=%s, numberOfRows=%s, fullTextFilter=%s, filters=%s, sortOrdering=%s, fetchFields=%s, sortField=%s, ordering=%s]",
                firstRow, numberOfRows, fullTextFilter, filters, sortOrdering, fetchFields, sortField, ordering);
    }

    @JsonSetter("filters")
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }
    
	public Map<String, Object> getFiltersNullSafe() {
		
		if (filters == null) {
			filters = new HashMap<String, Object>();
		}

		return filters;
	}

	/**
	 * @return the {@link #superType}
	 */
	public String getSuperType() {
		return superType;
	}

	/**
	 * @param superType the superType to set
	 */
	public void setSuperType(String superType) {
		this.superType = superType;
	}

	/**
	 * @return the {@link #superTypeFields}
	 */
	public Collection<String> getSuperTypeFields() {
		return superTypeFields;
	}

	/**
	 * @param superTypeFields the superTypeFields to set
	 */
	public void setSuperTypeFields(Set<String> superTypeFields) {
		this.superTypeFields = superTypeFields;
	}
	
	@QueryParam("filters")
	public void setFilters(String serializedFilters) {
		if(serializedFilters != null) {
			this.filters = JacksonUtil.fromString(serializedFilters, GenericTypeReferences.MAP_STRING_OBJECT);
		}
	}
	
}