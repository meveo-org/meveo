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

package org.meveo.event.qualifier.git;

import java.util.List;
import java.util.Set;

import org.eclipse.jgit.diff.DiffEntry;
import org.meveo.model.git.GitRepository;

public class CommitEvent {

    private final GitRepository gitRepository;
    private final Set<String> modifiedFiles;
    private final List<DiffEntry> diffs;

    public CommitEvent(GitRepository gitRepository, Set<String> modifiedFiles, List<DiffEntry> diffs) {
        this.gitRepository = gitRepository;
        this.modifiedFiles = modifiedFiles;
        this.diffs = diffs;
    }

    public GitRepository getGitRepository() {
        return gitRepository;
    }

    public Set<String> getModifiedFiles() {
        return modifiedFiles;
    }

	/**
	 * @return the {@link #diffs}
	 */
	public List<DiffEntry> getDiffs() {
		return diffs;
	}
    
}
