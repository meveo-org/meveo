package org.meveocrm.services.dwh;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;

import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.dwh.MeasurableQuantity;
import org.meveo.neo4j.base.Neo4jConnectionProvider;
import org.meveo.service.base.BusinessService;
import org.neo4j.driver.v1.*;

@Stateless
public class MeasurableQuantityService extends BusinessService<MeasurableQuantity> {
	
    @Inject
    private Neo4jConnectionProvider neo4jSessionFactory;

    public Object[] executeMeasurableQuantitySQL(MeasurableQuantity mq) {
        try {
            Query q = getEntityManager().createNativeQuery(mq.getSqlQuery());
            return (Object[]) q.getSingleResult();
        } catch (Exception e) {
            log.error("failed run {} - {}", mq.getSqlQuery(), e.getMessage());
        }
        return null;
    }
    
    public Object[] executeMeasurableQuantityCYPHER(MeasurableQuantity mq) {
//        try {
//        	// Begin transaction
//        	Session session = neo4jSessionFactory.getSession();
//        	Transaction tx = session.beginTransaction();
//        	StatementResult result = tx.run(mq.getCypherQuery());
//            final List<Record> recordList = result.list();
//            final Object[] = recordList.stream().map(Record::new).
//            Query q = getEntityManager().createNativeQuery(mq.getSqlQuery());
//            return (Object[]) q.getSingleResult();
//        } catch (Exception e) {
//            log.error("failed run {} - {}", mq.getCypherQuery(), e.getMessage());
//        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<MeasurableQuantity> listToBeExecuted(Date date) {
        QueryBuilder queryBuilder = new QueryBuilder(MeasurableQuantity.class, "a", null);
        queryBuilder.addCriterionDateRangeToTruncatedToDay("last_measure_date", date);
        Query query = queryBuilder.getQuery(getEntityManager());
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<MeasurableQuantity> listEditable() {
        QueryBuilder queryBuilder = new QueryBuilder(MeasurableQuantity.class, "a", null);
        queryBuilder.addBooleanCriterion("editable", true);
        Query query = queryBuilder.getQuery(getEntityManager());
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<MeasurableQuantity> listByCode(String code) {
        QueryBuilder queryBuilder = new QueryBuilder(MeasurableQuantity.class, "a", null);
        queryBuilder.addCriterion("code", "=", code, false);
        Query query = queryBuilder.getQuery(getEntityManager());
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<MeasurableQuantity> listByCodeAndDim(String measurableQuantityCode, String dimension1Filter, String dimension2Filter, String dimension3Filter,
            String dimension4Filter) {

        QueryBuilder queryBuilder = new QueryBuilder(MeasurableQuantity.class, "a", null);
        queryBuilder.addCriterion("code", "=", measurableQuantityCode, false);
        if (!StringUtils.isBlank(dimension1Filter)) {
            queryBuilder.addCriterion("dimension1", "=", dimension1Filter, false);
        }
        if (!StringUtils.isBlank(dimension2Filter)) {
            queryBuilder.addCriterion("dimension2", "=", dimension2Filter, false);
        }
        if (!StringUtils.isBlank(dimension3Filter)) {
            queryBuilder.addCriterion("dimension3", "=", dimension3Filter, false);
        }
        if (!StringUtils.isBlank(dimension4Filter)) {
            queryBuilder.addCriterion("dimension4", "=", dimension4Filter, false);
        }
        Query query = queryBuilder.getQuery(getEntityManager());
        return query.getResultList();
    }
}
