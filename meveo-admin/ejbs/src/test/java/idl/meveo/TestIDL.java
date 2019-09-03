/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package idl.meveo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.meveo.persistence.neo4j.service.graphql.GraphQLService;

import java.util.ArrayList;
import java.util.List;

public class TestIDL {

    private GraphQLService graphQLService;

    @Before
    public void before() {
        graphQLService = new GraphQLService();
    }

    /**
     * The goal is to test case where IDL is correct
     */
    @Test
    public void testCorrectIdl() {

        List<String> result = new ArrayList<>();
        String testCase1 = "type TypeA {\n" +
                " fieldA : TypeB\n" +
                "}\n" +
                "\n" +
                "type TypeB {\n" +
                "  fieldB : String\n" +
                "}";
        List<String> stringList = graphQLService.validateIdl(testCase1);

        Assert.assertEquals(stringList, result);
    }

    /**
     * The goal is to test case case where IDL is not correct
     */
    @Test
    public void testIncorrectIdl() {

        List<String> result = new ArrayList<>();
        result.add("TypeB");
        String testCase2 = "type TypeA {\n" +
                " fieldA : TypeB\n" +
                "}";
        List<String> stringList = graphQLService.validateIdl(testCase2);

        Assert.assertEquals(stringList, result);
    }
}