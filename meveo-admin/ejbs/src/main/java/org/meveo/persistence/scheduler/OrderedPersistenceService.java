/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.persistence.scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageTransaction;
import org.meveo.persistence.CustomPersistenceService;
import org.meveo.persistence.DBStorageTypeService;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.storage.RepositoryService;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 * @param <T> implementing service
 */
public abstract class OrderedPersistenceService<T extends CustomPersistenceService> {

    @Inject 
    private RepositoryService repositoryService;
    
    @Inject
    private CustomFieldInstanceService customFieldInstanceService;

    @Inject
    private CustomFieldsCacheContainerProvider cacheContainerProvider;
    
	@Inject
	private CrossStorageTransaction crossStorageTx;
	
    private T storageService;

    @PostConstruct
    private void init(){
        storageService = getStorageService();
    }

    protected abstract T getStorageService();
    
    protected abstract List<DBStorageType> getStorageTypes(String templateCode);

    /**
     * Iterate over the persistence schedule and persist the provided entities
     *
     * @param repositoryCode Repository coordinates
     * @param atomicPersistencePlan The schedule to follow
     * @throws BusinessException If the relation cannot be persisted
     */
    public List<PersistedItem> persist(String repositoryCode, AtomicPersistencePlan atomicPersistencePlan) throws BusinessException, ELException, IOException, BusinessApiException, EntityDoesNotExistsException {

        /* Iterate over persistence schedule and persist the node */

        SchedulerPersistenceContext context = new SchedulerPersistenceContext();
        final Iterator<Set<ItemToPersist>> iterator = atomicPersistencePlan.iterator();
        List<PersistedItem> persistedItems = new ArrayList<>();

        Repository repository = repositoryService.findByCode(repositoryCode);

        while (iterator.hasNext()) {

            for (ItemToPersist itemToPersist : iterator.next()) {
            	
            	List<DBStorageType> storages = getStorageTypes(itemToPersist.getCode());
            			
            	crossStorageTx.beginTransaction(repository, storages);

                PersistenceActionResult result = null;

                if (itemToPersist instanceof SourceEntityToPersist) {

                    /* Node is a source node */
                    final SourceEntityToPersist sourceNode = (SourceEntityToPersist) itemToPersist;
                    result = storageService.addSourceEntityUniqueCrt(
                    	  repository,
                          sourceNode.getRelationToPersist().getCode(),
                          sourceNode.getValues(),
                          sourceNode.getRelationToPersist().getEndEntityToPersist().getValues()
                    );

                } else if (itemToPersist instanceof EntityToPersist) {

                    /* Node is target or leaf node */
                    final EntityToPersist entityToPersist = (EntityToPersist) itemToPersist;
                    
                    CustomEntityInstance cei = new CustomEntityInstance();
                    cei.setCode((String) entityToPersist.getValues().get("code"));
                    cei.setCetCode(entityToPersist.getCode());
                    cei.setCet(cacheContainerProvider.getCustomEntityTemplate(cei.getCetCode()));
                    customFieldInstanceService.setCfValues(cei, entityToPersist.getCode(), itemToPersist.getValues());

                    result = storageService.createOrUpdate(repository, cei);
                    Set<EntityRef> persistedEntities = result.getPersistedEntities();
                    context.putNodeReferences(entityToPersist.getName(), persistedEntities);

                } else {

                    /* Item is a relation */
                    final RelationToPersist relationToPersist = (RelationToPersist) itemToPersist;

                    Set<EntityRef> startPersistedEntities = context.getNodeReferences(relationToPersist.getStartEntityToPersist().getName());
                    Set<EntityRef> endPersistedEntities = context.getNodeReferences(relationToPersist.getEndEntityToPersist().getName());
                    if (startPersistedEntities.isEmpty() || endPersistedEntities.isEmpty()) {
                        // TODO: Make possible to have one node found by its id
                        result = storageService.addCRTByValues(
                              repository,
                              relationToPersist.getCode(),
                              relationToPersist.getValues(),
                              relationToPersist.getStartEntityToPersist().getValues(),
                              relationToPersist.getEndEntityToPersist().getValues()
                        );
                    } else {
                        for (EntityRef startEntityRef : startPersistedEntities) {
                            if (!startEntityRef.isTrusted()) {
                                continue;
                            }

                            for (EntityRef endEntityRef : endPersistedEntities) {
                                if (!endEntityRef.isTrusted()) {
                                    continue;
                                }

                                result = storageService.addCRTByUuids(
                                	  repository,
                                      relationToPersist.getCode(),
                                      relationToPersist.getValues(),
                                      startEntityRef.getUuid(),
                                      endEntityRef.getUuid()
                                );
                            }
                        }
                    }
                }
                
                crossStorageTx.commitTransaction(repository, storages);
                
                if(result != null) {
                	persistedItems.add(new PersistedItem(itemToPersist, result.getBaseEntityUuid()));
                } else {
                	//LOGGER.warn("No persistence result from {}", itemToPersist);
                }
            }
            
        }

        return persistedItems;
    }
}
