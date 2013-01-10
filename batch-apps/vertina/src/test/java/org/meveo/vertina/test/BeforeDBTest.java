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
package org.meveo.vertina.test;

import java.io.File;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.vertina.VertinaConfig;
import org.testng.annotations.BeforeGroups;
/**
 * Executed before DB tests.
 * 
 * @author Donatas Remeika
 * @created Mar 9, 2009
 */
public class BeforeDBTest {

    @SuppressWarnings("deprecation")
    @BeforeGroups(groups = { "db" })
    public void setUp() throws Exception {
        MeveoPersistence.init(VertinaConfig.getPersistenceUnitName(), VertinaConfig.getPersistenceProperties());

        IDatabaseTester tester = new JdbcDatabaseTester("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:vertina", "sa", "");
        tester.setDataSet(new FlatXmlDataSet(new File("src/test/resources/test-data.xml")));
        IDatabaseConnection connection = tester.getConnection();
        DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new OracleDataTypeFactory());
        tester.onSetup();
    }
    
}
