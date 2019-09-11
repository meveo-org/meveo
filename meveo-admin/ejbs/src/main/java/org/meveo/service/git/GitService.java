/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.service.git;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.git.GitRepository;
import org.meveo.service.base.BusinessService;

import javax.inject.Inject;

/**
 * Persistence class for GitRepository
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
public class GitService extends BusinessService<GitRepository> {

    @Inject
    private GitClient gitClient;

    /**
     * Remove the GitRepository entity along with the corresponding repository in file system
     * @param entity Repository to remove
     */
    @Override
    public void remove(GitRepository entity) throws BusinessException {
        gitClient.remove(entity);
        super.remove(entity);
    }

    /**
     * Create the GitRepository entity along with the corresponding repository in file system
     * @param entity Repository to create
     */
    @Override
    public void create(GitRepository entity) throws BusinessException {
        gitClient.create(entity);
        super.create(entity);
    }
}
