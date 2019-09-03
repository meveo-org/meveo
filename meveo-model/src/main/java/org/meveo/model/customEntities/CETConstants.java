package org.meveo.model.customEntities;

/**
 * @author Rachid
 * @author Clément
 */
public class CETConstants {


    public static final String CET_UPDATE_DATE_FIELD = "updateDate";
    public static final String CET_ERROR_STATUS = "ERROR";

    public static final String STATEMENT_ENTITY_NODE_CODE_FIELD = "nodeCode";
    public static final String STATEMENT_ENTITY_SELECTED_SERVICES_FIELD = "selectedServices";
    public static final String STATEMENT_ENTITY_QUERIES_FIELD = "queries";
    public static final String STATEMENT_ENTITY_FILTERS_FIELD = "filters";
    public static final String STATEMENT_ENTITY_SEARCH_TYPE_FIELD = "searchType";
    public static final String STATEMENT_ENTITY_LOCAL_SEARCH_FIELD = "localSearch";
    public static final String STATEMENT_ENTITY_MAX_RESULTS_FIELD = "maxResults";
    public static final String STATEMENT_ENTITY_TOTAL_RESULTS_FIELD = "totalResults";


    public static final String PARAMETER_OPERATOR_SEPARATOR = "_";
    public static final String MATRIX_OPERATOR = "operator";
    public static final String MATRIX_FIELD_NAM = "fieldName";
    public static final String MATRIX_TYPE = "fieldType";
    public static final String MATRIX_COLUMN_NAMES = "operator/fieldName/fieldType";
    public static final String MATRIX_KEY_SEPARATOR = "|";
    public static final String MAP_KEY = "key";
    public static final String MATRIX = "matrix";

    public static final String QUERY_CATEGORY_CODE = "QueryCategory";
    public static final String QUERY_CATEGORY_QUERIES_Field = "queries";
    public static final String QUERY_CATEGORY_COEFFICIENT_Field = "coefficient";
    public static final String QUERY_CATEGORY_MAX_SCORE_Field = "maxScore";
    public static final String QUERY_CATEGORY_LEVEL_Field = "level";
    public static final String QUERY_CATEGORY_DETAILED_DESCRIPTION = "detailedDescription";
    public static final String QUERY_CATEGORY_CONNECTORS = "connectors";
    public static final String QUERY_CATEGORY_IS_SUPER_CATEGORY = "isSuperCategory";
    public static final String QUERY_CATEGORY_ATTACHED_CATEGORIES = "attachedCategories";
    public static final String QUERY_CATEGORY_WEBSITE = "website";
    public static final String QUERY_CATEGORY_TOTAL_PAGE_SCORES_Field = "totalPageScores";
    public static final String QUERY_CATEGORY_TOTAL_SEVERITY_INDEXES_Field = "totalSeverityIndexes";

    public static final String QUERY_CODE = "Query";
    public static final String QUERY_TAGS_Field = "tags";
    public static final String QUERY_QUERY_EL_Field = "queryEl";
    public static final String QUERY_QUERY_EXCLUSION_QUERY_Field = "exclusionQuery";
    public static final String QUERY_TAG_CODE = "QueryTag";
    public static final String QUERY_TAG_KEYWORDS_Field = "keywords";

    public static final String QUERY_QUERY_POSITIVE_Field = "positive";
    public static final String QUERY_QUERY_SEVERETY_INDEX_Field = "severityIndex";
    public static final String QUERY_QUERY_TARGET_INVOLVEMENT_Field = "targetInvolvement";
    public static final String COMPANY_NAME_Field = "companyName";
    public static final String COMPANY_INTERNAL_STATUS_FIELD = "internal_status";
    public static final String COMPANY_STATUS_FIELD = "status";
    public static final String COUNTRY_CODE_Field = "countryCode";
    public static final String COMPANY_NODE_ID_Field = "companyNodeId";
    public static final String CET_CODE_Field = "cetCode";


    public static final String USER_MISSION_TYPE_FIELD = "missionType";

    public static final String CONNECTOR_API_CODE_FIELD = "apiCode";
    public static final String CONNECTOR_API_URL_FIELD = "apiUrl";
    public static final String CONNECTOR_API_HTTP_METHOD_FIELD = "httpMethod";
    public static final String CONNECTOR_API_JSONATA_EXP_FIELD = "jsonataExp";
    public static final String CONNECTOR_API_EXTRACTOR_FIELD = "extractor";
    public static final String ACTIVE_STATUS = "ACTIVE";

    public static final String SERVICE_TEMPLATE_RELIABILITY_INDEX_NOT_RT = "reliabilityIndexForNotreliableTarget";
    public static final String SERVICE_TEMPLATE_RELIABILITY_INDEX_RT = "reliabilityIndexForReliableTarget";

    public static final String SERVICE_TEMPLATE_RELIABILITY_INDEX = "reliabilityIndex";

    public static final String SERVICE_TEMPLATE_IS_GET_DETAILS_ALLOWED = "isGetDetailsAllowed";
    //*************End CET fields************************/


    public static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    public static final int RANDOM_STRING_LENGTH = 10;

    public static final String USER_ENTITY_SUBSCRIPTION_FIELD = "subscriptionCode";
    public static final String USER_LOCALISATION_FIELD = "localisation";
    public static final String USER_MISSION_TYPES_FIELD = "missionTypes";

    public static final String SEARCH_TYPES_ENTITY_CODE = "SearchType";
    public static final String SEARCH_TYPES_CET_FIELDS_FIELD = "cetFields";
    public static final String SEARCH_TYPES_CRT_FIELDS_FIELD = "crtFields";
    public static final String SEARCH_TYPES_CET_FIELD_TYPE_FIELD = "type";
    public static final String CRT_SEARCH_CRT_FIELDS_FIELD = "crtFields";
    public static final String SEARCH_TYPES_PICTO_CODE_FIELD = "pictoCode";

    public static final String DEFAULT_DATE_PATTERN = "dd/MM/yyyy";

    public static final String COMPLET_DATE_PATTERN = "dd/MM/yyyy hh:mm:ss";


    public static final String DATE_PATTERN_REPORT_NAME = "ddMMYYY";

    public static final String NOT_CAPTURED = "Not_Captured";
    public static final String SEPARATOR_LIST = "|";
    public static final String SEPARATOR_MAC_ADDRESS = ",";
    public static final String SEPARATOR_FILTER_MAC_ADDRESS = ";";

    public static final String SEMI_COLON = ";";
    public static final String NEW_LINE_SEPARATOR = "\n";

    public static final String UNRELEVANT = "Unrelevant";
    public static final String VALIDATED = "Validated";
    public static final String YES = "YES";
    public static final String NO = "NO";

    public static final String MODEL_TECHNICAL_LETTER_TEMPLATE = "Model_Technical_Letter";
    public static final String UNDER_SCORE = "_";
    public static final String FR_COUNTRY_CODE = "FR";
    public static final String MONTH = "month";
    public static final String MONTHS = "months";
    public static final String SEPARATOR = ", ";
    public static final String EN_COUNTRY_CODE = "EN";
    public static final String PT_COUNTRY_CODE = "PT";
    public static final String SPAIN_COUNTRY = "Spain";
    public static final String REDACT_DOMAIN = "redact";
    public static final String CET_WIN_FIELD = "win";
}
