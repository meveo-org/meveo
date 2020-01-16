package org.meveo.service.crm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JSONSchemaIntoJavaClassParser {

    private Map<String, Object> jsonMap;

    @SuppressWarnings("unchecked")
    public CompilationUnit parseJavaFile(String file) {
        CompilationUnit compilationUnit = new CompilationUnit();
        try {
            File sourceDir = new File(file);
            byte[] mapData = Files.readAllBytes(sourceDir.toPath());
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(mapData, HashMap.class);
            parseFields(jsonMap, compilationUnit);
        } catch (IOException e) {
        }
        return compilationUnit;
    }

    public CompilationUnit parseJsonContentIntoJavaFile(String content) {
        CompilationUnit compilationUnit = new CompilationUnit();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(content, HashMap.class);
            parseFields(jsonMap, compilationUnit);
        } catch (IOException e) {
        }
        return compilationUnit;
    }

    private void parseFields(Map<String, Object> jsonMap, CompilationUnit compilationUnit) {
        compilationUnit.setPackageDeclaration("org.meveo.model.customEntities");
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass((String) jsonMap.get("id")).setPublic(true);
        if (classDeclaration != null) {
            Collection<FieldDeclaration> fds = new ArrayList<>();
            if (jsonMap.containsKey("storages")) {
                compilationUnit.addImport("org.meveo.model.persistence.DBStorageType");
                FieldDeclaration fd = new FieldDeclaration();
                VariableDeclarator variableDeclarator = new VariableDeclarator();
                variableDeclarator.setName("storages");
                variableDeclarator.setType("DBStorageType");
                fd.setModifiers(Modifier.Keyword.PRIVATE);
                fd.addVariable(variableDeclarator);
                classDeclaration.addMember(fd);
                ((ArrayList<FieldDeclaration>) fds).add(fd);
            }
            Map<String, Object> items = (Map<String, Object>) jsonMap.get("properties");
            if (items != null) {
                for (Map.Entry<String, Object> item : items.entrySet()) {
                    String code = item.getKey();
                    Map<String, Object> values = (Map<String, Object>) item.getValue();
                    FieldDeclaration fd = new FieldDeclaration();
                    VariableDeclarator vd = new VariableDeclarator();
                    vd.setName(code);
                    if (values.get("type") != null) {
                        if (values.get("type").equals("array")) {
                            compilationUnit.addImport("java.util.List");
                            Map<String, Object> value = (Map<String, Object>) values.get("items");
                            if (value.containsKey("$ref")) {
                                String ref = (String) value.get("$ref");
                                if (ref != null) {
                                    String[] data = ref.split("/");
                                    if (data.length > 0) {
                                        String name = data[data.length - 1];
                                        compilationUnit.addImport("org.meveo.model.customEntities." + name);
                                        vd.setType("List<" + name + ">");
                                    }
                                }
                            } else if (value.containsKey("type")) {
                                if (value.get("type").equals("number")) {
                                    vd.setType("List<Long>");
                                } else {
                                    String typeItem = (String) value.get("type");
                                    typeItem = Character.toUpperCase(typeItem.charAt(0)) + typeItem.substring(1);
                                    vd.setType("List<" + typeItem + ">");
                                }
                            }
                        } else if (values.get("type").equals("object")) {
                            compilationUnit.addImport("java.util.Map");
                            vd.setType("Map<String, String>");
                        } else if (values.get("type").equals("number")) {
                            vd.setType("Long");
                        } else if ((values.get("format")!= null )) {
                            if (values.get("format").equals("date-time")) {
                                compilationUnit.addImport("java.util.Date");
                                vd.setType("Date");
                            }
                        } else {
                            String type = (String) values.get("type");
                            type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
                            vd.setType(type);
                        }
                    }
                    fd.addVariable(vd);
                    fd.setModifiers(Modifier.Keyword.PRIVATE);
                    if (values.get("nullable").equals(false)) {
                        fd.addMarkerAnnotation("NotNull");
                    }
                    classDeclaration.addMember(fd);
                    ((ArrayList<FieldDeclaration>) fds).add(fd);
                }
            }
            for (FieldDeclaration fieldDeclaration : fds) {
                if (fieldDeclaration != null) {
                    fieldDeclaration.createGetter();
                    fieldDeclaration.createSetter();
                }
            }

        }
    }
}
