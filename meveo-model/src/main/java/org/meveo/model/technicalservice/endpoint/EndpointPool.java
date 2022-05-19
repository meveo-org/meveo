/**
 * 
 */
package org.meveo.model.technicalservice.endpoint;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;

/**
 * Configuration of the script instances pool of an endpoint
 * 
 * @author ClementBareth
 * @since 7.1.0
 */
@Embeddable
public class EndpointPool implements Serializable {

	private static final long serialVersionUID = -8015938405821343193L;
	
	@Column(name = "use_pool")
	@Type(type = "numeric_boolean")
	private boolean usePool = false;
	
	/**
	 * Minimum number of script instance in the pool
	 */
	@Column(name = "min_pool")
	private String min;
	
	/**
	 * Maxiumum number of script instance in the pool
	 */
	@Column(name = "max_pool")
	private String max;
	
	/**
	 * Maximum idle time before script get evicted (in seconds)
	 */
	@Column(name = "max_idle_time_pool")
	private String maxIdleTime;

	/**
	 * @return the {@link #min}
	 */
	public String getMin() {
		return min;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(String min) {
		this.min = min;
	}

	/**
	 * @return the {@link #max}
	 */
	public String getMax() {
		return max;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(String max) {
		this.max = max;
	}

	/**
	 * @return the {@link #maxIdleTime}
	 */
	public String getMaxIdleTime() {
		return maxIdleTime;
	}

	/**
	 * @param maxIdle the maxIdle to set
	 */
	public void setMaxIdleTime(String maxIdle) {
		this.maxIdleTime = maxIdle;
	}

	/**
	 * @return the {@link #usePool}
	 */
	public boolean isUsePool() {
		return usePool;
	}

	/**
	 * @param usePool the usePool to set
	 */
	public void setUsePool(boolean usePool) {
		this.usePool = usePool;
	}

}
