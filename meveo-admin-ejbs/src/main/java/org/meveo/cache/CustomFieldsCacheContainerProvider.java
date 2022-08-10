/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.naming.InitialContext;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.Hibernate;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.catalog.CalendarDaily;
import org.meveo.model.catalog.CalendarInterval;
import org.meveo.model.catalog.CalendarYearly;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.crm.impl.CustomFieldException;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.crm.impl.CustomFieldTemplateUtils;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.util.PersistenceUtils;
import org.slf4j.Logger;

/**
 * Provides cache related services (loading, update) for custom field value related operations
 *
 * @author Clément Bareth
 * @author Andrius Karpavicius
 * @author Wassim Drira
 * @lastModifiedVersion 6.3.0
 * 
 */
@Singleton
@DependsOn("CachesInitializer")
public class CustomFieldsCacheContainerProvider implements Serializable {

    private static final long serialVersionUID = 180156064688145292L;

    public static final String MEVEO_CFT_CACHE = "meveo-cft-cache";
    public static final String MEVEO_CET_CACHE = "meveo-cet-cache";
    public static final String MEVEO_CRT_CACHE = "meveo-crt-cache";

    @Inject
    protected Logger log;

    @EJB
    private CustomFieldTemplateService customFieldTemplateService;

    @EJB
    private CustomEntityTemplateService customEntityTemplateService;

    @EJB
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    private ParamBean paramBean = ParamBean.getInstance();
    
    /**
     * Groups custom field templates applicable to the same entity type. Key format: &lt;custom field template appliesTo code&gt;. Value is a map of custom field templates
     * identified by a template code
     */
    private Cache<CacheKeyStr, Map<String, CustomFieldTemplate>> cftsByAppliesTo;

    /**
     * Contains custom entity templates. Key format: &lt;CET code&gt;, value: &lt;CustomEntityTemplate&gt;
     */
    private Cache<CacheKeyStr, CustomEntityTemplate> cetsByCode;

    /**
     * Contains custom entity templates. Key format: &lt;CET code&gt;, value: &lt;CustomEntityTemplate&gt;
     */
    private Cache<CacheKeyStr, CustomRelationshipTemplate> crtsByCode;

    private EmbeddedCacheManager cacheContainer;

    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    @PostConstruct
    protected void initCaches() {
    	try {
			InitialContext initialContext = new InitialContext();
			cacheContainer = (EmbeddedCacheManager) initialContext.lookup("java:jboss/infinispan/container/meveo");
		} catch (Exception e) {
			throw new RuntimeException("Cannot instantiate cache container", e);
		}
    	
        Lock lock = cacheLock.writeLock();
        lock.lock();

        try {
            cftsByAppliesTo = cacheContainer.getCache(MEVEO_CFT_CACHE, true);
            cetsByCode = cacheContainer.getCache(MEVEO_CET_CACHE, true);
            crtsByCode = cacheContainer.getCache(MEVEO_CRT_CACHE, true);

            try {
                populateCache(null, true);
                
                // Check caches integrity
                cftsByAppliesTo.values();
                cetsByCode.values();
                crtsByCode.values();
            } catch (Exception e) {
        		log.warn("Failed to populate CET / CRT / CFT caches. They will be cleaned and repopulated");
                
                cftsByAppliesTo.clear();
                cetsByCode.clear();
                crtsByCode.clear();
                
        		populateCache(null, true);
            }

        } catch(Exception e1) {
    		log.error("Failed to populate CET / CRT / CFT caches.", e1);
    		
    	} finally {
            lock.unlock();
        }
    }

