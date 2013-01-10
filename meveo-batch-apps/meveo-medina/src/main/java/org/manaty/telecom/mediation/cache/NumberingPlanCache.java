/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.manaty.telecom.mediation.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.meveo.model.mediation.NumberingPlan;
import org.manaty.telecom.mediation.MedinaPersistence;

/**
 * Simple cache manager that has hash maps for storing NumberingPlans and routing tables.
 * It is uploaded from database and is not later changed, so if numbering plans are updated in db,
 * application must be restarted.
 * 
 * @author Ignas Lelys
 * @created 2009.09.28
 */
public final class NumberingPlanCache {

    private static final Logger logger = Logger.getLogger(TransactionalMagicNumberCache.class);
    
    /**
     * Finds all regexps that needs only 'starts with' functionality. For example 123*.
     * Then those regexps are put in separate cache from real ones, and only String.startsWith()
     * feature is used instead of full regexp matching.
     */
    private static final Pattern STARTS_WITH_PATTERN_REGEXP = Pattern.compile("^[^\\*]*\\*$");

    /** Numbering plan cache by phone prefix */
    private Map<Key, NumberingPlan> numberingPlansByPhonePrefix;

    /**
     * Instance of cache manager.
     */
    private static NumberingPlanCache instance;

    static {
        try {
            instance = new NumberingPlanCache();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create cache manager instance!", e);
        }
    }

    /**
     * Static factory method to get instance of cache manager. Manager is
     * singleton.
     */
    public static NumberingPlanCache getInstance() {
        return instance;
    }

    /**
     * Private constructor.
     */
    private NumberingPlanCache() {
        super();
        loadCache();
    }

    /**
     * Load cache from database.
     * 
     * @return Number of numbering plans found in database.
     */
    @SuppressWarnings( { "unchecked" })
    private int loadCache() {
    	
    	Map<Key, NumberingPlan> numberingPlansByPhoneNumberCopy = new HashMap<Key, NumberingPlan>();
        Map<Key, NumberingPlan> numberingPlansByPhoneRegexpCopy = new HashMap<Key, NumberingPlan>();
        Map<Key, NumberingPlan> numberingPlansByPhonePrefixCopy = new HashMap<Key, NumberingPlan>();
        Map<RoutingKey, String> routingCacheCopy = new HashMap<RoutingKey, String>();
        Map<String, List<Key>> orderedRegexpsByOfferCodeCopy = new HashMap<String, List<Key>>();
        Map<String, List<Key>> orderedStartWithRegexpsByOfferCodeCopy = new HashMap<String, List<Key>>();
        
        EntityManager em = MedinaPersistence.getEntityManager();

        logger.info("Loading numbering plans cache to memory...");
        Query numberingPlanQuery = em.createQuery("from NumberingPlan");
        List<NumberingPlan> numberingPlans = (List<NumberingPlan>) numberingPlanQuery.getResultList();
        
        for (NumberingPlan numberingPlan : numberingPlans) {
            addNumberingPlanToCache(numberingPlan, numberingPlansByPhoneNumberCopy, numberingPlansByPhoneRegexpCopy,
            		numberingPlansByPhonePrefixCopy, orderedRegexpsByOfferCodeCopy, orderedStartWithRegexpsByOfferCodeCopy);
        }
        
        // sort phone regexp by regexp length
        for (String offer : orderedRegexpsByOfferCodeCopy.keySet()) {
        	Collections.sort(orderedRegexpsByOfferCodeCopy.get(offer), new KeyComparator());
        }
        for (String offer : orderedStartWithRegexpsByOfferCodeCopy.keySet()) {
        	Collections.sort(orderedStartWithRegexpsByOfferCodeCopy.get(offer), new KeyComparator());
        }
        
        // set unmodifiable maps for cache
        numberingPlansByPhonePrefix = Collections.unmodifiableMap(numberingPlansByPhonePrefixCopy);
        
        logger.info(String.format("%s numbering plans loaded from database to cache.", numberingPlans.size()));
        return numberingPlans.size();
    }
    
 
    /**
     * Get numbering plan by provided phone number. Provided
     * 
     * @param phoneNumber
     *            Phone number.
     * @return NumberingPlan.
     */
    public NumberingPlan getNumberingPlanFromCache(String ProviderCode,String discriminator, String userId) {
    	if (userId == null)
    		return null;

    	NumberingPlan numberingPlan = null;
    	// check cache by phone prefix (4 symbols length)
        if (phoneNumber.length() >= 4) {
            String prefix = phoneNumber.substring(0, 4);
            numberingPlan = numberingPlansByPhonePrefix.get(new Key(prefix, offerCode, cdrType.toString(), isOutgoing));
        }
        // if not found check cache by phone prefix (3 symbols length)
        if (numberingPlan == null && phoneNumber.length() >= 3) {
            String prefix = phoneNumber.substring(0, 3);
            numberingPlan = numberingPlansByPhonePrefix.get(new Key(prefix, offerCode, cdrType.toString(), isOutgoing));
        }
        // if not found check cache by phone prefix (2 symbols length)
        if (numberingPlan == null && phoneNumber.length() >= 2) {
            String prefix = phoneNumber.substring(0, 2);
            numberingPlan = numberingPlansByPhonePrefix.get(new Key(prefix, offerCode, cdrType.toString(), isOutgoing));
        }
        // if not found check cache by phone prefix (1 symbol length)
        if (numberingPlan == null && phoneNumber.length() >= 1) {
            String prefix = phoneNumber.substring(0, 1);
            numberingPlan = numberingPlansByPhonePrefix.get(new Key(prefix, offerCode, cdrType.toString(), isOutgoing));
        }
        return numberingPlan;
    }
    
