/**
 * 
 */
package org.meveo.api.module;

import java.util.ArrayList;
import java.util.List;

import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.model.module.MeveoModule;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class ModuleInstallResult {

	private int nbSkipped;
	private int nbOverwritten;
	private int nbAdded;
	private List<MeveoModuleItemDto> installedItems = new ArrayList<>();
	private MeveoModule installedModule;
	
	public MeveoModule getInstalledModule() {
		return installedModule;
	}

	public void setInstalledModule(MeveoModule installedModule) {
		this.installedModule = installedModule;
	}

	public int getNbSkipped() {
		return nbSkipped;
	}

	public void setNbSkipped(int nbSkipped) {
		this.nbSkipped = nbSkipped;
	}

	public int getNbOverwritten() {
		return nbOverwritten;
	}

	public void setNbOverwritten(int nbOverwrited) {
		this.nbOverwritten = nbOverwrited;
	}

	public int getNbAdded() {
		return nbAdded;
	}

	public void setNbAdded(int nbAdded) {
		this.nbAdded = nbAdded;
	}
	
	public void incrNbAdded() {
		this.nbAdded++;
	}
	
	public void incrNbOverwritten() {
		this.nbOverwritten++;
	}
	
	public void merge(ModuleInstallResult result) {
		this.nbAdded += result.nbAdded;
		this.nbOverwritten += result.nbOverwritten;
		this.nbSkipped += result.nbSkipped;
		this.installedItems.addAll(result.installedItems);
	}
	
	public void addItem(MeveoModuleItemDto item) {
		this.installedItems.add(item);
	}
	
	public List<MeveoModuleItemDto> getInstalledItems() {
		return installedItems;
	}

	@Override
	public String toString() {
		return nbSkipped + " items skipped, " + nbOverwritten + " items overwritten, " + nbAdded + " items added.";
	}

}
