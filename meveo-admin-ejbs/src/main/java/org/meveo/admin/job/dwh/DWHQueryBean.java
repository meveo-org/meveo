package org.meveo.admin.job.dwh;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.time.DateUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.job.logging.JobLoggingInterceptor;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.interceptor.PerformanceInterceptor;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.dwh.MeasurableQuantity;
import org.meveo.model.dwh.MeasuredValue;
import org.meveo.model.dwh.MeasurementPeriodEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.service.job.JobExecutionService;
import org.meveocrm.services.dwh.MeasurableQuantityService;
import org.meveocrm.services.dwh.MeasuredValueService;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.slf4j.Logger;

@Stateless
public class DWHQueryBean {

    @Inject
    private MeasurableQuantityService mqService;

    @Inject
    private MeasuredValueService mvService;

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    @Inject
    private Logger log;

    @Inject
    private JobExecutionService jobExecutionService;

    // iso 8601 date and datetime format
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Interceptors({JobLoggingInterceptor.class, PerformanceInterceptor.class})
    void executeQuery(JobExecutionResultImpl result, String parameter) {

        String measurableQuantityCode = parameter;
        Date toDate = new Date();

        if (!StringUtils.isBlank(parameter)) {
            if (parameter.indexOf("to=") > 0) {
                String s = parameter.substring(parameter.indexOf("to=") + 3);
                if (s.indexOf(";") > 0) {
                    measurableQuantityCode = parameter.substring(parameter.indexOf(";") + 1);
                    Date parsedDate = org.meveo.model.shared.DateUtils.guessDate(s.substring(0, s.indexOf(";")), "yyyy-MM-dd");
                    if (parsedDate != null) {
                        toDate = parsedDate;
                    }
                } else {
                    if (parameter.indexOf(";") > 0) {
                        measurableQuantityCode = parameter.substring(0, parameter.indexOf(";"));
                    } else {
                        measurableQuantityCode = null;
                    }
                }

            }
        }
        log.debug("measurableQuantityCode={}, toDate={}", measurableQuantityCode, toDate);

        List<MeasurableQuantity> mqList = new ArrayList<>();
        if (StringUtils.isBlank(measurableQuantityCode)) {
            mqList = mqService.listToBeExecuted(new Date());
        } else {
            MeasurableQuantity mq = mqService.findByCode(measurableQuantityCode);
            if (mq == null) {
                result.registerError("Cannot find measurable quantity with code " + measurableQuantityCode);
                return;
            }
            mqList.add(mq);
        }
        result.setNbItemsToProcess(mqList.size());
        for (MeasurableQuantity mq : mqList) {

            if (!jobExecutionService.isJobRunningOnThis(result.getJobInstance().getId())) {
                break;
            }

            if (StringUtils.isBlank(mq.getSqlQuery())) {
                result.registerError("Measurable quantity with code " + measurableQuantityCode + " has no SQL query set.");
                log.info("Measurable quantity with code {} has no SQL query set.", measurableQuantityCode);
                continue;
            }

            mq.increaseMeasureDate();
            result.registerSucces();
            try {
                for (MeasuredValue measuredValue : getMeasuredValues(mq, toDate)) {
                    if (measuredValue.getId() == null) {
                        mvService.create(measuredValue);
                    }else {
                    	mvService.update(measuredValue);
                    }
                }
            } catch (Exception e) {
                result.registerError("Measurable quantity with code " + measurableQuantityCode + " contain invalid SQL query: " + e.getMessage());
            }
        }
    }

    private List<MeasuredValue> getMeasuredValues(MeasurableQuantity mq, Date toDate) throws Exception {
        EntityManager em = emWrapper.getEntityManager();
        try {

            if (mq.getLastMeasureDate() == null) {
                mq.setLastMeasureDate(mq.getPreviousDate(toDate).toInstant());
            }

            List<Object> results = new ArrayList<>();   // List of query results

            if (mq.getNextMeasureDate().isBefore(toDate.toInstant())) {

                // Execute and get results of sql query if defined
                if (!StringUtils.isBlank(mq.getSqlQuery())) {
                    results.addAll(sqlResults(mq, em));
                }

                // Execute and get result of cypher query if defined
                if (!StringUtils.isBlank(mq.getCypherQuery())) {
                    results.addAll(cypherResults(mq));
                }
            }

            return extractMeasuredValues(em, mq, results);

        } catch (Exception e) {
            log.error("Measurable quantity with code " + mq.getCode() + " contain invalid SQL query", e);
            throw new Exception(e);
        }
    }

