package org.meveo.service.custom;

import javax.ejb.Stateless;

import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.service.base.BusinessService;

import java.util.List;


@Stateless
public class CustomEntityCategoryService extends BusinessService<CustomEntityCategory> {
    /**
     * Get a list of custom entity categories
     *
     * @return A list of custom entity categories
     */
    public List<CustomEntityCategory> getCustomEntityCategories() {
        return getEntityManager().createNamedQuery("CustomEntityCategory.getCustomEntityCategories", CustomEntityCategory.class).getResultList();
    }
}
