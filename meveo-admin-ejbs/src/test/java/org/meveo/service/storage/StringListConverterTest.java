package org.meveo.service.storage;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.meveo.model.converter.StringListConverter;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
public class StringListConverterTest {

	private StringListConverter slc;

	@Before
	public void setup() {
		slc = new StringListConverter();
	}

	@Test
	public void testSplit() {
		List<String> input = new ArrayList<>();
		input.add("img/jpg");
		input.add("img/png");

		String output = slc.convertToDatabaseColumn(input);
		assertEquals("img/jpg|img/png", output);
	}

	@Test
	public void testJoin() {
		List<String> arr = new ArrayList<>();
		arr.add("img/jpg");
		arr.add("img/png");

		String input = "img/jpg|img/png";

		List<String> output = slc.convertToEntityAttribute(input);

		assertEquals(arr, output);
	}

	public static void main(String args[]) {
		new StringListConverterTest();
	}

	public StringListConverterTest() {
		slc = new StringListConverter();
		String input = "img/jpg|img/png";
		List<String> output = Arrays.asList(input.split("\\|"));
		System.out.println(output);
	}
}
