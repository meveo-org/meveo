/**
 * 
 */
package org.meveo.model.dev;

import java.io.File;

import org.meveo.model.NotifiableEntity;
import org.meveo.model.git.GitRepository;

@NotifiableEntity
public class FileChangedEvent {

	private File file;
	
	private GitRepository gitRepository;
	
	private ChangeType type;

	
	/**
	 * Instantiates a new FileChangedEvent
	 *
	 * @param file
	 * @param type
	 */
	public FileChangedEvent(File file, ChangeType type, GitRepository gitRepository) {
		super();
		this.file = file;
		this.type = type;
		this.gitRepository = gitRepository;
	}
	
	/**
	 * @return the {@link #gitRepository}
	 */
	public GitRepository getGitRepository() {
		return gitRepository;
	}

	/**
	 * @return the {@link #file}
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the {@link #type}
	 */
	public ChangeType getType() {
		return type;
	}

	public static enum ChangeType {
		CREATED,
		MODIFIED,
		DELETED
	}
}
