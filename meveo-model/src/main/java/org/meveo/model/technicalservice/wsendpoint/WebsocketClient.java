/**
 * 
 */
package org.meveo.model.technicalservice.wsendpoint;

public class WebsocketClient extends WSEndpoint {
	
	private String url;
	private int retryDelayInSeconds = 60;
	private int nbMaxRetry = Integer.MAX_VALUE;

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
