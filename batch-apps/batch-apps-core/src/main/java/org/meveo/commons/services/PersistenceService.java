/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.commons.services;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.ProviderNotAllowedException;
import org.meveo.commons.utils.PaginationConfiguration;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.AuditableEntity;
import org.meveo.model.BaseEntity;
import org.meveo.model.EnableEntity;
import org.meveo.model.IEntity;
import org.meveo.model.IdentifiableEnum;
import org.meveo.model.UniqueEntity;
import org.meveo.model.admin.User;
import org.meveo.model.crm.Provider;

/**
 * Generic implementation that provides the default implementation for
 * persistence methods declared in the {@link IPersistenceService} interface.
 * 
 * 
 * @author Sebastien
 * @created 2011.03.30
 */
public abstract class PersistenceService<E extends IEntity> {

    protected final Class<E> entityClass;

    protected EntityManager em;

    protected Logger log = Logger.getLogger(PersistenceService.class);

    public PersistenceService(EntityManager em) {
        this();
        this.em = em;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private PersistenceService() {
        Class clazz = getClass();
        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
            clazz = clazz.getSuperclass();
        }
        Object o = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];

        if (o instanceof TypeVariable) {
            this.entityClass = (Class<E>) ((TypeVariable) o).getBounds()[0];
        } else {
            this.entityClass = (Class<E>) o;
        }
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public E findById(Long id) {
        return findById(id, false);
    }

    public E findById(Long id, List<String> fetchFields) {
        return findById(id, fetchFields, false);
    }

    public E findById(Long id, boolean refresh) {
        log.debug("start of find " + getEntityClass().getSimpleName() + " by id (id=" + id + ") ..");
        final Class<? extends E> productClass = getEntityClass();
        E e = em.find(productClass, id);
        if (refresh) {
            log.debug("refreshing loaded entity");
            em.refresh(e);
        }
        log.debug("end of find " + getEntityClass().getSimpleName() + " by id (id=" + id + "). Result found=" + (e != null) + ".");
        return e;
    }

    /**
     * @see org.meveo.service.base.local.IPersistenceService#findById(java.lang.Long,
     *      java.util.List, boolean)
     */
    @SuppressWarnings("unchecked")
    public E findById(Long id, List<String> fetchFields, boolean refresh) {
        log.debug("start of find " + getEntityClass().getSimpleName() + " by id (id=" + id + ") ..");
        final Class<? extends E> productClass = getEntityClass();
        StringBuilder queryString = new StringBuilder("from " + productClass.getName() + " a");
        if (fetchFields != null && !fetchFields.isEmpty()) {
            for (String fetchField : fetchFields) {
                queryString.append(" left join fetch a." + fetchField);
            }
        }
        queryString.append(" where a.id = :id");
        Query query = em.createQuery(queryString.toString());
        query.setParameter("id", id);

        E e = (E) query.getResultList().get(0);

        if (refresh) {
            log.debug("refreshing loaded entity");
            em.refresh(e);
        }
        log.debug("end of find " + getEntityClass().getSimpleName() + " by id (id=" + id + "). Result found=" + (e != null) + ".");
        return e;
    }

    public void remove(Long id, Provider provider) {
        E e = findById(id);
        if (e != null) {
            remove(e, provider);
        }
    }

    public void disable(Long id, User updater, Provider provider) {
        E e = findById(id);
        if (e instanceof EnableEntity) {
            ((EnableEntity) e).setDisabled(true);
            update(e, updater, provider);
        }
    }

    public void remove(E e, Provider provider) {
        checkProvider(e, provider);
        log.debug("start of remove " + getEntityClass().getSimpleName() + " entity (id=" + e.getId() + ") ..");
        em.remove(e);
        em.flush();
        log.debug("end of remove " + getEntityClass().getSimpleName() + " entity (id=" + e.getId() + ").");
    }

