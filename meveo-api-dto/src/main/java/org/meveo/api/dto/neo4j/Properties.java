package org.meveo.api.dto.neo4j;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("completName_IDX")
    @Expose
    private String completNameIDX;
    @SerializedName("completName")
    @Expose
    private String completName;
    @SerializedName("updateDate")
    @Expose
    private Long internalUpdateDate;
    @SerializedName("internal_active")
    @Expose
    private String internalActive;
    @SerializedName("person_key")
    @Expose
    private String personKey;
    @SerializedName("creationDate_IDX")
    @Expose
    private String creationDateIDX;
    @SerializedName("companyType_IDX")
    @Expose
    private String companyTypeIDX;
    @SerializedName("companyType")
    @Expose
    private String companyType;
    @SerializedName("companyName")
    @Expose
    private String companyName;
    @SerializedName("identificationNumber")
    @Expose
    private String identificationNumber;
    @SerializedName("company_key")
    @Expose
    private String companyKey;
    @SerializedName("creationDate")
    @Expose
    private String creationDate;
    @SerializedName("companyName_IDX")
    @Expose
    private String companyNameIDX;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("streetName")
    @Expose
    private String streetName;
    @SerializedName("countryCode")
    @Expose
    private String countryCode;
    @SerializedName("address_IDX")
    @Expose
    private String addressIDX;
    @SerializedName("sourceUrl")
    @Expose
    private String sourceUrl;
    @SerializedName("sourceUrl_IDX")
    @Expose
    private String sourceUrlIDX;
    @SerializedName("sourceName_IDX")
    @Expose
    private String sourceNameIDX;
    @SerializedName("sourceName")
    @Expose
    private String sourceName;
    @SerializedName("sourceCode")
    @Expose
    private String sourceCode;
    @SerializedName("activityCode")
    @Expose
	private String activityCode;
	@SerializedName("activity")
    @Expose
	private String activity;
	@SerializedName("internal_connectorName")
    @Expose
    private String connectorName;
    @SerializedName("totalResult")
    @Expose
    private String totalResult;
    @SerializedName("score")
    @Expose
    private String score;
    @SerializedName("level")
    @Expose
    private String level;
    @SerializedName("queryCategoryName_IDX")
    @Expose
    private String queryCategoryNameIDX;
    @SerializedName("queryCategory_key")
    @Expose
    private String queryCategory_key;
    @SerializedName("coefficient")
    @Expose
    private String coefficient;
    @SerializedName("queryCategoryName")
    @Expose
    private String queryCategoryName;
    @SerializedName("maxScore")
    @Expose
    private String maxScore;
    @SerializedName("nbrResults")
    @Expose
    private String nbrResults;
    @SerializedName("query")
    @Expose
    private String query;
    @SerializedName("query_key")
    @Expose
    private String query_key;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("queryEl")
    @Expose
    private String queryEl;
    @SerializedName("exclusionQuery")
    @Expose
    private String exclusionQuery;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCompletNameIDX() {
        return completNameIDX;
    }

    public void setCompletNameIDX(String completNameIDX) {
        this.completNameIDX = completNameIDX;
    }

    public String getCompletName() {
        return completName;
    }

    public void setCompletName(String completName) {
        this.completName = completName;
    }

    public Long getInternalUpdateDate() {
        return internalUpdateDate;
    }

    public void setInternalUpdateDate(Long internalUpdateDate) {
        this.internalUpdateDate = internalUpdateDate;
    }

    public String getInternalActive() {
        return internalActive;
    }

    public void setInternalActive(String internalActive) {
        this.internalActive = internalActive;
    }

    public String getPersonKey() {
        return personKey;
    }

    public void setPersonKey(String personKey) {
        this.personKey = personKey;
    }

    public String getCreationDateIDX() {
        return creationDateIDX;
    }

    public void setCreationDateIDX(String creationDateIDX) {
        this.creationDateIDX = creationDateIDX;
    }

    public String getCompanyTypeIDX() {
        return companyTypeIDX;
    }

    public void setCompanyTypeIDX(String companyTypeIDX) {
        this.companyTypeIDX = companyTypeIDX;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(String companyKey) {
        this.companyKey = companyKey;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCompanyNameIDX() {
        return companyNameIDX;
    }

    public void setCompanyNameIDX(String companyNameIDX) {
        this.companyNameIDX = companyNameIDX;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddressIDX() {
        return addressIDX;
    }

    public void setAddressIDX(String addressIDX) {
        this.addressIDX = addressIDX;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceUrlIDX() {
        return sourceUrlIDX;
    }

    public void setSourceUrlIDX(String sourceUrlIDX) {
        this.sourceUrlIDX = sourceUrlIDX;
    }

    public String getSourceNameIDX() {
        return sourceNameIDX;
    }

    public void setSourceNameIDX(String sourceNameIDX) {
        this.sourceNameIDX = sourceNameIDX;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

	public String getActivityCode() {
		return activityCode;
	}

	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

    public String getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(String totalResult) {
        this.totalResult = totalResult;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getQueryCategoryNameIDX() {
        return queryCategoryNameIDX;
    }

    public void setQueryCategoryNameIDX(String queryCategoryNameIDX) {
        this.queryCategoryNameIDX = queryCategoryNameIDX;
    }

    public String getQueryCategory_key() {
        return queryCategory_key;
    }

    public void setQueryCategory_key(String queryCategory_key) {
        this.queryCategory_key = queryCategory_key;
    }

    public String getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(String coefficient) {
        this.coefficient = coefficient;
    }

    public String getQueryCategoryName() {
        return queryCategoryName;
    }

    public void setQueryCategoryName(String queryCategoryName) {
        this.queryCategoryName = queryCategoryName;
    }

    public String getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(String maxScore) {
        this.maxScore = maxScore;
    }

    public String getNbrResults() {
        return nbrResults;
    }

    public void setNbrResults(String nbrResults) {
        this.nbrResults = nbrResults;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery_key() {
        return query_key;
    }

    public void setQuery_key(String query_key) {
        this.query_key = query_key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQueryEl() {
        return queryEl;
    }

    public void setQueryEl(String queryEl) {
        this.queryEl = queryEl;
    }

    public String getExclusionQuery() {
        return exclusionQuery;
    }

    public void setExclusionQuery(String exclusionQuery) {
        this.exclusionQuery = exclusionQuery;
    }
}
