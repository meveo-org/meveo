/**
 * 
 */
package org.meveo.model.tests;

import org.meveo.model.IEntity;
import org.meveo.model.scripts.FunctionCategory;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class CategoryTest implements IEntity<Long> {
	
	private FunctionCategory category;
	
	private long nbOk;
	
	private long nbKo;
	
	public CategoryTest(FunctionCategory category, long nbOk, long nbKo) {
		super();
		this.category = category;
		this.nbOk = nbOk;
		this.nbKo = nbKo;
	}

	public boolean isStable() {
		return nbKo == 0;
	}

	public FunctionCategory getCategory() {
		return category;
	}

	public long getNbOk() {
		return nbOk;
	}

	public long getNbKo() {
		return nbKo;
	}
	
	public String getCode() {
		return category.getCode();
	}

	public String getDescription() {
		return category.getDescription();
	}
	
	public long getSuccessPercentage() {
		double l = ((double) nbOk) / (((double) nbOk)  + ((double) nbKo));
		return Math.round(l * 100);
	}
	
	public long getFailurePercentage() {
		double l = ((double) nbKo) / (((double) nbOk)  + ((double) nbKo));
		return Math.round(l * 100);
	}

	@Override
	public Long getId() {
		return category.getId();
	}

	@Override
	public void setId(Long id) {
		
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	
}