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

package org.meveo.persistence;

import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.persistence.scheduler.EntityRef;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface CustomPersistenceService {

    void addSourceEntityUniqueCrt(String configurationCode, String relationCode, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException;

    PersistenceActionResult createOrUpdate(String configurationCode, String entityCode, Map<String, Object> values) throws BusinessException;

    void addCRTByValues(String configurationCode, String relationCode, Map<String, Object> relationValues, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException;

    void addCRTByUuids(String configurationCode, String relationCode, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws ELException, BusinessException;

    default List<String> getTrustedUuids(Set<EntityRef> createdEntityReferences) {
        return createdEntityReferences.stream()
                .filter(EntityRef::isTrusted)
                .map(EntityRef::getUuid)
                .collect(Collectors.toList());
    }
}
