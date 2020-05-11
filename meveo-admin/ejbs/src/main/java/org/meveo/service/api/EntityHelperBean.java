/**
 * 
 */
package org.meveo.service.api;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.BusinessEntity;

/**
 * Generic business bean for helping dealing with entities
 * 
 * @author clement.bareth
 * @since 6.9.0
 * @version 6.9.0
 */
@Stateless
public class EntityHelperBean {
	
    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    /**
     * Find entities that reference a given class and ID
     * 
     * @param entityClass Class of the entity
     * @param <T> Type of the entity
     * @param id Record identifier
     * @return A concatinated list of entities (humanized classnames and their codes) E.g. Customer Account: first ca, second ca, third ca; Customer: first customer, second
     *         customer
     */
    @SuppressWarnings("rawtypes")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public <T> String findReferencedByEntities(Class<T> entityClass, Long id) {

        T referencedEntity = emWrapper.getEntityManager().getReference(entityClass, id);

        int totalMatched = 0;
        String matchedEntityInfo = null;
        Map<Class, List<Field>> classesAndFields = ReflectionUtils.getClassesAndFieldsOfType(entityClass);

        for (Entry<Class, List<Field>> classFieldInfo : classesAndFields.entrySet()) {

            boolean isBusinessEntity = BusinessEntity.class.isAssignableFrom(classFieldInfo.getKey());

            String sql = "select " + (isBusinessEntity ? "code" : "id") + " from " + classFieldInfo.getKey().getName() + " where ";
            boolean fieldAddedToSql = false;
            for (Field field : classFieldInfo.getValue()) {
                // For now lets ignore list type fields
                if (field.getType() == entityClass) {
                    sql = sql + (fieldAddedToSql ? " or " : " ") + field.getName() + "=:id";
                    fieldAddedToSql = true;
                }
            }

            if (fieldAddedToSql) {

                List entitiesMatched = emWrapper.getEntityManager().createQuery(sql).setParameter("id", referencedEntity).setMaxResults(10).getResultList();
                if (!entitiesMatched.isEmpty()) {

                    matchedEntityInfo = (matchedEntityInfo == null ? "" : matchedEntityInfo + "; ") + ReflectionUtils.getHumanClassName(classFieldInfo.getKey().getSimpleName())
                            + ": ";
                    boolean first = true;
                    for (Object entityIdOrCode : entitiesMatched) {
                        matchedEntityInfo += (first ? "" : ", ") + entityIdOrCode;
                        first = false;
                    }

                    totalMatched += entitiesMatched.size();
                }
            }

            if (totalMatched > 10) {
                break;
            }
        }

        return matchedEntityInfo;
    }
    
}
