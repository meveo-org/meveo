package json.meveo;

import org.junit.Assert;
import org.junit.Test;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.crm.impl.JSONSchemaIntoTemplateParser;
import java.util.Map;

public class JSONSchemaIntoTemplateParserTest {

    private JSONSchemaIntoTemplateParser jsonSchemaIntoTemplateParser = new JSONSchemaIntoTemplateParser();

    private Map<String, Object> jsonMap;

    @Test
    public void testParseCode() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("Offering", customEntityTemplateDto.getCode());
    }

    @Test
    public void testParseName() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("Offering", customEntityTemplateDto.getName());
    }

    @Test
    public void testParseSuperTemplate() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("WebContent", customEntityTemplateDto.getSuperTemplate());
    }

    @Test
    public void testParseFieldsSize() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(5, customEntityTemplateDto.getFields().size());
    }

    @Test
    public void testParseField0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("updateDate", customEntityTemplateDto.getFields().get(0).getCode());
    }

    @Test
    public void testParseFieldType0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("string".toUpperCase(), customEntityTemplateDto.getFields().get(0).getFieldType().toString());
    }

    @Test
    public void testParseFieldNullable0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(true, !customEntityTemplateDto.getFields().get(0).isValueRequired());
    }

    @Test
    public void testParseFieldReadOnly0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(false, !customEntityTemplateDto.getFields().get(0).isAllowEdit());
    }

    @Test
    public void testParseField1() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("availabilityEndDate", customEntityTemplateDto.getFields().get(1).getCode());
    }

    @Test
    public void testParseReference1() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("Datetime", customEntityTemplateDto.getFields().get(1).getEntityClazz());
    }

    @Test
    public void testParseField2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("package", customEntityTemplateDto.getFields().get(2).getCode());
    }

    @Test
    public void testParseFieldType2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("string".toUpperCase(), customEntityTemplateDto.getFields().get(2).getFieldType().toString());
    }

    @Test
    public void testParseFieldNullable2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(true, !customEntityTemplateDto.getFields().get(2).isValueRequired());
    }

    @Test
    public void testParseFieldReadOnly2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(false, !customEntityTemplateDto.getFields().get(2).isAllowEdit());
    }

    @Test
    public void testParseFieldMaxLength() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(255, customEntityTemplateDto.getFields().get(2).getMaxValue().longValue());
    }

    @Test
    public void testParseField3() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("languages", customEntityTemplateDto.getFields().get(3).getCode());
    }

    @Test
    public void testParseFieldType3() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("string".toUpperCase(), customEntityTemplateDto.getFields().get(3).getFieldType().toString());
    }

    @Test
    public void testParseFieldNullable3() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(true, !customEntityTemplateDto.getFields().get(3).isValueRequired());
    }

    @Test
    public void testParseFieldReadOnly3() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(false, !customEntityTemplateDto.getFields().get(3).isAllowEdit());
    }

    @Test
    public void testParseFieldUnique3() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals(true, customEntityTemplateDto.getFields().get(3).isUnique());
    }

    @Test
    public void testParseStorage3() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("LIST", customEntityTemplateDto.getFields().get(3).getStorageType().toString());
    }

    @Test
    public void testParseField4() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("items", customEntityTemplateDto.getFields().get(4).getCode());
    }

    @Test
    public void testParseType4() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("ENTITY", customEntityTemplateDto.getFields().get(4).getFieldType().toString());
    }

    @Test
    public void testParseFieldEntityClazz() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile("src/test/resources/single_cet.json");
        Assert.assertEquals("TodoItem", customEntityTemplateDto.getFields().get(4).getEntityClazz());
    }

    @Test
    public void testParserFromContent() {
        String content = "{\n" +
                "  \"title\": \"Offering\",\n" +
                "  \"description\": \"Offering\",\n" +
                "  \"id\": \"Offering\",\n" +
                "  \"allOf\": [\n" +
                "    {\n" +
                "      \"$ref\": \"#/definitions/WebContent\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"updateDate\": {\n" +
                "      \"title\": \"Offering.updateDate\",\n" +
                "      \"description\": \"updateDate\",\n" +
                "      \"id\": \"CE_Offering_updateDate\",\n" +
                "      \"nullable\": true,\n" +
                "      \"readOnly\": false,\n" +
                "      \"type\": \"string\",\n" +
                "      \"format\": \"date-time\"\n" +
                "    },\n" +
                "    \"availabilityEndDate\": {\n" +
                "      \"title\": \"Offering.availabilityEndDate\",\n" +
                "      \"description\": \"ENTITY\",\n" +
                "      \"id\": \"CE_Offering_availabilityEndDate\",\n" +
                "      \"nullable\": true,\n" +
                "      \"readOnly\": false,\n" +
                "      \"$ref\": \"#/definitions/Datetime\"\n" +
                "    },\n" +
                "    \"package\": {\n" +
                "      \"title\": \"Offering.package\",\n" +
                "      \"description\": \"package\",\n" +
                "      \"id\": \"CE_Offering_package\",\n" +
                "      \"nullable\": true,\n" +
                "      \"readOnly\": false,\n" +
                "      \"type\": \"string\",\n" +
                "      \"maxLength\": 255\n" +
                "    }\n" +
                "  }\n" +
                "}";
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonContent(content);
        Assert.assertEquals(3, customEntityTemplateDto.getFields().size());
        Assert.assertEquals("Offering", customEntityTemplateDto.getCode());
        Assert.assertEquals("Datetime", customEntityTemplateDto.getFields().get(1).getEntityClazz());
    }
}
