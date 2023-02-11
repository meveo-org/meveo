/**
 * 
 */
package org.meveo.service.base;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.commons.utils.FilteredQueryBuilder;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.BaseEntity;
import org.meveo.model.IEntity;
import org.meveo.model.IdentifiableEnum;
import org.meveo.model.UniqueEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryBuilderHelper {
   private static Logger logger = LoggerFactory.getLogger(QueryBuilderHelper.class);

   /**
    * Entity list search parameter name - parameter's value contains entity class
    */
   public static String SEARCH_ATTR_TYPE_CLASS = "type_class";
   /**
    * Entity list search parameter value - parameter's value is null
    */
   public static String SEARCH_IS_NULL = "IS_NULL";
   /**
    * Entity list search parameter value - parameter's value is not null
    */
   public static String SEARCH_IS_NOT_NULL = "IS_NOT_NULL";
   /**
    * Entity list search parameter criteria - wildcard Or
    */
   public static String SEARCH_WILDCARD_OR = "wildcardOr";

   /**
    * Entity list search parameter name - parameter's value contains sql statement
    */
   public static String SEARCH_SQL = "SQL";

   /**
    * Entity list search parameter name - parameter's value contains filter name
    */
   public static String SEARCH_FILTER = "$FILTER";

   /**
    * Entity list search parameter name - parameter's value contains filter
    * parameters
    */
   public static String SEARCH_FILTER_PARAMETERS = "$FILTER_PARAMETERS";

   /**
    * Entity list search parameter criteria - just like wildcardOr but Ignoring
    * case
    */
   public static String SEARCH_WILDCARD_OR_IGNORE_CAS = "wildcardOrIgnoreCase";

   @SuppressWarnings({ "rawtypes", "unchecked" })
   public static QueryBuilder getQuery(PaginationConfiguration config, Class<?> entityClass) {		

   	// final Class<? extends E> entityClass = getEntityClass();

   	final String entityAlias = "a";

   	Map<String, Object> filters = config.getFilters();

   	QueryBuilder queryBuilder = new QueryBuilder(entityClass, entityAlias, config.getFetchFields());

   	if (filters != null && !filters.isEmpty()) {

   		if (filters.containsKey(SEARCH_FILTER)) {
   			Filter filter = (Filter) filters.get(SEARCH_FILTER);
   			Map<CustomFieldTemplate, Object> parameterMap = (Map<CustomFieldTemplate, Object>) filters.get(SEARCH_FILTER_PARAMETERS);
   			queryBuilder = new FilteredQueryBuilder(filter, parameterMap, false, false);
   		} else {

   			for (String key : filters.keySet()) {

   				Object filterValue = filters.get(key);
   				if (filterValue == null) {
   					continue;
   				}					

   				// Key format is: condition field1 field2 or condition-field1-field2-fieldN
   				// example: "ne code", condition=ne, fieldName=code, fieldName2=null
   				String[] fieldInfo = key.split(" ");
   				String condition = fieldInfo.length == 1 ? null : fieldInfo[0];
   				String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1];
   				String fieldName2 = fieldInfo.length == 3 ? fieldInfo[2] : null;

   				String[] fields = null;
   				if (condition != null) {
   					fields = Arrays.copyOfRange(fieldInfo, 1, fieldInfo.length);
   				}

   				// if ranged search - field value in between from - to values. Specifies "from"
   				// value: e.g value<=field.value
   				if ("fromRange".equals(condition)) {
   					if (filterValue instanceof Double) {
   						BigDecimal rationalNumber = new BigDecimal((Double) filterValue);
   						queryBuilder.addCriterion("a." + fieldName, " >= ", rationalNumber, true);
   					} else if (filterValue instanceof Number) {
   						queryBuilder.addCriterion("a." + fieldName, " >= ", filterValue, true);
   					} else if (filterValue instanceof Date) {
   						queryBuilder.addCriterionDateRangeFromTruncatedToDay("a." + fieldName, ((Date) filterValue).toInstant());
   					} else if (filterValue instanceof Instant) {
   						queryBuilder.addCriterionDateRangeFromTruncatedToDay("a." + fieldName, (Instant) filterValue);
   					}

   					// if ranged search - field value in between from - to values. Specifies "to"
   					// value: e.g field.value<=value
   				} else if ("toRange".equals(condition)) {
   					if (filterValue instanceof Double) {
   						BigDecimal rationalNumber = new BigDecimal((Double) filterValue);
   						queryBuilder.addCriterion("a." + fieldName, " <= ", rationalNumber, true);
   					} else if (filterValue instanceof Number) {
   						queryBuilder.addCriterion("a." + fieldName, " <= ", filterValue, true);
   					} else if (filterValue instanceof Date) {
   						queryBuilder.addCriterionDateRangeToTruncatedToDay("a." + fieldName, ((Date) filterValue).toInstant());
   					} else if (filterValue instanceof Instant) {
   						queryBuilder.addCriterionDateRangeToTruncatedToDay("a." + fieldName, (Instant) filterValue);
   					}

   					// Value is in field value (list)
   				} else if ("list".equals(condition)) {
   					String paramName = queryBuilder.convertFieldToParam(fieldName);
   					queryBuilder.addSqlCriterion(":" + paramName + " in elements(a." + fieldName + ")", paramName, filterValue);

   					// Field value is in value (list)
   				} else if ("inList".equals(condition) || "not-inList".equals(condition)) {

   					boolean isNot = "not-inList".equals(condition);

   					Field field = ReflectionUtils.getField(entityClass, fieldName);
   					Class<?> fieldClassType = field.getType();

   					// Searching for a list inside a list field requires to join it first as
   					// collection member
   					if (Collection.class.isAssignableFrom(fieldClassType)) {

   						String paramName = queryBuilder.convertFieldToParam(fieldName);
   						String collectionItem = queryBuilder.convertFieldToCollectionMemberItem(fieldName);

   						// this worked at first, but now complains about distinct clause, so switched to
   						// EXISTS clause instead.
   						// queryBuilder.addCollectionMember(fieldName);
   						// queryBuilder.addSqlCriterion(collectionItem + " IN (:" + paramName + ")",
   						// paramName, filterValue);

   						String inListAlias = collectionItem + "Alias";
   						queryBuilder.addSqlCriterion(
   								" exists (select " + inListAlias + " from " + entityClass.getName() + " " + inListAlias + ",IN (" + inListAlias + "." + fieldName + ") as "
   										+ collectionItem + " where " + inListAlias + "=a and " + collectionItem + (isNot ? " NOT " : "") + " IN (:" + paramName + "))",
   								paramName, filterValue);

   					} else {
   						if (filterValue instanceof String) {
   							queryBuilder.addSql("a." + fieldName + (isNot ? " NOT " : "") + " IN (" + filterValue + ")");
   						} else if (filterValue instanceof Collection) {
   							String paramName = queryBuilder.convertFieldToParam(fieldName);
   							queryBuilder.addSqlCriterion("a." + fieldName + (isNot ? " NOT " : "") + " IN (:" + paramName + ")", paramName, filterValue);
   						}
   					}

   					// Search by an entity type
   				} else if (SEARCH_ATTR_TYPE_CLASS.equals(fieldName)) {
   					if (filterValue instanceof Collection && !((Collection) filterValue).isEmpty()) {
   						List classes = new ArrayList<Class>();
   						for (Object classNameOrClass : (Collection) filterValue) {
   							if (classNameOrClass instanceof Class) {
   								classes.add(classNameOrClass);
   							} else {
   								try {
   									classes.add(Class.forName((String) classNameOrClass));
   								} catch (ClassNotFoundException e) {
   									logger.error("Search by a type will be ignored - unknown class {}", classNameOrClass);
   								}
   							}
   						}

   						if (condition == null) {
   							queryBuilder.addSqlCriterion("type(a) in (:typeClass)", "typeClass", classes);
   						} else if ("ne".equalsIgnoreCase(condition)) {
   							queryBuilder.addSqlCriterion("type(a) not in (:typeClass)", "typeClass", classes);
   						}

   					} else if (filterValue instanceof Class) {
   						if (condition == null) {
   							queryBuilder.addSqlCriterion("type(a) = :typeClass", "typeClass", filterValue);
   						} else if ("ne".equalsIgnoreCase(condition)) {
   							queryBuilder.addSqlCriterion("type(a) != :typeClass", "typeClass", filterValue);
   						}

   					} else if (filterValue instanceof String) {
   						try {
   							if (condition == null) {
   								queryBuilder.addSqlCriterion("type(a) = :typeClass", "typeClass", Class.forName((String) filterValue));
   							} else if ("ne".equalsIgnoreCase(condition)) {
   								queryBuilder.addSqlCriterion("type(a) != :typeClass", "typeClass", Class.forName((String) filterValue));
   							}
   						} catch (ClassNotFoundException e) {
   							logger.error("Search by a type will be ignored - unknown class {}", filterValue);
   						}
   					}

   					// The value is in between two field values
   				} else if ("minmaxRange".equals(condition)) {
   					if (filterValue instanceof Double) {
   						BigDecimal rationalNumber = new BigDecimal((Double) filterValue);
   						queryBuilder.addCriterion("a." + fieldName, " <= ", rationalNumber, false);
   						queryBuilder.addCriterion("a." + fieldName2, " >= ", rationalNumber, false);
   					} else if (filterValue instanceof Number) {
   						queryBuilder.addCriterion("a." + fieldName, " <= ", filterValue, false);
   						queryBuilder.addCriterion("a." + fieldName2, " >= ", filterValue, false);
   					}
   					if (filterValue instanceof Date) {
   						Date value = (Date) filterValue;
   						Calendar c = Calendar.getInstance();
   						c.setTime(value);
   						int year = c.get(Calendar.YEAR);
   						int month = c.get(Calendar.MONTH);
   						int date = c.get(Calendar.DATE);
   						c.set(year, month, date, 0, 0, 0);
   						value = c.getTime();
   						queryBuilder.addCriterion("a." + fieldName, "<=", value, false);
   						queryBuilder.addCriterion("a." + fieldName2, ">=", value, false);
   					}

   					// The value is in between two field values with either them being optional
   				} else if ("minmaxOptionalRange".equals(condition)) {

   					String paramName = queryBuilder.convertFieldToParam(fieldName);

   					String sql = "((a." + fieldName + " IS NULL and a." + fieldName2 + " IS NULL) or (a." + fieldName + "<=:" + paramName + " and :" + paramName + "<a."
   							+ fieldName2 + ") or (a." + fieldName + "<=:" + paramName + " and a." + fieldName2 + " IS NULL) or (a." + fieldName + " IS NULL and :" + paramName
   							+ "<a." + fieldName2 + "))";
   					queryBuilder.addSqlCriterionMultiple(sql, paramName, filterValue);

   					// The value range is overlapping two field values with either them being
   					// optional
   				} else if ("overlapOptionalRange".equals(condition)) {

   					String paramNameFrom = queryBuilder.convertFieldToParam(fieldName);
   					String paramNameTo = queryBuilder.convertFieldToParam(fieldName2);

   					String sql = "(( a." + fieldName + " IS NULL and a." + fieldName2 + " IS NULL) or  ( a." + fieldName + " IS NULL and a." + fieldName2 + ">:" + paramNameFrom
   							+ ") or (a." + fieldName2 + " IS NULL and a." + fieldName + "<:" + paramNameTo + ") or (a." + fieldName + " IS NOT NULL and a." + fieldName2
   							+ " IS NOT NULL and ((a." + fieldName + "<=:" + paramNameFrom + " and :" + paramNameFrom + "<a." + fieldName2 + ") or (:" + paramNameFrom + "<=a."
   							+ fieldName + " and a." + fieldName + "<:" + paramNameTo + "))))";

   					if (filterValue.getClass().isArray()) {
   						queryBuilder.addSqlCriterionMultiple(sql, paramNameFrom, ((Object[]) filterValue)[0], paramNameTo, ((Object[]) filterValue)[1]);
   					} else if (filterValue instanceof List) {
   						queryBuilder.addSqlCriterionMultiple(sql, paramNameFrom, ((List) filterValue).get(0), paramNameTo, ((List) filterValue).get(1));
   					}

   					// Any of the multiple field values wildcard or not wildcard match the value (OR
   					// criteria)
   				} else if ("likeCriterias".equals(condition)) {

   					queryBuilder.startOrClause();
   					if (filterValue instanceof String) {
   						String filterString = (String) filterValue;
   						for (String field : fields) {
   							queryBuilder.addCriterionWildcard("a." + field, filterString, true);
   						}
   					}
   					queryBuilder.endOrClause();

   					// Any of the multiple field values wildcard match the value (OR criteria) - a
   					// diference from "likeCriterias" is that wildcard will be appended to the value
   					// automatically
   				} else if (SEARCH_WILDCARD_OR.equals(condition)) {
   					queryBuilder.startOrClause();
   					for (String field : fields) {
   						String filterValueAsStr = (String) filterValue;
   						queryBuilder.addSql("upper(a." + field + ") like '%" + filterValueAsStr.toUpperCase() + "%'");
   					}
   					queryBuilder.endOrClause();

   					// Search by additional Sql clause with specified parameters
   				} else if (SEARCH_SQL.equals(key)) {
   					if (filterValue.getClass().isArray()) {
   						String additionalSql = (String) ((Object[]) filterValue)[0];
   						Object[] additionalParameters = Arrays.copyOfRange(((Object[]) filterValue), 1, ((Object[]) filterValue).length);
   						queryBuilder.addSqlCriterionMultiple(additionalSql, additionalParameters);
   					} else {
   						queryBuilder.addSql((String) filterValue);
   					}

   				} else {
   					if (filterValue instanceof String && SEARCH_IS_NULL.equals(filterValue)) {
   						Field field = ReflectionUtils.getField(entityClass, fieldName);
   						Class<?> fieldClassType = field.getType();

   						if (Collection.class.isAssignableFrom(fieldClassType)) {
   							queryBuilder.addSql("a." + fieldName + " is empty ");
   						} else {
   							queryBuilder.addSql("a." + fieldName + " is null ");
   						}

   					} else if (filterValue instanceof String && SEARCH_IS_NOT_NULL.equals(filterValue)) {
   						Field field = ReflectionUtils.getField(entityClass, fieldName);
   						Class<?> fieldClassType = field.getType();

   						if (Collection.class.isAssignableFrom(fieldClassType)) {

   							queryBuilder.addSql("a." + fieldName + " is not empty ");
   						} else {
   							queryBuilder.addSql("a." + fieldName + " is not null ");
   						}

   					} else if (filterValue instanceof String) {
   						if ("moduleBelonging".equals(key) && "Meveo".equals(filterValue)) {
   							continue;
   						}

   						if ("moduleBelonging".equals(key)) {
   							String jpqlFilterByModule = "EXISTS (FROM MeveoModule m JOIN MeveoModuleItem mi ON m.id = mi.meveoModule WHERE mi.itemClass = '" + entityClass.getName() + "' AND mi.itemCode = " + entityAlias + ".code AND m.code = :moduleCode)";
   							queryBuilder.addSqlCriterion(jpqlFilterByModule, "moduleCode", filterValue);
   						} else {

   							// if contains dot, that means join is needed
   							String filterString = (String) filterValue;
   							boolean wildcard = (filterString.contains("*"));
   							if (wildcard) {
   								queryBuilder.addCriterionWildcard("a." + fieldName, filterString, true, "ne".equals(condition));
   							} else {
   								queryBuilder.addCriterion("a." + fieldName, "ne".equals(condition) ? " != " : " = ", filterString, true);
   							}
   						}

   					} else if (filterValue instanceof Date) {
   						queryBuilder.addCriterionDateTruncatedToDay("a." + fieldName, ((Date) filterValue).toInstant());

   					} else if (filterValue instanceof Instant) {
   						queryBuilder.addCriterionDateTruncatedToDay("a." + fieldName, (Instant) filterValue);

   					} else if (filterValue instanceof Number) {
   						queryBuilder.addCriterion("a." + fieldName, "ne".equals(condition) ? " != " : " = ", filterValue, true);

   					} else if (filterValue instanceof Boolean) {
   						queryBuilder.addCriterion("a." + fieldName, "ne".equals(condition) ? " not is" : " is ", filterValue, true);

   					} else if (filterValue instanceof Enum) {
   						if (filterValue instanceof IdentifiableEnum) {
   							String enumIdKey = fieldName + "Id";
   							queryBuilder.addCriterion("a." + enumIdKey, "ne".equals(condition) ? " != " : " = ", ((IdentifiableEnum) filterValue).getId(), true);
   						} else {
   							queryBuilder.addCriterionEnum("a." + fieldName, (Enum) filterValue, "ne".equals(condition) ? " != " : " = ");
   						}

   					} else if (BaseEntity.class.isAssignableFrom(filterValue.getClass())) {
   						queryBuilder.addCriterionEntity("a." + fieldName, filterValue, "ne".equals(condition) ? " != " : " = ");

   					} else if (filterValue instanceof UniqueEntity || filterValue instanceof IEntity) {
   						queryBuilder.addCriterionEntity("a." + fieldName, filterValue, "ne".equals(condition) ? " != " : " = ");

   					} else if (filterValue instanceof List) {
   						queryBuilder.addSqlCriterion("a." + fieldName + ("ne".equals(condition) ? " not in  " : " in ") + ":" + fieldName, fieldName, filterValue);
   					}
   				}
   			}
   		}
   	}

   	if (filters != null && filters.containsKey("$FILTER")) {
   		Filter filter = (Filter) filters.get("$FILTER");
   		queryBuilder.addPaginationConfiguration(config, filter.getPrimarySelector().getAlias());
   	} else {
   		queryBuilder.addPaginationConfiguration(config, "a");
   	}

   	return queryBuilder;
   }
}
