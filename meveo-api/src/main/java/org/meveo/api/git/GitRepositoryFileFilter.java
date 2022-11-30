/**
 * 
 */
package org.meveo.api.git;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.model.git.GitRepository;
import org.meveo.service.git.GitHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ClementBareth
 * @since 
 * @version
 */
public class GitRepositoryFileFilter implements FileFilter {
	
	private static Logger LOG = LoggerFactory.getLogger(GitRepositoryFileFilter.class);
	
	private List<File> watchedDirectories = new ArrayList<>();

	public GitRepositoryFileFilter(GitRepository repo) {
		var rootRepo = GitHelper.getRepositoryDir(null, repo);
		
		for (var dir : repo.getWatchedDirectories()) {
			var watchedDir = new File(rootRepo, dir);
			if (!watchedDir.exists()) {
				LOG.warn("Watched repository {} does not exist", watchedDir);
			} else {
				watchedDirectories.add(watchedDir);
			}
		}
	}
	
	@Override
	public boolean accept(File pathname) {
		return watchedDirectories.stream().anyMatch(dir -> MeveoFileUtils.isFileInDirectory(pathname, dir) || MeveoFileUtils.isFileInDirectory(dir, pathname));
	}

}
