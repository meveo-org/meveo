/**
 * 
 */
package org.meveo.api.git;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.module.MeveoModuleApi;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Processed;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.dev.FileChangedEvent;
import org.meveo.model.dev.FileChangedEvent.ChangeType;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.GitRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 


@ApplicationScoped
public class GitRepositoryWatcher {

	@Inject
	private GitRepositoryService gitRepositoryService;

	@Inject
	@Processed
	private Event<FileChangedEvent> fileChangedEvent;
	
	@Inject
	private MeveoModuleApi moduleApi;
	
	@Inject
	private MeveoModuleService meveoModuleService;
	
	private static Logger log = LoggerFactory.getLogger(GitRepositoryWatcher.class);

	private Map<String, FileAlterationMonitor> monitors = new ConcurrentHashMap<>();

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		for (var repo : gitRepositoryService.list()) {
			if (repo.isDevMode()) {
				this.startWatch(repo);
			}
		}
	}
	
	public void onCreate(@Observes @Created GitRepository repo) {
		if (repo.isDevMode()) {
			startWatch(repo);
		}
	}
	
	public void onUpdate(@Observes @Updated GitRepository repo) {
		stopWatch(repo);
		if (repo.isDevMode()) {
			startWatch(repo);
		}
	}
	
	public void onDelete(@Observes @Removed GitRepository repo) {
		stopWatch(repo);
	}

	private void stopWatch(GitRepository gitRepository) {
		var monitor = monitors.remove(gitRepository.getCode());
		if (monitor != null) {
			try {
				monitor.stop();
				log.info("Stop monitoring {}", gitRepository.getCode());
			} catch (Exception e) {
				log.error("Failed to stop monitoring", e);
			}
		}
	}
	
	private void applyChanges(ChangeType type, File file, GitRepository gitRepository) {
		File directory = GitHelper.getRepositoryDir(null, gitRepository);
		String fileName = GitHelper.computeRelativePath(directory, file);
		MeveoModule module = this.meveoModuleService.findByCodeWithFetchEntities(gitRepository.getCode());
		log.info("{} detected on {} : {}", type, gitRepository.getCode(), fileName);
		
		Set<MeveoModuleItemDto> installItems = new HashSet<>();
		Set<MeveoModuleItemDto> updateItems = new HashSet<>();
		Set<MeveoModuleItem> deleteItems = new HashSet<>();


		if (type == ChangeType.CREATED) {
			moduleApi.computeItemsToAdd(installItems, gitRepository, directory, fileName);
		} else if (type == ChangeType.MODIFIED) {
			moduleApi.compteItemsToUpdate(updateItems, gitRepository, directory, fileName);
		} else if (type == ChangeType.DELETED) {
			moduleApi.computeItemsToDelete(deleteItems, directory, fileName, module.getCode());
		}
		
		if (!installItems.isEmpty() || !updateItems.isEmpty() || !deleteItems.isEmpty()) {
			if (module != null) {
				try {
					moduleApi.applyChanges(module, installItems, updateItems, deleteItems);
				}  catch (Exception e) {
					log.error("Failed to apply changes to module {}", module.getCode(), e);
				}
			}
		}
	}

	private void startWatch(GitRepository gitRepository) {
		File directory = GitHelper.getRepositoryDir(null, gitRepository);
		FileAlterationObserver observer = new FileAlterationObserver(directory, new GitRepositoryFileFilter(gitRepository));
		
		FileAlterationMonitor monitor = new FileAlterationMonitor(500);
		
		FileAlterationListener listener = new FileAlterationListenerAdaptor() {
		    @Override
		    public void onFileCreate(File file) {
		    	fileChangedEvent.fireAsync(new FileChangedEvent(file, ChangeType.CREATED, gitRepository));
		    	applyChanges(ChangeType.CREATED, file, gitRepository);
		    }

		    @Override
		    public void onFileDelete(File file) {
		    	fileChangedEvent.fireAsync(new FileChangedEvent(file, ChangeType.DELETED, gitRepository));
		    	applyChanges(ChangeType.DELETED, file, gitRepository);
		    }

		    @Override
		    public void onFileChange(File file) {
		    	fileChangedEvent.fireAsync(new FileChangedEvent(file, ChangeType.MODIFIED, gitRepository));
		    	applyChanges(ChangeType.MODIFIED, file, gitRepository);
		    }
		};
		
		observer.addListener(listener);
		monitor.addObserver(observer);
		
		try {
			monitor.start();
			monitors.put(gitRepository.getCode(), monitor);
			log.info("Start monitoring {}", gitRepository.getCode());
		} catch (Exception e1) {
			log.error("Failed to start monitoring", e1);
		}
		
	}

}
