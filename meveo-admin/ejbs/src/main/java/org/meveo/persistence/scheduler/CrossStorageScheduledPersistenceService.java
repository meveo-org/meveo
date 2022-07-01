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

import org.meveo.event.qualifier.Updated;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.persistence.CrossStorageService;
import org.meveo.persistence.DBStorageTypeService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class CrossStorageScheduledPersistenceService extends OrderedPersistenceService<CrossStorageService> {
	
	private static ConcurrentHashMap<String, List<DBStorageType>> storageTypesCache = new ConcurrentHashMap<>();

    @Inject
    private CrossStorageService service;
    
	@Inject
	private DBStorageTypeService dbStorageTypeService;

    @Override
    protected CrossStorageService getStorageService() {
        return service;
    }

	@Override
	protected List<DBStorageType> getStorageTypes(String templateCode) {
		return storageTypesCache.computeIfAbsent(templateCode, dbStorageTypeService::findTemplateStorages);
	}
	
	public void onCetUpdate(@Observes @Updated CustomEntityTemplate cet) {
		storageTypesCache.remove(cet.getCode());
	}
	
	public void onCrtUpdate(@Observes @Updated CustomRelationshipTemplate crt) {
		storageTypesCache.remove(crt.getCode());
	}
}
