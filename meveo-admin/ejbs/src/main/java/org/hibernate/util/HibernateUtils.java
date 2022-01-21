/**
 * 
 */
package org.hibernate.util;

import org.hibernate.proxy.HibernateProxy;

/**
 * 
 * @author ClementBareth
 * @since 
 * @version
 */
public class HibernateUtils {

	public static boolean isLazyLoaded(Object object) {
		if (!(object instanceof HibernateProxy)) {
			return false;
		}
		
		HibernateProxy proxy = (HibernateProxy) object;
		return proxy.getHibernateLazyInitializer().isUninitialized();
	}
}
