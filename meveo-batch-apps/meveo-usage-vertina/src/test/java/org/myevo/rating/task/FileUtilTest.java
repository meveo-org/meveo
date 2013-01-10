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
package org.myevo.rating.task;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.List;

import org.junit.Test;
import org.myevo.rating.utils.FileUtil;

public class FileUtilTest {
	
	@Test
	public void getCSVList() throws ParseException{
		String directory = "test-data/edr";
		
		List<String> fileNames = FileUtil.getCSVList(directory);
		
		assertEquals(1, fileNames.size());
		assertEquals("test-data/edr/EDR_DATA_Ticket_16_Ferme-anormale_PA3_idcel_59-idcom306-VM-VD.txt.csv", fileNames.get(0));
	}
	
}
