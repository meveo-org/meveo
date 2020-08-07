/**
 * 
 */
package org.meveo.api.module;

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
	}

	@Override
	public String toString() {
		return nbSkipped + " items skipped, " + nbOverwritten + " items overwritten, " + nbAdded + " items added.";
	}

}
