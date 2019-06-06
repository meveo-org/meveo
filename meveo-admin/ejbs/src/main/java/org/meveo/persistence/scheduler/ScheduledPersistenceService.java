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

import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.persistence.CustomPersistenceService;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Set;

public abstract class ScheduledPersistenceService<T extends CustomPersistenceService> {

    private T storageService;

    @PostConstruct
    private void init(){
        storageService = getStorageService();
    }

    protected abstract T getStorageService();

    /**
     * Iterate over the persistence schedule and persist the provided entities
     *
     * @param configurationCode Repository coordinates
     * @param atomicPersistencePlan The schedule to follow
     * @throws BusinessException If the relation cannot be persisted
     */
    public void persist(String configurationCode, AtomicPersistencePlan atomicPersistencePlan) throws BusinessException, ELException {

        /* Iterate over persistence schedule and persist the node */

        SchedulerPersistenceContext context = new SchedulerPersistenceContext();
        final Iterator<Set<ItemToPersist>> iterator = atomicPersistencePlan.iterator();

        while (iterator.hasNext()) {

            for (ItemToPersist itemToPersist : iterator.next()) {
                if (itemToPersist instanceof SourceEntityToPersist) {

                    /* Node is a source node */
                    final SourceEntityToPersist sourceNode = (SourceEntityToPersist) itemToPersist;
                    storageService.addSourceEntityUniqueCrt(
                          configurationCode,
                          sourceNode.getRelationToPersist().getCode(),
                          sourceNode.getValues(),
                          sourceNode.getRelationToPersist().getEndEntityToPersist().getValues()
                    );

                } else if (itemToPersist instanceof EntityToPersist) {

                    /* Node is target or leaf node */
                    final EntityToPersist entityToPersist = (EntityToPersist) itemToPersist;
                    Set<EntityRef> persistedEntities = storageService.createOrUpdate(configurationCode, entityToPersist.getCode(), entityToPersist.getValues()).getPersistedEntities();
                    context.putNodeReferences(entityToPersist.getName(), persistedEntities);

                } else {

                    /* Item is a relation */
                    final RelationToPersist relationToPersist = (RelationToPersist) itemToPersist;
                    storageService.addCRTByValues(
                            configurationCode,
                            relationToPersist.getCode(),
                            relationToPersist.getValues(),
                            relationToPersist.getStartEntityToPersist().getValues(),
                            relationToPersist.getEndEntityToPersist().getValues()
                    );

                    Set<EntityRef> startPersistedEntities = context.getNodeReferences(relationToPersist.getStartEntityToPersist().getName());
                    Set<EntityRef> endPersistedEntities = context.getNodeReferences(relationToPersist.getEndEntityToPersist().getName());
                    if (startPersistedEntities.isEmpty() || endPersistedEntities.isEmpty()) {
                        // TODO: Make possible to have one node found by its id
                        storageService.addCRTByValues(
                              configurationCode,
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

                                storageService.addCRTByUuids(
                                      configurationCode,
                                      relationToPersist.getCode(),
                                      relationToPersist.getValues(),
                                      startEntityRef.getUuid(),
                                      endEntityRef.getUuid()
                                );
                            }
                        }
                    }
                }
            }

        }
    }
}
