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
package org.meveo.commons.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for FileUtil utility class.
 * 
 * @author Donatas Remeika
 * @created Mar 5, 2009
 */
public class FileUtilTest {

    private String testingDirectory;

    @BeforeMethod(groups = { "unit" })
    public void init() {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempDirPath);
        File testingDir = new File(tempDir, String.valueOf(System.currentTimeMillis()));
        while (testingDir.exists()) {
            testingDir = new File(tempDir, String.valueOf(System.currentTimeMillis()));
        }
        testingDir.mkdir();
        testingDirectory = testingDir.getAbsolutePath();
    }

    @Test(groups = { "unit" })
    public void testAddingExtension() throws IOException {
        String testingExtension = ".testext";
        File newFile = File.createTempFile("TEST", ".test", new File(testingDirectory));
        FileUtils.addExtension(newFile, testingExtension);
        File expectedFile = new File(newFile.getAbsoluteFile() + testingExtension);
        Assert.assertTrue(expectedFile.exists());
        Assert.assertFalse(newFile.exists());
    }

    @Test(groups = { "unit" })
    public void testFileMoveWithoutRenaming() throws IOException {
        File newFile = File.createTempFile("TEST", ".test", new File(testingDirectory));
        File newDirectory = new File(testingDirectory, String.valueOf(System.currentTimeMillis()));
        newDirectory.mkdir();
        FileUtils.moveFile(newDirectory.getAbsolutePath(), newFile, null);
        File expectedFile = new File(newDirectory.getAbsoluteFile(), newFile.getName());
        Assert.assertTrue(expectedFile.exists());
        Assert.assertFalse(newFile.exists());
    }
    
    @Test(groups = { "unit" })
    public void testReplacingExtension() throws IOException {
        String filename = "test";
        String testingExtension = ".testext";
        String replaced = FileUtils.replaceFilenameExtension(filename + ".temp", testingExtension);
        Assert.assertEquals(replaced, filename + testingExtension);
    }
    
    @Test(groups = { "unit" })
    public void testReplacingExtensionForFileWithoutExtension() throws IOException {
        String filename = "test";
        String testingExtension = ".testext";
        String replaced = FileUtils.replaceFilenameExtension(filename, testingExtension);
        Assert.assertEquals(replaced, filename + testingExtension);
    }
    
    @Test(groups = { "unit" })
    public void testGetFormatForFileWithoutExtension() throws IOException {
        String filename = "test";
        FileFormat format = FileUtils.getFileFormatByExtension(filename);
        Assert.assertEquals(format, FileFormat.OTHER);
    }
    
    @Test(groups = { "unit" })
    public void testGetASNFormat() throws IOException {
        String filename = "test.aSN";
        FileFormat format = FileUtils.getFileFormatByExtension(filename);
        Assert.assertEquals(format, FileFormat.ASN);
    }
    
    @Test(groups = { "unit" })
    public void testGetCSVFormat() throws IOException {
        String filename = "test.CSv";
        FileFormat format = FileUtils.getFileFormatByExtension(filename);
        Assert.assertEquals(format, FileFormat.CSV);
    }
    
    @Test(groups = { "unit" })
    public void testGetFileForParsingFromEmptyDir() {
        File fileForParsing = FileUtils.getFileForParsing(testingDirectory, Collections.singletonList(".test2"));
        Assert.assertNull(fileForParsing);
    }

    @Test(groups = { "unit" })
    public void testGetFileForParsingWithDifferentExtension() throws IOException {
        File.createTempFile("TEST", ".test", new File(testingDirectory));
        File fileForParsing = FileUtils.getFileForParsing(testingDirectory, Collections.singletonList(".testx"));
        Assert.assertNull(fileForParsing);
    }

    @Test(groups = { "unit" })
    public void testGetExistingFileForParsing() throws IOException {
        File.createTempFile("TEST", ".test1", new File(testingDirectory));
        File.createTempFile("TEST", ".1test", new File(testingDirectory));
        File expectedFile = File.createTempFile("TEST", ".test", new File(testingDirectory));
        File fileForParsing = FileUtils.getFileForParsing(testingDirectory, Collections.singletonList(".test"));
        Assert.assertEquals(fileForParsing.getAbsolutePath(), expectedFile.getAbsolutePath());
    }

    @Test(groups = { "unit" })
    public void testGetFileForParsingFromFile() throws IOException {
        File tempFile = File.createTempFile("TEST", "file", new File(testingDirectory));
        File fileForParsing = FileUtils
                .getFileForParsing(tempFile.getAbsolutePath(), Collections
                .singletonList(".test"));
        Assert.assertNull(fileForParsing);
    }

    @Test(groups = { "unit" })
    public void testGetFileForParsingFromNonExistingDir() {
        File nonExistingDir = new File(new File(testingDirectory), "notCreated");
        File fileForParsing = FileUtils.getFileForParsing(nonExistingDir.getAbsolutePath(), Collections
                .singletonList(".test"));
        Assert.assertNull(fileForParsing);
    }
}
