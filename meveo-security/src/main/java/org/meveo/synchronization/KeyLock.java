/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.synchronization;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds a cache of {@link ReentrantLock} by key
 *
 * @see ReentrantLock
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
public class KeyLock {

    private static Logger logger = LoggerFactory.getLogger(KeyLock.class);

    private LoadingCache<String, ReentrantLock> lockCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ReentrantLock>() {
                    @Override
                    public ReentrantLock load(String key) {
                        return new ReentrantLock();
                    }
                });

    public void lock(String key) {
        try {
            lockCache.getUnchecked(key).tryLock(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Interrupted while acquiring lock", e);
        }
    }

    public void unlock(String key){
        lockCache.getUnchecked(key).unlock();
    }

}
