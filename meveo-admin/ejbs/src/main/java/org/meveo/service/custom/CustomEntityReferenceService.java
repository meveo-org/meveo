package org.meveo.service.custom;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.model.customEntities.CustomEntityReference;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.base.PersistenceService;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hien Bach
 */
@Stateless
public class CustomEntityReferenceService extends PersistenceService<CustomEntityReference> {

    /**
     * Get a list of custom entity templates for cache
     *
     * @return A list of custom entity templates
     */
    public List<CustomEntityTemplate> getCETFromReference() {
        List<CustomEntityTemplate> list = new ArrayList<>();
        List<CustomEntityReference> entityReferences = getEntityManager().createNamedQuery("CustomEntityReference.getCER", CustomEntityReference.class).getResultList();
        if (CollectionUtils.isNotEmpty(entityReferences)) {
            for (CustomEntityReference customEntityReference : entityReferences) {
                list.add(customEntityReference.getCustomEntityTemplate());
            }
        }
        return list;
    }

    /**
     * Get a list of custom entity templates for cache
     *
     * @return A list of custom entity templates
     */
    public boolean checkExistingCET(Long cetId) {
        boolean existed = false;
        List<CustomEntityReference> entityReferences = getEntityManager().createNamedQuery("CustomEntityReference.getExistingCET", CustomEntityReference.class)
                .setParameter("cetId", cetId).getResultList();
        if (CollectionUtils.isNotEmpty(entityReferences)) {
            existed = true;
        }
        return existed;
    }

    /**
     * Get a list of custom entity templates for cache
     *
     * @return A list of custom entity templates
     */
    public boolean checkExistingForUpdateCET(Long cetId, Long id) {
        boolean existed = false;
        List<CustomEntityReference> entityReferences = getEntityManager().createNamedQuery("CustomEntityReference.getExistingUpdateCET", CustomEntityReference.class)
                .setParameter("id", id)
                .setParameter("cetId", cetId).getResultList();
        if (CollectionUtils.isNotEmpty(entityReferences)) {
            existed = true;
        }
        return existed;
    }

    @Override
    public List<CustomEntityReference> findByCodeLike(String entityCode) {
        return super.findByCodeLike(entityCode);
    }
}