    /**
     * Populate custom field template cache.
     */
    private void populateCFTCache() {

        String currentProvider = null;

        log.debug("Start to pre-populate CFT cache for provider {}", currentProvider);

        CacheKeyStr lastAppliesTo = null;
        Map<String, CustomFieldTemplate> cftsSameAppliesTo = null;

        List<CustomFieldTemplate> cfts = customFieldTemplateService.getCFTForCache();
        for (CustomFieldTemplate cft : cfts) {

            CacheKeyStr cacheKeyByAppliesTo = getCFTCacheKeyByAppliesTo(cft);

            if (lastAppliesTo == null) {
                cftsSameAppliesTo = new TreeMap<>();
                lastAppliesTo = cacheKeyByAppliesTo;

            } else if (!lastAppliesTo.equals(cacheKeyByAppliesTo)) {
                cftsByAppliesTo.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(lastAppliesTo, cftsSameAppliesTo);
                cftsSameAppliesTo = new TreeMap<>();
                lastAppliesTo = cacheKeyByAppliesTo;
            }

            if (cft.getCalendar() != null) {
                cft.setCalendar(PersistenceUtils.initializeAndUnproxy(cft.getCalendar()));
                if (cft.getCalendar() instanceof CalendarDaily) {
                    ((CalendarDaily) cft.getCalendar()).setHours(PersistenceUtils.initializeAndUnproxy(((CalendarDaily) cft.getCalendar()).getHours()));
                } else if (cft.getCalendar() instanceof CalendarYearly) {
                    ((CalendarYearly) cft.getCalendar()).setDays(PersistenceUtils.initializeAndUnproxy(((CalendarYearly) cft.getCalendar()).getDays()));
                } else if (cft.getCalendar() instanceof CalendarInterval) {
                    ((CalendarInterval) cft.getCalendar()).setIntervals(PersistenceUtils.initializeAndUnproxy(((CalendarInterval) cft.getCalendar()).getIntervals()));
                }
            }
            if (cft.getListValues() != null) {
                cft.getListValues().values().toArray(new String[] {});
            }

            customFieldTemplateService.detach(cft);

            cftsSameAppliesTo.put(cft.getCode(), cft);
        }

        if (cftsSameAppliesTo != null && !cftsSameAppliesTo.isEmpty()) {
            cftsByAppliesTo.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(lastAppliesTo, cftsSameAppliesTo);
        }

        log.info("CFT cache populated with {} values of provider {}.", cfts.size(), currentProvider);

    }

    /**
     * Populate custom entity template cache.
     */
    private void populateCETCache() {

        boolean prepopulateCETCache = Boolean.parseBoolean(paramBean.getProperty("cache.cacheCET.prepopulate", "true"));

        if (!prepopulateCETCache) {
            log.info("CET cache pre-population will be skipped");
            return;
        }

        String currentProvider = null;

        log.debug("Start to pre-populate CET cache for provider {}", currentProvider);

        // Cache custom entity templates sorted by a cet.name
        List<CustomEntityTemplate> allCets = customEntityTemplateService.getCETForCache();

        for (CustomEntityTemplate cet : allCets) {
            customEntityTemplateService.detach(cet);
            cetsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(new CacheKeyStr(currentProvider, cet.getCode()), cet);
        }

        log.info("CET cache populated with {} values of provider {}.", allCets.size(), currentProvider);
    }

    private void populateCRTCache() {

        String currentProvider = null;

        // Cache custom relationship templates sorted by a crt.name
        List<CustomRelationshipTemplate> allCrts = customRelationshipTemplateService.getCRTForCache();

        for (CustomRelationshipTemplate crt : allCrts) {
            customRelationshipTemplateService.detach(crt);
            crt.setStartNode((CustomEntityTemplate) Hibernate.unproxy(crt.getStartNode()));
            crt.setEndNode((CustomEntityTemplate) Hibernate.unproxy(crt.getEndNode()));
            crtsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(new CacheKeyStr(currentProvider, crt.getCode()), crt);
        }

        log.info("CRT cache populated with {} values of provider {}.", allCrts.size(), currentProvider);
    }

    /**
     * Get a summary of cached information.
     * 
     * @return A map containing cache information with cache name as a key and cache as a value
     */
    // @Override
    @SuppressWarnings("rawtypes")
    public Map<String, Cache> getCaches() {
        Map<String, Cache> summaryOfCaches = new HashMap<>();
        summaryOfCaches.put(cftsByAppliesTo.getName(), cftsByAppliesTo);
        summaryOfCaches.put(cetsByCode.getName(), cetsByCode);
        summaryOfCaches.put(crtsByCode.getName(), crtsByCode);

        return summaryOfCaches;
    }

