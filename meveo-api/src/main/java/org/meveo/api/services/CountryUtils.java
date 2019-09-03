package org.meveo.api.services;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.meveo.commons.utils.StringUtils;

@Singleton
@Startup
public class CountryUtils implements Serializable{

	 /**
	 * 
	 */
	private static final long serialVersionUID = 2633960703470285525L;
	
	Map<String, String> countriesEN = new HashMap<>();
	Map<String, String> countriesFR = new HashMap<>();
	Map<String, String> countriesIsoEN = new HashMap<>();
	Map<String, String> countriesIsoFR = new HashMap<>();
	Map<String, String> languagesMap = new HashMap<>();
    Map<String, Locale> countriesIso3 = new HashMap<>();

    @PostConstruct
    public void init() {

        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            Locale.setDefault(Locale.US);
            countriesEN.put(l.getDisplayCountry().toLowerCase(), iso);
            countriesIsoEN.put(iso, l.getDisplayCountry());
            Locale.setDefault(Locale.FRANCE);
            countriesFR.put(l.getDisplayCountry().toLowerCase(), iso);
            countriesIsoFR.put(iso, l.getDisplayCountry());
            countriesIso3.put(l.getISO3Country().toUpperCase(), l);
        }
    }


    public String getCountryIsoCode(Locale locale, String countryName) {
        if (!StringUtils.isBlank(countryName)) {
            countryName = countryName.toLowerCase();
            if (locale == null || locale.equals(Locale.US)) {
                return countriesEN.get(countryName.toLowerCase());
            } else if (locale.equals(Locale.FRANCE)) {
                return countriesFR.get(countryName.toLowerCase());
            }
        }

        return null;
    }

    public String getCountryName(Locale locale, String isoCode) {
        if (!StringUtils.isBlank(isoCode)) {
            isoCode = isoCode.toUpperCase();
            if (locale == null || locale.equals(Locale.US)) {
                return countriesIsoEN.get(isoCode);
            } else if (locale.equals(Locale.FRANCE)) {
                return countriesIsoFR.get(isoCode);
            }
        }

        return null;
    }

    public String getNativeCountryName(String countryCode) {
        Locale locale = null;
        if (languagesMap.get(countryCode) == null) {
            locale = new Locale("", countryCode);
        } else {
            //create a Locale with own country's languages
            locale = new Locale(languagesMap.get(countryCode), countryCode);
        }

        return locale.getDisplayCountry(locale);
    }

    public String getIso2CountryCode(String iso3CountryCode) {
        return countriesIso3.get(iso3CountryCode).getCountry();
    }
}
