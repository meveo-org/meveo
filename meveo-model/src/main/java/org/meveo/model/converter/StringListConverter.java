package org.meveo.model.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

import org.meveo.commons.utils.StringUtils;

/**
 * Converts a string database column field into a list of strings with a given
 * delimiter which defaults to comma '|'.
 * 
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
public class StringListConverter implements AttributeConverter<List<String>, String> {

	private static final String SPLIT_CHAR = "|";
	private static final String SPLIT_CHAR_DELIM = "\\|";

	@Override
	public String convertToDatabaseColumn(List<String> stringList) {
		return stringList == null ? "" : String.join(SPLIT_CHAR, stringList);
	}

	@Override
	public List<String> convertToEntityAttribute(String string) {
		return StringUtils.isBlank(string) ? new ArrayList<>() : Arrays.asList(string.split(SPLIT_CHAR_DELIM));
	}
}