    /**
     * Refresh cache by name. Removes current provider's data from cache and populates it again
     * 
     * @param cacheName Name of cache to refresh or null to refresh all caches
     */
    public void refreshCache(String cacheName) {

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            if (cacheName == null || cacheName.equals(cftsByAppliesTo.getName()) || cacheName.contains(cftsByAppliesTo.getName())) {
                cftsByAppliesToClear();
                populateCFTCache();
            }

            if (cacheName == null || cacheName.equals(cetsByCode.getName()) || cacheName.contains(cetsByCode.getName())) {
                cetsByCodeClear();
                populateCETCache();
            }

            if (cacheName == null || cacheName.equals(crtsByCode.getName()) || cacheName.contains(crtsByCode.getName())) {
                crtsByCodeClear();
                populateCRTCache();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Populate cache by name
     *
     * @param cacheName Name of cache to populate or null to populate all caches
     * @param onlyIfEmpty If <code>true</code>, will popuplate cache only if it is empty
     */
    public void populateCache(String cacheName, boolean onlyIfEmpty) {

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            if (cacheName == null || cacheName.equals(cftsByAppliesTo.getName()) || cacheName.contains(cftsByAppliesTo.getName())) {
                AdvancedCache<CacheKeyStr, Map<String, CustomFieldTemplate>> cftCache = cftsByAppliesTo.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
                if (cftCache.isEmpty() || !onlyIfEmpty) {
                    populateCFTCache();
                } else {
                    log.info("CFT cache already loaded with {} values", cftCache.size());
                }
            }

            if (cacheName == null || cacheName.equals(cetsByCode.getName()) || cacheName.contains(cetsByCode.getName())) {
                AdvancedCache<CacheKeyStr, CustomEntityTemplate> cetCache = cetsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
                if (cetCache.isEmpty() || !onlyIfEmpty) {
                    populateCETCache();
                } else {
                    log.info("CET cache already loaded with {} values", cetCache.size());
                }
            }

            if (cacheName == null || cacheName.equals(crtsByCode.getName()) || cacheName.contains(crtsByCode.getName())) {
                AdvancedCache<CacheKeyStr, CustomRelationshipTemplate> crtCache = crtsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
                if (crtCache.isEmpty() || !onlyIfEmpty) {
                    populateCRTCache();
                } else {
                    log.info("CRT cache already loaded with {} values", crtCache.size());
                }
        }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Store mapping between CF code and value storage in cache time period and cache by CFT appliesTo value.
     * 
     * @param cft Custom field template definition
     */
    public void addUpdateCustomFieldTemplate(CustomFieldTemplate cft) {

        CacheKeyStr cacheKeyByAppliesTo = getCFTCacheKeyByAppliesTo(cft);

        log.trace("Adding/updating custom field template {} for {} to CFT cache of Provider {}.", cft.getCode(), cacheKeyByAppliesTo, null);

        Map<String, CustomFieldTemplate> cftsOld = cftsByAppliesTo.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(cacheKeyByAppliesTo);
        Map<String, CustomFieldTemplate> cfts = new TreeMap<>();
        if (cftsOld != null) {
            cfts.putAll(cftsOld);
        }

        // Load calendar for lazy loading
        if (cft.getCalendar() != null) {
            cft.setCalendar(PersistenceUtils.initializeAndUnproxy(cft.getCalendar()));
            if (cft.getCalendar() instanceof CalendarDaily) {
                ((CalendarDaily) cft.getCalendar()).setHours(PersistenceUtils.initializeAndUnproxy(((CalendarDaily) cft.getCalendar()).getHours()));
                cft.getCalendar().nextCalendarDate(new Date());
            } else if (cft.getCalendar() instanceof CalendarYearly) {
                ((CalendarYearly) cft.getCalendar()).setDays(PersistenceUtils.initializeAndUnproxy(((CalendarYearly) cft.getCalendar()).getDays()));
                cft.getCalendar().nextCalendarDate(new Date());
            } else if (cft.getCalendar() instanceof CalendarInterval) {
                ((CalendarInterval) cft.getCalendar()).setIntervals(PersistenceUtils.initializeAndUnproxy(((CalendarInterval) cft.getCalendar()).getIntervals()));
                cft.getCalendar().nextCalendarDate(new Date());
            }
        }
        if (cft.getListValues() != null) {
            cft.getListValues().values().toArray(new String[] {});
        }

        cft = SerializationUtils.clone(cft);

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            cfts.put(cft.getCode(), cft);
            cftsByAppliesTo.getAdvancedCache()
                .withFlags(Flag.IGNORE_RETURN_VALUES)
                .put(cacheKeyByAppliesTo, cfts);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove mapping between CF code and value storage in cache time period and remove from cache by CFT appliesTo value.
     * 
     * @param cft Custom field template definition
     */
    public void removeCustomFieldTemplate(CustomFieldTemplate cft) {

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            CacheKeyStr cacheKeyByAppliesTo = getCFTCacheKeyByAppliesTo(cft);

            String currentProvider = null;
            log.trace("Removing custom field template {} for {} from CFT cache of Provider {}.", cft.getCode(), cacheKeyByAppliesTo, currentProvider);

            Map<String, CustomFieldTemplate> cftsOld = cftsByAppliesTo.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(cacheKeyByAppliesTo);

            if (cftsOld != null && cftsOld.containsKey(cft.getCode())) {

                Map<String, CustomFieldTemplate> cfts = new TreeMap<>(cftsOld);
                cfts.remove(cft.getCode());

                // If no value are left in the map - LEAVE, as cache can be populated at runtime
                // instead of at application start and need to distinguish
                // between not cached key and key with no records
                cftsByAppliesTo.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cacheKeyByAppliesTo, cfts);

                log.trace("Removed custom field template {} for {} from CFT cache for Provider {}.", cft.getCode(), cacheKeyByAppliesTo, currentProvider);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Mark in cache that there are no custom field templates cached under this cache key
     * 
     * @param appliesTo AlliesTo value
     */
    public void markNoCustomFieldTemplates(String appliesTo) {

        CacheKeyStr cacheKeyByAppliesTo = new CacheKeyStr(null, appliesTo);
        if (!cftsByAppliesTo.getAdvancedCache().containsKey(cacheKeyByAppliesTo)) {
            Lock lock = cacheLock.writeLock();
            lock.lock();
            try {
                cftsByAppliesTo.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cacheKeyByAppliesTo, new HashMap<>());
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Store custom entity template to cache.
     * 
     * @param cet Custom entity template definition
     */
    public void addUpdateCustomEntityTemplate(CustomEntityTemplate cet) {
        log.trace("Adding CET template {} to CET cache", cet.getCode());

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            cetsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(new CacheKeyStr(null, cet.getCode()), cet);
        } finally {
            lock.unlock();
        }

        // Sort values by cet.name
        // Collections.sort(cetsByProvider);

    }

    /**
     * Remove custom entity template from cache.
     * 
     * @param cet Custom entity template definition
     */
    public void removeCustomEntityTemplate(CustomEntityTemplate cet) {
        log.trace("Removing CET template {} from CET cache", cet.getCode());

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            cetsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(new CacheKeyStr(null, cet.getCode()));
        } finally {
            lock.unlock();
        }
    }

    public void addUpdateCustomRelationshipTemplate(CustomRelationshipTemplate crt) {
        log.trace("Adding / Updating CRT template {} to CRT cache", crt.getCode());

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            crtsByCode.getAdvancedCache()
                .withFlags(Flag.IGNORE_RETURN_VALUES)
                .put(new CacheKeyStr(null, crt.getCode()), crt);
        } finally {
            lock.unlock();
        }
    }

    public void removeCustomRelationshipTemplate(CustomRelationshipTemplate crt) {
        log.trace("Removing CRT template {} from CRT cache", crt.getCode());

        Lock lock = cacheLock.writeLock();
        lock.lock();
        try {
            crtsByCode.getAdvancedCache()
                .withFlags(Flag.IGNORE_RETURN_VALUES)
                .remove(new CacheKeyStr(null, crt.getCode()));
        } finally {
            lock.unlock();
        }
    }

    private CacheKeyStr getCFTCacheKeyByAppliesTo(CustomFieldTemplate cft) {
        return new CacheKeyStr(null, cft.getAppliesTo());
    }

    /**
     * Get custom field templates for a given entity (appliesTo value).
     * 
     * @param appliesTo entity (appliesTo value)
     * @return A map of custom field templates with template code as a key or NULL if cache key not found
     */
    public Map<String, CustomFieldTemplate> getCustomFieldTemplates(String appliesTo) {
        CacheKeyStr key = new CacheKeyStr(null, appliesTo);

        Map<String, CustomFieldTemplate> cfMaps;
//        Lock lock = cacheLock.readLock();
//        lock.lock();
        try {
            cfMaps = cftsByAppliesTo.get(key);
            if(cfMaps == null) {
            	// Handle case insensitivity
            	for(CacheKeyStr cacheKey : cftsByAppliesTo.keySet()) {
            		if(cacheKey.getKey().toUpperCase().equals(appliesTo.toUpperCase())) {
            			cfMaps = cftsByAppliesTo.get(cacheKey);
            			break;
            		}
            	}
            }
        } finally {
//            lock.unlock();
        }
        if(cfMaps == null || cfMaps.isEmpty()) {
            cfMaps = customFieldTemplateService.findByAppliesToNoCache(appliesTo);
            if(cfMaps != null){
                cfMaps.forEach((k,v) -> {
                    customFieldTemplateService.detach(v);
                    addUpdateCustomFieldTemplate(v);
                });
            }
        }
        return cfMaps;
    }

    /**
     * Get custom entity templates
     * 
     * @return A list of custom entity templates
     */
    public Collection<CustomEntityTemplate> getCustomEntityTemplates() {

        Lock lock = cacheLock.readLock();
        lock.lock();
        try {
            return cetsByCode.values();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get custom entity template by code
     * 
     * @param code Custom entity template code
     * @return Custom entity template or NULL if not found
     */
    public CustomEntityTemplate getCustomEntityTemplate(String code) {
        CacheKeyStr key = new CacheKeyStr(null, code);
        CustomEntityTemplate customEntityTemplate;
        Lock lock = cacheLock.readLock();
        lock.lock();
        try {
            customEntityTemplate = cetsByCode.get(key);
        } finally {
            lock.unlock();
        }

        if(customEntityTemplate == null){
            customEntityTemplate = customEntityTemplateService.findByCode(code);
            if(customEntityTemplate != null) {
                customEntityTemplateService.detach(customEntityTemplate);
                addUpdateCustomEntityTemplate(customEntityTemplate);
            }
        }

        return customEntityTemplate;
    }

    /**
     * Get custom relationship template by code
     *
     * @param code Custom relationship template code
     * @return Custom relationship template or NULL if not found
     */
    public CustomRelationshipTemplate getCustomRelationshipTemplate(String code) {
        CacheKeyStr key = new CacheKeyStr(null, code);
        CustomRelationshipTemplate customRelationshipTemplate;
        Lock lock = cacheLock.readLock();
        lock.lock();
        try {
            customRelationshipTemplate = crtsByCode.get(key);
        } finally {
            lock.unlock();
        }

        if(customRelationshipTemplate == null) {
            customRelationshipTemplate = customRelationshipTemplateService.findByCode(code);
            if(customRelationshipTemplate != null){
                customRelationshipTemplateService.detach(customRelationshipTemplate);
                customRelationshipTemplate.setStartNode((CustomEntityTemplate) Hibernate.unproxy(customRelationshipTemplate.getStartNode()));
                customRelationshipTemplate.setEndNode((CustomEntityTemplate) Hibernate.unproxy(customRelationshipTemplate.getEndNode()));
                addUpdateCustomRelationshipTemplate(customRelationshipTemplate);
            }
        }

        return customRelationshipTemplate;
    }

    /**
     * Get custom relationships
     *
     * @return Cached custom relationship templates
     */
    public Collection<CustomRelationshipTemplate> getCustomRelationshipTemplates() {
        Lock lock = cacheLock.readLock();
        lock.lock();
        try {
            return crtsByCode.values();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get custom field template of a given code, applicable to a given entity
     * 
     * @param code Custom field template code
     * @param entity Entity
     * @return Custom field template
     */
    public CustomFieldTemplate getCustomFieldTemplate(String code, ICustomFieldEntity entity) {
        try {
            return getCustomFieldTemplate(code, CustomFieldTemplateUtils.calculateAppliesToValue(entity));

        } catch (CustomFieldException e) {
            log.error("Can not determine applicable CFT type for entity of {} class.", entity.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * Get custom field template of a given code, applicable to a given entity
     * 
     * @param code Custom field template code
     * @param appliesTo Entity appliesTo value
     * @return Custom field template or NULL if not found
     */
    public CustomFieldTemplate getCustomFieldTemplate(String code, String appliesTo) {
    	if(code == null) throw new IllegalArgumentException("Code should be provided");
    	
        Map<String, CustomFieldTemplate> cfts = getCustomFieldTemplates(appliesTo);
        if (cfts != null) {
            return cfts.get(code);
        }
        return null;
    }

    /**
     * Clear the data belonging to the current provider from cache
     */
    public void cetsByCodeClear() {
        String currentProvider = null;
        log.debug("cetsByCodeClear() => " + currentProvider + ".");
        ArrayList<CacheKeyStr> itemsToBeRemoved = new ArrayList<>();

        Lock readLock = cacheLock.readLock();
        readLock.lock();
        try {
            Iterator<Entry<CacheKeyStr, CustomEntityTemplate>> iter = cetsByCode.getAdvancedCache()
                .withFlags(Flag.IGNORE_RETURN_VALUES)
                .entrySet()
                .iterator();
            while (iter.hasNext()) {
                Entry<CacheKeyStr, CustomEntityTemplate> entry = iter.next();
                boolean comparison = (entry.getKey().getProvider() == null)
                    ? currentProvider == null
                    : entry.getKey().getProvider().equals(currentProvider);
                if (comparison) {
                    itemsToBeRemoved.add(entry.getKey());
                }
            }
        } finally {
          readLock.unlock();
        }

        for (CacheKeyStr elem : itemsToBeRemoved) {
            log.debug("Remove element Provider:" + elem.getProvider() + " Key:" + elem.getKey() + ".");
            Lock writeLock = cacheLock.writeLock();
            writeLock.lock();
            try {
                cetsByCode.getAdvancedCache()
                    .withFlags(Flag.IGNORE_RETURN_VALUES)
                    .remove(elem);
            } finally {
                writeLock.unlock();;
            }
        }
    }

    /**
     * Clear the data belonging to the current provider from cache
     */
    public void crtsByCodeClear() {
        String currentProvider = null;
        log.debug("crtsByCodeClear() => " + currentProvider + ".");
        ArrayList<CacheKeyStr> itemsToBeRemoved = new ArrayList<>();

        Lock readLock = cacheLock.readLock();
        readLock.lock();
        try {
            CloseableIterator<Entry<CacheKeyStr, CustomRelationshipTemplate>> iter = crtsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).entrySet().iterator();
            while (iter.hasNext()) {
                Entry<CacheKeyStr, CustomRelationshipTemplate> entry = iter.next();
                boolean comparison = (entry.getKey().getProvider() == null) ? currentProvider == null : entry.getKey().getProvider().equals(currentProvider);
                if (comparison) {
                    itemsToBeRemoved.add(entry.getKey());
                }
            }
        } finally {
            readLock.unlock();
        }

        for (CacheKeyStr elem : itemsToBeRemoved) {
            log.debug("Remove element Provider:" + elem.getProvider() + " Key:" + elem.getKey() + ".");
            Lock writeLock = cacheLock.writeLock();
            writeLock.lock();
            try {
                crtsByCode.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(elem);
            } finally {
                writeLock.unlock();;
            }
        }
    }

    /**
     * Clear the data belonging to the current provider from cache
     */
    public void cftsByAppliesToClear() {
        String currentProvider = null;
        // cftsByAppliesTo.keySet().removeIf(key -> (key.getProvider() == null) ? currentProvider == null : key.getProvider().equals(currentProvider));
        ArrayList<CacheKeyStr> itemsToBeRemoved = new ArrayList<>();

        Lock readLock = cacheLock.readLock();
        readLock.lock();
        try {
            Iterator<Entry<CacheKeyStr, Map<String, CustomFieldTemplate>>> iter = cftsByAppliesTo.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).entrySet().iterator();
            while (iter.hasNext()) {
                Entry<CacheKeyStr, Map<String, CustomFieldTemplate>> entry = iter.next();
                boolean comparison = (entry.getKey().getProvider() == null) ? currentProvider == null : entry.getKey().getProvider().equals(currentProvider);
                if (comparison) {
                    itemsToBeRemoved.add(entry.getKey());
                }
            }
        } finally {
            readLock.unlock();
        }

        for (CacheKeyStr elem : itemsToBeRemoved) {
            log.debug("Remove element Provider:" + elem.getProvider() + " Key:" + elem.getKey() + ".");
            Lock writeLock = cacheLock.writeLock();
            writeLock.lock();
            try {
                cftsByAppliesTo.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(elem);
            } finally {
                writeLock.unlock();;
            }
        }
    }

    /**
     * Clear all the data in CFT cache
     */
    public void cftsByAppliesToClearAll() {
        Lock writeLock = cacheLock.writeLock();
        writeLock.lock();
        try {
            cftsByAppliesTo.clear();
        } finally {
            writeLock.unlock();;
        }
    }
}
