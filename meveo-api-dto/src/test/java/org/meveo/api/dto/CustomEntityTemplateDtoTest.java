/**
 * 
 */
package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class CustomEntityTemplateDtoTest {

	@Test
	public void testSorting() {
		var dto1 = new CustomEntityTemplateDto();
		dto1.setCode("dto1");
		
		var dto2 = new CustomEntityTemplateDto();
		dto2.setCode("dto2");
		dto2.setSuperTemplate("dto1");
		
		var dto3 = new CustomEntityTemplateDto();
		dto3.setCode("dto3");
		dto3.setSuperTemplate("dto2");
		
		var listToSort = new ArrayList<CustomEntityTemplateDto>();
		listToSort.add(dto3);
		listToSort.add(dto1);
		listToSort.add(dto2);
		
		Collections.sort(listToSort);
		
		assert listToSort.get(0) == dto1;
		assert listToSort.get(1) == dto2;
		assert listToSort.get(2) == dto3;
	}
}
