package json.meveo;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.Assert;
import org.junit.Test;
import org.meveo.service.crm.impl.JSONSchemaIntoJavaClassParser;

public class JSONSchemaIntoJavaClassTest {

    private JSONSchemaIntoJavaClassParser JSONSchemaIntoJavaClassParser = new JSONSchemaIntoJavaClassParser();

    @Test
    public void testParserPackage() {
        CompilationUnit compilationUnit = JSONSchemaIntoJavaClassParser.parseJavaFile("src/test/resources/Url.json");
        Assert.assertEquals("package org.meveo.model.customEntities;", compilationUnit.getPackageDeclaration().toString().replace("Optional[", "").replace("]", "").trim());
    }

    @Test
    public void testParseImport() {
        CompilationUnit compilationUnit = JSONSchemaIntoJavaClassParser.parseJavaFile("src/test/resources/Url.json");
        Assert.assertEquals("import org.meveo.model.customEntities.Link;", compilationUnit.getImport(3).toString().trim());
    }

    @Test
    public void testParseName() {
        CompilationUnit compilationUnit = JSONSchemaIntoJavaClassParser.parseJavaFile("src/test/resources/Url.json");
        Assert.assertEquals("Url", compilationUnit.getType(0).getName().toString().trim());
    }

    @Test
    public void testParseField() {
        CompilationUnit compilationUnit = JSONSchemaIntoJavaClassParser.parseJavaFile("src/test/resources/Url.json");
        Assert.assertEquals("private DBStorageType storages", compilationUnit.getType(0).getMember(0).toString().replace(";","").trim());
    }

    @Test
    public void testParseAnnotationsOfField() {
        CompilationUnit compilationUnit = JSONSchemaIntoJavaClassParser.parseJavaFile("src/test/resources/Url.json");
        Assert.assertEquals("@NotNull", compilationUnit.getType(0).getMember(1).getAnnotations().toString().replace("[","").replace("]","").trim());
    }

}