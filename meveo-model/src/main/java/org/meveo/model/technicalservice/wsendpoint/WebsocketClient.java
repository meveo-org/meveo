/**
 * 
 */
package org.meveo.model.technicalservice.wsendpoint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;

@Entity
@ModuleItem(value = "WebsocketClient", path = "websocket-clients")
@ModuleItemOrder(80)
@Table(name = "websocket_client")
@GenericGenerator(name = "ID_GENERATOR", strategy = "increment")
@ObservableEntity
public class WebsocketClient extends Websocket {
	
	private static final long serialVersionUID = -8049840006660921466L;
	
	@Column(name = "url")
	private String url;
	
	@Column(name = "retry_delay")
	private int retryDelayInSeconds = 60;
	
	@Column(name = "nb_max_retry")
	private int nbMaxRetry = 100;

	/**
	 * @return the {@link #url}
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the {@link #retryDelayInSeconds}
	 */
	public int getRetryDelayInSeconds() {
		return retryDelayInSeconds;
	}

	/**
	 * @param retryDelayInSeconds the retryDelayInSeconds to set
	 */
	public void setRetryDelayInSeconds(int retryDelayInSeconds) {
		this.retryDelayInSeconds = retryDelayInSeconds;
	}

	/**
	 * @return the {@link #nbMaxRetry}
	 */
	public int getNbMaxRetry() {
		return nbMaxRetry;
	}

	/**
	 * @param nbMaxRetry the nbMaxRetry to set
	 */
	public void setNbMaxRetry(int nbMaxRetry) {
		this.nbMaxRetry = nbMaxRetry;
	}
	
}