    public void remove(Set<Long> ids) {
        Query query = em.createQuery("delete from " + getEntityClass().getName() + " where id in (:ids)");
        query.setParameter("ids", ids);
        query.executeUpdate();
    }

    public void remove(Set<Long> ids, Provider provider) {
        Query query = em.createQuery("delete from " + getEntityClass().getName() + " where id in (:ids) and provider.id = :providerId");
        query.setParameter("ids", ids);
        query.setParameter("providerId", provider != null ? provider.getId() : null);
        query.executeUpdate();
    }

    public void update(E e, User updater, Provider provider) {
        log.debug("start of update " + getEntityClass().getSimpleName() + " entity (id=" + e.getId() + ") ..");
        if (e instanceof AuditableEntity) {
            if (updater != null) {
                ((AuditableEntity) e).updateAudit(updater);
            }
        }
        checkProvider(e, provider);
        em.merge(e);
        log.debug("end of update " + getEntityClass().getSimpleName() + " entity (id=" + e.getId() + ") ..");
    }

    public void create(E e, User creator, Provider provider) {
        log.debug("start of create " + e.getClass().getSimpleName() + " entity ..");
        if (e instanceof AuditableEntity) {
            if (creator != null) {
                ((AuditableEntity) e).updateAudit(creator);
            }
        }
        if (e instanceof BaseEntity && (((BaseEntity) e).getProvider() == null)) {
            ((BaseEntity) e).setProvider(provider);
        }
        em.persist(e);
        log.debug("end of create " + e.getClass().getSimpleName() + ". entity id=" + e.getId() + ".");
    }

    @SuppressWarnings("unchecked")
    public List<? extends E> list(Provider provider) {
        final Class<? extends E> entityClass = getEntityClass();
        QueryBuilder queryBuilder = new QueryBuilder(entityClass, "a", null);
        if (BaseEntity.class.isAssignableFrom(entityClass)) {
            queryBuilder.startOrClause();
            queryBuilder.addCriterionEntity("a.provider", provider);
            // queryBuilder.addSql("a.provider is null");
            queryBuilder.endOrClause();
        }
        Query query = queryBuilder.getQuery(em);
        return query.getResultList();
    }

    @SuppressWarnings({ "unchecked" })
    public List<? extends E> list(PaginationConfiguration config, Provider provider) {
        QueryBuilder queryBuilder = getQuery(config, provider);
        Query query = queryBuilder.getQuery(em);
        return query.getResultList();
    }

    public long count(PaginationConfiguration config, Provider provider) {
        QueryBuilder queryBuilder = getQuery(config, provider);
        return queryBuilder.count(em);
    }

    public long count(Provider provider) {
        final Class<? extends E> entityClass = getEntityClass();
        QueryBuilder queryBuilder = new QueryBuilder(entityClass, "a", null);
        if (BaseEntity.class.isAssignableFrom(entityClass)) {
            queryBuilder.startOrClause();
            queryBuilder.addCriterionEntity("a.provider", provider);
            // queryBuilder.addSql("a.provider is null");
            queryBuilder.endOrClause();
        }
        return queryBuilder.count(em);
    }

