package json.meveo;

import org.junit.Assert;
import org.junit.Test;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.service.crm.impl.JSONSchemaIntoTemplateParser;
import java.util.Map;

public class JSONSchemaIntoTemplateParserTest {

    private JSONSchemaIntoTemplateParser jsonSchemaIntoTemplateParser = new JSONSchemaIntoTemplateParser();

    private Map<String, Object> jsonMap;

    @Test
    public void testParseCode() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("TodoList", customEntityTemplateDto.getCode());
    }

    @Test
    public void testParseName() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("Todo List", customEntityTemplateDto.getName());
    }

    @Test
    public void testParseFieldsSize() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(3, customEntityTemplateDto.getFields().size());
    }

    @Test
    public void testParseField0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("icon", customEntityTemplateDto.getFields().get(0).getCode());
    }

    @Test
    public void testParseFieldType0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("string".toUpperCase(), customEntityTemplateDto.getFields().get(0).getFieldType().toString());
    }

    @Test
    public void testParseFieldNullable0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(true, !customEntityTemplateDto.getFields().get(0).isValueRequired());
    }

    @Test
    public void testParseFieldReadOnly0() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(false, !customEntityTemplateDto.getFields().get(0).isAllowEdit());
    }

    @Test
    public void testParseField1() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("title", customEntityTemplateDto.getFields().get(1).getCode());
    }

    @Test
    public void testParseFieldType1() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("string".toUpperCase(), customEntityTemplateDto.getFields().get(1).getFieldType().toString());
    }

    @Test
    public void testParseFieldNullable1() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(true, !customEntityTemplateDto.getFields().get(1).isValueRequired());
    }

    @Test
    public void testParseFieldReadOnly1() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(false, !customEntityTemplateDto.getFields().get(1).isAllowEdit());
    }

    @Test
    public void testParseFieldMaxLength() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(255, customEntityTemplateDto.getFields().get(1).getMaxValue().longValue());
    }

    @Test
    public void testParseField2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("items", customEntityTemplateDto.getFields().get(2).getCode());
    }

    @Test
    public void testParseFieldType2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("entity".toUpperCase(), customEntityTemplateDto.getFields().get(2).getFieldType().toString());
    }

    @Test
    public void testParseFieldNullable2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(true, !customEntityTemplateDto.getFields().get(2).isValueRequired());
    }

    @Test
    public void testParseFieldReadOnly2() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(false, !customEntityTemplateDto.getFields().get(2).isAllowEdit());
    }

    @Test
    public void testParseFieldUnique() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals(true, customEntityTemplateDto.getFields().get(2).isUnique());
    }

    @Test
    public void testParseFieldEntityClazz() {
        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJson("D:\\json\\single_cet.json");
        Assert.assertEquals("TodoItem", customEntityTemplateDto.getFields().get(2).getEntityClazz());
    }
}