    private List<Object> cypherResults(MeasurableQuantity mq) throws BusinessException {
        List<Object> results = new ArrayList<>();   // List of query results
        String cypherQuery = formatQuery(mq, mq.getCypherQuery());  // Cypher query template to execute
        log.debug("resolve query:{}, nextMeasureDate={}, lastMeasureDate={}", mq.getCypherQuery(), mq.getNextMeasureDate(), mq.getLastMeasureDate());
        log.debug("execute query:{}", cypherQuery);

        /* Start transaction */
        String neo4jUrl = ParamBean.getInstance().getProperty("neo4j.host", "localhost");
        String neo4jLogin = ParamBean.getInstance().getProperty("neo4j.login", "neo4j");
        String neo4jPassword = ParamBean.getInstance().getProperty("neo4j.password", "meveo");
        Driver driver = GraphDatabase.driver("bolt://" + neo4jUrl, AuthTokens.basic(neo4jLogin, neo4jPassword));
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();

        try {
            // Execute query
            final StatementResult statementResult = transaction.run(cypherQuery);
            List<Record> recordList = statementResult.list();
            recordList.forEach(record -> {
                Object[] recordObject = {
                        record.get(0),  // Date or Value
                        record.get(1),  // Value or Dimension 1 
                        record.get(2),  // Dimension 1 or 2 or null
                        record.get(3),  // Dimension 2 or 3 or null
                        record.get(4),  // Dimension 3 or 4 or null
                        record.get(5),  // Dimension 4 or null
                };
                results.add(recordObject);
            });
            transaction.success();
        } catch (Exception e) {
            throw new BusinessException(e);
        } finally {
            session.close();
            transaction.close();
        }
        return results;
    }

    private List<Object> sqlResults(MeasurableQuantity mq, EntityManager em) {
        List<Object> results = new ArrayList<>();   			// List of query results
        String sqlQuery = formatQuery(mq, mq.getSqlQuery());	// SQL Query template to execute
        log.debug("resolve query:{}, nextMeasureDate={}, lastMeasureDate={}", mq.getSqlQuery(), mq.getNextMeasureDate(), mq.getLastMeasureDate());
        log.debug("execute query:{}", sqlQuery);
        Query query = em.createNativeQuery(sqlQuery);
        results.addAll(query.getResultList());
        return results;
    }

    private List<MeasuredValue> extractMeasuredValues(EntityManager em, MeasurableQuantity mq, List<Object> results) {
        List<MeasuredValue> measuredValues = new ArrayList<>();
        for (Object res : results) {
            MeasurementPeriodEnum mve = (mq.getMeasurementPeriod() != null) ? mq.getMeasurementPeriod() : MeasurementPeriodEnum.DAILY;
            BigDecimal value;
            Instant date = mq.getLastMeasureDate();
            String dimension1 = mq.getDimension1();
            String dimension2 = mq.getDimension2();
            String dimension3 = mq.getDimension3();
            String dimension4 = mq.getDimension4();
            if (res instanceof Object[]) {
                Object[] resTab = (Object[]) res;
                value = new BigDecimal("" + resTab[0]);
                int i = 1;
                if (resTab.length > i) {
                    try {
                        date = (Instant) resTab[1];
                        i++;
                    } catch (Exception e) {}
                    if (resTab.length > i) {
                        dimension1 = resTab[i] == null ? "" : resTab[i].toString();
                        i++;
                        if (resTab.length > i) {
                            dimension2 = resTab[i] == null ? "" : resTab[i].toString();
                            i++;
                            if (resTab.length > i) {
                                dimension3 = resTab[i] == null ? "" : resTab[i].toString();
                                i++;
                                if (resTab.length > i) {
                                    dimension4 = resTab[i] == null ? "" : resTab[i].toString();
                                }
                            }
                        }
                    }
                }
            } else {
                value = new BigDecimal("" + res);
            }
            date = date instanceof Instant ? ((Instant) date).truncatedTo(ChronoUnit.DAYS)
            								: DateUtils.truncate(date, Calendar.DAY_OF_MONTH).toInstant();
            
            MeasuredValue mv = mvService.getByDate(em, date, mve, mq);
            if (mv == null) {
                mv = new MeasuredValue();
            }
            mv.setMeasurableQuantity(mq);
            mv.setMeasurementPeriod(mve);
            mv.setValue(value);
            mv.setDate(date);
            mv.setDimension1(dimension1);
            mv.setDimension2(dimension2);
            mv.setDimension3(dimension3);
            mv.setDimension4(dimension4);
            measuredValues.add(mv);
        }
        return measuredValues;
    }

    private String formatQuery(MeasurableQuantity mq, String queryStr) {
        queryStr = queryStr.replaceAll("#\\{date\\}", df.format(mq.getLastMeasureDate()));
        queryStr = queryStr.replaceAll("#\\{dateTime\\}", tf.format(mq.getLastMeasureDate()));
        queryStr = queryStr.replaceAll("#\\{nextDate\\}", df.format(mq.getNextMeasureDate()));
        queryStr = queryStr.replaceAll("#\\{nextDateTime\\}", tf.format(mq.getNextMeasureDate()));
        return queryStr;
    }
}