    /**
     * Finds startsWith regexp match (something like 123*). This kind of regexps is majority in DB.
     * 
     * @param phoneNumber Phone number for which we search match.
     * @param offerCode Provided offer code.
     * @param cdrType CDRType (SMS,VOICE)
     * @param isOutgoing Is ticket outgoing.
     * 
     * @return NumberingPlan if found. Otherwise null.
     */
    private NumberingPlan findStartsWithMatch(String phoneNumber, String offerCode, CDRType cdrType, Boolean isOutgoing) {
    	List<Key> orderedRegexps = orderedStartWithRegexpsByOfferCode.get(offerCode);
		if (orderedRegexps != null) {
            for (Key key : orderedRegexps) {
                String startWith = key.phoneKey;
                if (phoneNumber.startsWith(startWith)) {
                	NumberingPlan numberingPlan = numberingPlansByPhoneRegexp.get(new Key(startWith, offerCode, cdrType.toString(), isOutgoing));
                    if (numberingPlan != null) {
                        return numberingPlan;
                    }
                }
            }
		}
		return null;
    }
    
    /**
     * Finds real regexp match. There are not much of those. Full regexp matching is done.
     * 
     * @param phoneNumber Phone number for which we search match.
     * @param offerCode Provided offer code.
     * @param cdrType CDRType (SMS,VOICE)
     * @param isOutgoing Is ticket outgoing.
     * 
     * @return NumberingPlan if found. Otherwise null.
     */
    private NumberingPlan findRegexpMatch(String phoneNumber, String offerCode, CDRType cdrType, Boolean isOutgoing) {
    	List<Key> orderedRegexps = orderedRegexpsByOfferCode.get(offerCode);
		if (orderedRegexps != null) {
            for (Key key : orderedRegexps) {
                String regexp = key.phoneKey;
                if (phoneNumber.matches(regexp)) {
                	NumberingPlan numberingPlan = numberingPlansByPhoneRegexp.get(new Key(regexp, offerCode, cdrType.toString(), isOutgoing));
                    if (numberingPlan != null) {
                        return numberingPlan;
                    }
                }
            }
		}
		return null;
    }
    