    /**
     * Creates query to filter entities according data provided in pagination
     * configuration.
     * 
     * @param config
     *            PaginationConfiguration data holding object
     * @return query to filter entities according pagination configuration data.
     */
    @SuppressWarnings("rawtypes")
    private QueryBuilder getQuery(PaginationConfiguration config, Provider provider) {

        final Class<? extends E> entityClass = getEntityClass();
        QueryBuilder queryBuilder = new QueryBuilder(entityClass, "a", config.getFetchFields());
        if (BaseEntity.class.isAssignableFrom(entityClass)) {
            queryBuilder.startOrClause();
            queryBuilder.addCriterionEntity("a.provider", provider);
            // queryBuilder.addSql("a.provider is null");
            queryBuilder.endOrClause();
        }
        Map<String, Object> filters = config.getFilters();
        if (filters != null) {
            if (!filters.isEmpty()) {
                for (String key : filters.keySet()) {
                    Object filter = filters.get(key);
                    if (filter != null) {
                        // if ranged search (from - to fields)
                        if (key.contains("fromRange-")) {
                            String parsedKey = key.substring(10);
                            if (filter instanceof Double) {
                                BigDecimal rationalNumber = new BigDecimal((Double) filter);
                                queryBuilder.addCriterion("a." + parsedKey, " >= ", rationalNumber, true);
                            } else if (filter instanceof Number) {
                                queryBuilder.addCriterion("a." + parsedKey, " >= ", filter, true);
                            } else if (filter instanceof Date) {
                                queryBuilder.addCriterionDateRangeFromTruncatedToDay("a." + parsedKey, (Date) filter);
                            }
                        } else if (key.contains("toRange-")) {
                            String parsedKey = key.substring(8);
                            if (filter instanceof Double) {
                                BigDecimal rationalNumber = new BigDecimal((Double) filter);
                                queryBuilder.addCriterion("a." + parsedKey, " <= ", rationalNumber, true);
                            } else if (filter instanceof Number) {
                                queryBuilder.addCriterion("a." + parsedKey, " <= ", filter, true);
                            } else if (filter instanceof Date) {
                                queryBuilder.addCriterionDateRangeToTruncatedToDay("a." + parsedKey, (Date) filter);
                            }
                        } else if (key.contains("list-")) {
                            // if searching elements from list
                            String parsedKey = key.substring(5);
                            queryBuilder.addSqlCriterion(":" + parsedKey + " in elements(a." + parsedKey + ")", parsedKey, filter);
                        }
                        // if not ranged search
                        else {
                            if (filter instanceof String) {
                                // if contains dot, that means join is needed
                                String filterString = (String) filter;
                                queryBuilder.addCriterionWildcard("a." + key, filterString, true);
                            } else if (filter instanceof Date) {
                                queryBuilder.addCriterionDateTruncatedToDay("a." + key, (Date) filter);
                            } else if (filter instanceof Number) {
                                queryBuilder.addCriterion("a." + key, " = ", filter, true);
                            } else if (filter instanceof Boolean) {
                                queryBuilder.addCriterion("a." + key, " is ", filter, true);
                            } else if (filter instanceof Enum) {
                                if (filter instanceof IdentifiableEnum) {
                                    String enumIdKey = new StringBuilder(key).append("Id").toString();
                                    queryBuilder.addCriterion("a." + enumIdKey, " = ", ((IdentifiableEnum) filter).getId(), true);
                                } else {
                                    queryBuilder.addCriterionEnum("a." + key, (Enum) filter);
                                }
                            } else if (BaseEntity.class.isAssignableFrom(filter.getClass())) {
                                queryBuilder.addCriterionEntity("a." + key, filter);
                            } else if (filter instanceof UniqueEntity) {
                                queryBuilder.addCriterionEntity("a." + key, filter);
                            }
                        }
                    }
                }
            }
        }
        queryBuilder.addPaginationConfiguration(config, "a");
        return queryBuilder;
    }

    /**
     * Check entity provider. If current provider is not same as entity provider
     * exception is thrown since different provider should not be allowed to
     * modify (update or delete) entity.
     */
    private void checkProvider(E e, Provider currentProvider) {
        if (currentProvider != null) {
            if (e instanceof BaseEntity) {
                Provider provider = ((BaseEntity) e).getProvider();
                boolean notSameProvider = !(provider != null && provider.getId().equals(currentProvider.getId()));
                log.debug("checkProvider  currentProvider id=" + currentProvider.getId() + ", entityprovider id=" + provider != null ? provider.getId() : null);
                if (notSameProvider) {
                    throw new ProviderNotAllowedException();
                }
            }
        }

    }
}