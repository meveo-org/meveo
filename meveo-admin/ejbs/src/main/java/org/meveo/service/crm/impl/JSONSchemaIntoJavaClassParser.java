package org.meveo.service.crm.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.Hibernate;
import org.meveo.model.CustomEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

/**
 * Parse a cet map into a java source code.
 *
 * @author Edward P. Legaspi
 * @author Clément Bareth
 * @since 6.8.0
 * @version 6.10.0
 */
public class JSONSchemaIntoJavaClassParser {
	
	@Inject
	private JSONSchemaGenerator schemaGenerator;
	
	@Inject
	private CustomEntityTemplateService cetService;
	
	@Inject
	private Logger log;

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

    public CompilationUnit parseJsonContentIntoJavaFile(String content, CustomEntityTemplate template) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.addImport(CustomEntity.class);
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(content, HashMap.class);
            parseFields(jsonMap, compilationUnit);
            
            if(template.getSuperTemplate() != null) {
            	var parentTemplate = Hibernate.isInitialized(template.getSuperTemplate()) ? 
            			template.getSuperTemplate() :
            			cetService.findById(template.getSuperTemplate().getId());
            			
            	var parentClass = JavaParser.parseClassOrInterfaceType(parentTemplate.getCode());
            	compilationUnit.getClassByName((String) jsonMap.get("id"))
            		.ifPresent(cl -> {
            			compilationUnit.addImport("org.meveo.model.customEntities." + parentTemplate.getCode());
            			cl.addExtendedType(parentClass);
            		});
            }
            
            compilationUnit.getClassByName((String) jsonMap.get("id"))
    		.ifPresent(cl -> {
                cl.addImplementedType(CustomEntity.class);
    			
    			cl.getMethodsByName("getUuid")
    				.stream()
    				.findFirst()
    				.ifPresent(method -> method.addAnnotation(Override.class));
    			
    			var getCetCode = cl.addMethod("getCetCode", Keyword.PUBLIC);
    			getCetCode.addAnnotation(Override.class);
    			getCetCode.setType(String.class);
    			var getCetCodeBody = new BlockStmt();
    			getCetCodeBody.getStatements().add(new ReturnStmt('"' + template.getCode() + '"'));
    			getCetCode.setBody(getCetCodeBody);
    		});
            
        } catch (IOException e) {
        	
        }
        
        return compilationUnit;
    }

    private void parseFields(Map<String, Object> jsonMap, CompilationUnit compilationUnit) {
        compilationUnit.setPackageDeclaration("org.meveo.model.customEntities");
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass((String) jsonMap.get("id")).setPublic(true);
        if (classDeclaration != null) {
            Collection<FieldDeclaration> fds = new ArrayList<>();
            FieldDeclaration field = new FieldDeclaration();
            VariableDeclarator variable = new VariableDeclarator();
            variable.setName("uuid");
            variable.setType("String");
            field.setModifiers(Modifier.Keyword.PRIVATE);
            field.addVariable(variable);
            classDeclaration.addMember(field);
            ((ArrayList<FieldDeclaration>) fds).add(field);
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
                                        
                                        try {
                                        	Class.forName(name);
                                        	compilationUnit.addImport(name);
                                            String[] className = name.split("\\.");
                                            name = className[className.length -1];
                                        } catch (ClassNotFoundException e) {
                                            compilationUnit.addImport("org.meveo.model.customEntities." + name);
                                        }
                                        
                                        vd.setType("List<" + name + ">");
                                    }
                                }
                            } else if (value.containsKey("type")) {
                                if (value.get("type").equals("integer")) {
                                    vd.setType("List<Long>");
                                } else if (value.get("type").equals("number")) {
                                    vd.setType("List<Double>");
                                } else {
                                    String typeItem = (String) value.get("type");
                                    typeItem = Character.toUpperCase(typeItem.charAt(0)) + typeItem.substring(1);
                                    vd.setType("List<" + typeItem + ">");
                                }
                            } else if (value.containsKey("enum")) {
                                vd.setType("List<String>");
                            }
                        } else if (values.get("type").equals("object")) {
                            compilationUnit.addImport("java.util.Map");
                            Map<String, Object> patternProperties = (Map<String, Object>) values.get("patternProperties");
                            Map<String, Object> properties = (Map<String, Object>) patternProperties.get("^.*$");
                            if (properties.containsKey("$ref")) {
                                String ref = (String) properties.get("$ref");
                                if (ref != null) {
                                    String[] data = ref.split("/");
                                    if (data.length > 0) {
                                        String name = data[data.length - 1];
                                        try {
                                        	Class.forName(name);
                                        	compilationUnit.addImport(name);
                                            String[] className = name.split("\\.");
                                            name = className[className.length -1];
                                        } catch (ClassNotFoundException e) {
                                            compilationUnit.addImport("org.meveo.model.customEntities." + name);
                                        }
                                        vd.setType("Map<String, " + name + ">");
                                    }
                                }
                            } else if (properties.containsKey("type")) {
                                if (properties.get("type").equals("string")) {
                                    vd.setType("Map<String, String>");
                                } else {
                                    vd.setType("Map<String, Object>");
                                }
                            }
                        } else if (values.get("type").equals("integer")) {
                            if (code.equals("count")) {
                                vd.setType("Integer");
                            } else {
                                vd.setType("Long");
                            }
                        } else if (values.get("type").equals("number")) {
                            vd.setType("Double");
                        } else if ((values.get("format") != null)) {
                            if (values.get("format").equals("date-time")) {
                                compilationUnit.addImport("java.time.Instant");
                                vd.setType("Instant");
                            }
                        } else if (values.get("default") != null && (values.get("default").equals("true") || values.get("default").equals("false"))) {
                            vd.setType("Boolean");
                        } else {
                            String type = (String) values.get("type");
                            type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
                            vd.setType(type);
                        }

                    } else if (values.get("$ref") != null) {
                        String[] data = ((String) values.get("$ref")).split("/");
                        if (data.length > 0) {
                            String name = data[data.length - 1];
                            // Handle cases where prefixed by 'org.meveo.model.customEntities.CustomEntityTemplate -'
                            name = CustomFieldTemplate.retrieveCetCode(name);
                            
                            try {
                            	Class.forName(name);
                            	compilationUnit.addImport(name);
                                String[] className = name.split("\\.");
                                name = className[className.length -1];
                            } catch (ClassNotFoundException e) {
                                compilationUnit.addImport("org.meveo.model.customEntities." + name);
                            }
                            vd.setType(name);
                        }
                    } else if (values.get("enum") != null && values.get("type") == null) {
                        vd.setType("String");
                    }

                    fd.addVariable(vd);
                    fd.setModifiers(Modifier.Keyword.PRIVATE);
                    if (values.get("nullable").equals(false)) {
                        fd.addMarkerAnnotation("NotNull");
                        compilationUnit.addImport("javax.validation.constraints.NotNull");
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
