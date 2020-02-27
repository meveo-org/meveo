/**
 * 
 */
package org.meveo.api.technicalservice.utils;

import org.meveo.api.dto.technicalservice.InputPropertyDto;
import org.meveo.api.dto.technicalservice.OutputPropertyDto;
import org.meveo.interfaces.technicalservice.description.properties.PropertyDescription;
import org.meveo.model.technicalservice.InputMeveoProperty;
import org.meveo.model.technicalservice.MeveoPropertyDescription;
import org.meveo.model.technicalservice.OutputMeveoProperty;

/**
 * 
 * @author clement.bareth
 * @since 6.8.0
 * @version 6.8.0
 */
public class DescriptionUtils {

    public static PropertyDescription toDto(PropertyDescription description) {
    	if(description instanceof MeveoPropertyDescription) {
    		if(description instanceof InputMeveoProperty) {
    			return new InputPropertyDto((InputMeveoProperty) description);
    		} else {
    			return new OutputPropertyDto((OutputMeveoProperty) description);
    		}
    	}
    	
    	return description;    	
    }
    
}
