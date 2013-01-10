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
package org.meveo.core.inputloader;

import java.io.File;

import org.meveo.commons.utils.FileUtils;
import org.meveo.config.task.TestConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link SimpleFileInputLoader} tests.
 * 
 * @author Ignas Lelys
 * @created Aug 4, 2010
 *
 */
public class SimpleFileInputLoaderTest {
    
    private static final String TEST_FILE_NAME = "test.csv";
    private static final String TEST_DUPLICATED_FILE_NAME = "test_duplicated_file.txt";
    private static final String TEST_DIR = "target/test-classes/files/";
    
    @Test(groups = {"db"})
    public void testLoadInput() {
        SimpleFileInputLoader loader = new SimpleFileInputLoader();
        loader.config = new TestConfig();
        loader.meveoFileConfig = new TestConfig();
        Input input = loader.loadInput();
        Assert.assertEquals(input.getName(), TEST_FILE_NAME);
        
        File f = new File(TEST_DIR + "test.csv.processing");
        Assert.assertTrue(f.exists());
        
        // rename back after test
        FileUtils.moveFile(TEST_DIR, f, TEST_FILE_NAME);
    }
    
    @Test(groups = {"db"})
    public void testIsDuplicateInput() {
        AbstractInputLoader loader = new SimpleFileInputLoader();
        loader.config = new TestConfig();
        File f = new File(TEST_DIR + TEST_DUPLICATED_FILE_NAME);
        
        Assert.assertTrue(loader.isDuplicateInput(f, TEST_DUPLICATED_FILE_NAME));
    }
    
}