    /**
     * Fills provided cache maps with info from numberingPlan parameter. Those 'copy' maps will be later used to initialize
     * unmodifiable cache maps.
     * 
     * @param numberingPlan Numbering plan to add to cache.
     * @param numberingPlansByPhoneNumberCopy Plans by phone number cache.
     * @param numberingPlansByPhoneRegexpCopy Plans by regexp cache.
     * @param numberingPlansByPhonePrefixCopy Plans by prefix cache.
     * @param orderedRegexpsByOfferCodeCopy Ordered regexps cache.
     * @param orderedPrefixesByOfferCodeCopy Ordered prefixes cache.
     */
    private void addNumberingPlanToCache(NumberingPlan numberingPlan, 
		    Map<Key, NumberingPlan> numberingPlansByPhonePrefixCopy,
		    Map<String, List<Key>> orderedRegexpsByOfferCodeCopy,
		    Map<String, List<Key>> orderedStartWithRegexpsByOfferCodeCopy
		    ) {
    	
        if (numberingPlan.getPhoneNumber() != null) {
            numberingPlansByPhoneNumberCopy.put(new Key(numberingPlan.getPhoneNumber(), numberingPlan.getOfferCode(), numberingPlan.getCdrType(), numberingPlan.isOutgoing()), numberingPlan);
        } else if (numberingPlan.getPhoneNumberRegexp() != null) {
        	String phoneNumberRegexp = numberingPlan.getPhoneNumberRegexp();
        	Matcher matcher = STARTS_WITH_PATTERN_REGEXP.matcher(phoneNumberRegexp);
        	boolean matchStartsWith = matcher.matches();
        	if (matchStartsWith) { 
        		// when we have startWith pattern (e.g. 33*) we can later use String.startsWith, so we can remove * symbol.
        		String modifiedStartsWithRegexp = phoneNumberRegexp.substring(0, phoneNumberRegexp.indexOf('*'));
        		Key key = new Key(modifiedStartsWithRegexp, numberingPlan.getOfferCode(), numberingPlan.getCdrType(), numberingPlan.isOutgoing());
        		numberingPlansByPhoneRegexpCopy.put(key, numberingPlan);
        		if (orderedStartWithRegexpsByOfferCodeCopy.get(key.offerCode) == null) {
        			orderedStartWithRegexpsByOfferCodeCopy.put(key.offerCode, new ArrayList<Key>());
        		}
        		orderedStartWithRegexpsByOfferCodeCopy.get(key.offerCode).add(key);
        	} else {
        		Key key = new Key(phoneNumberRegexp, numberingPlan.getOfferCode(), numberingPlan.getCdrType(), numberingPlan.isOutgoing());
        		numberingPlansByPhoneRegexpCopy.put(key, numberingPlan);
        		if (orderedRegexpsByOfferCodeCopy.get(key.offerCode) == null) {
        			orderedRegexpsByOfferCodeCopy.put(key.offerCode, new ArrayList<Key>());
        		}
        		orderedRegexpsByOfferCodeCopy.get(key.offerCode).add(key);
        		
        	}
        } else if (numberingPlan.getPhonePrefix() != null) {
            Key key = new Key(numberingPlan.getPhonePrefix(), numberingPlan.getOfferCode(), numberingPlan.getCdrType(), numberingPlan.isOutgoing());
            numberingPlansByPhonePrefixCopy.put(key, numberingPlan);
        }
    }
    
    /**
     * Class for MVNORouting cache key.
     * 
     * @author Ignas Lelys
     * @created 2010.02.18
     */
    public static class RoutingKey {
    	public RoutingKey(String offerCode, String routingPrefix, MvnoRoutingTypeEnum typeAppel) {
			super();
			this.type = typeAppel;
			this.offerCode = offerCode;
			this.routingPrefix = routingPrefix;
		}
		public String offerCode;
    	public String routingPrefix;
    	public MvnoRoutingTypeEnum type;
		public String getOfferCode() {
			return offerCode;
		}
		public void setOfferCode(String offerCode) {
			this.offerCode = offerCode;
		}
		public String getRoutingPrefix() {
			return routingPrefix;
		}
		public void setRoutingPrefix(String routingPrefix) {
			this.routingPrefix = routingPrefix;
		}
		public MvnoRoutingTypeEnum getType() {
			return type;
		}
		public void setType(MvnoRoutingTypeEnum type) {
			this.type = type;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((offerCode == null) ? 0 : offerCode.hashCode());
			result = prime * result
					+ ((routingPrefix == null) ? 0 : routingPrefix.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RoutingKey other = (RoutingKey) obj;
			if (offerCode == null) {
				if (other.offerCode != null)
					return false;
			} else if (!offerCode.equals(other.offerCode))
				return false;
			if (routingPrefix == null) {
				if (other.routingPrefix != null)
					return false;
			} else if (!routingPrefix.equals(other.routingPrefix))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
    }
    
    /**
     * Class for numbering plans cache key.
     * 
     * @author Ignas Lelys
     * @created 2009.09.28
     */
    public static class Key {
        public Key(String providerCode, String discriminator) {
            super();
            this.providerCode = providerCode;
            this.discriminator = discriminator;
        }
        public String providerCode;
        public String discriminator;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((providerCode == null) ? 0 : providerCode.hashCode());
            result = prime * result + ((discriminator == null) ? 0 : discriminator.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (providerCode == null) {
                if (other.providerCode != null)
                    return false;
            } else if (!providerCode.equals(other.providerCode))
                return false;
            if (offerCode == null) {
                if (other.offerCode != null)
                    return false;
            } else if (!offerCode.equals(other.offerCode))
                return false;
            if (phoneKey == null) {
                if (other.phoneKey != null)
                    return false;
            } else if (!phoneKey.equals(other.phoneKey))
                return false;
            return true;
        }
    }
    
    // compares length of phone key
    private static class KeyComparator implements Comparator<Key> {

        public int compare(Key key1, Key key2) {
            if (key1.phoneKey != null && key2.phoneKey != null) {
                if (key1.phoneKey.length() < key2.phoneKey.length()) {
                    return 1;
                } else if (key1.phoneKey.length() > key2.phoneKey.length()) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }



}
