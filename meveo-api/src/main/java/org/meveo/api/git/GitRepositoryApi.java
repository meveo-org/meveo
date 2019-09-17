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

package org.meveo.api.git;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.git.GitRepositoryDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.git.GitRepository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.git.GitRepositoryService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API for managing git repositories
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
@Stateless
public class GitRepositoryApi extends BaseCrudApi<GitRepository, GitRepositoryDto> {

    @Inject
    private GitRepositoryService gitRepositoryService;

    public GitRepositoryApi() {
        super(GitRepository.class, GitRepositoryDto.class);
    }

    public List<GitRepositoryDto> list() {
        return gitRepositoryService.list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void remove(String code) throws BusinessException {
        final GitRepository repository = gitRepositoryService.findByCode(code);
        gitRepositoryService.remove(repository);
    }

    @Override
    public GitRepositoryDto toDto(GitRepository entity) {
        GitRepositoryDto dto = new GitRepositoryDto();
        dto.setReadingRoles(entity.getReadingRoles());
        dto.setWritingRoles(entity.getWritingRoles());
        dto.setRemoteOrigin(entity.getRemoteOrigin());
        dto.setRemoteUsername(entity.getDefaultRemoteUsername());
        dto.setRemotePassword(entity.getDefaultRemotePassword());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setMeveoRepository(entity.isMeveoRepository());
        dto.setCurrentBranch(entity.getCurrentBranch());
        dto.setBranches(entity.getBranches());
        return dto;
    }

    @Override
    public GitRepository fromDto(GitRepositoryDto dto) throws EntityDoesNotExistsException {
        GitRepository entity = new GitRepository();
        entity.setRemoteOrigin(dto.getRemoteOrigin());
        entity.setCode(dto.getCode());

        updateEntity(dto, entity);

        return entity;
    }

    @Override
    public IPersistenceService<GitRepository> getPersistenceService() {
        return gitRepositoryService;
    }

    @Override
    public boolean exists(GitRepositoryDto dto) {
        try {
            return find(dto.getCode()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public GitRepositoryDto find(String code) throws MeveoApiException, EntityDoesNotExistsException {
        return toDto(gitRepositoryService.findByCode(code));
    }

    @Override
    public GitRepository createOrUpdate(GitRepositoryDto dtoData) throws MeveoApiException, BusinessException {
        return exists(dtoData) ? update(dtoData) : create(dtoData);
    }

    public GitRepository create(GitRepositoryDto dtoData) throws BusinessException {
        final GitRepository repository = fromDto(dtoData);
        gitRepositoryService.create(repository);
        return repository;
    }

    public GitRepository update(GitRepositoryDto dto) throws BusinessException {
        final GitRepository entity = gitRepositoryService.findByCode(dto.getCode());

        if(dto.getRemoteOrigin() != null && entity.getRemoteOrigin() != null && !dto.getRemoteOrigin().equals(entity.getRemoteOrigin())) {
            throw new IllegalArgumentException("Cannot update remote origin of GitRepository " + dto.getCode());
        }

        updateEntity(dto, entity);

        gitRepositoryService.update(entity);

        return entity;
    }

    private void updateEntity(GitRepositoryDto dto, GitRepository entity) {
        entity.setReadingRoles(dto.getReadingRoles());
        entity.setWritingRoles(dto.getWritingRoles());
        entity.setDefaultRemoteUsername(dto.getRemoteUsername());
        entity.setDefaultRemotePassword(dto.getRemotePassword());
        entity.setDescription(dto.getDescription());
        entity.setMeveoRepository(dto.isMeveoRepository());
    }
}
