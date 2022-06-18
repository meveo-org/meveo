package org.meveo.service.crm.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.meveo.model.CustomEntity;
import org.meveo.model.CustomRelation;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.customEntities.MeveoMatrix;
import org.meveo.model.customEntities.annotations.Relation;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
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
	private CustomFieldTemplateService customFieldService;
	
	@Inject
	private Logger log;

    private Map<String, Object> jsonMap;

    /**
     * Note : this method is only used for test purpose
     * 
     * @param file the file to parse
     * @return the parsed file
     */
    @SuppressWarnings("unchecked")
    public CompilationUnit parseJavaFile(String file) {
        CompilationUnit compilationUnit = new CompilationUnit();
        try {
            File sourceDir = new File(file);
            byte[] mapData = Files.readAllBytes(sourceDir.toPath());
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(mapData, HashMap.class);
            parseFields(jsonMap, compilationUnit, new CustomEntityTemplate(), Map.of());
        } catch (IOException e) {
        }
        return compilationUnit;
    }
    
    @SuppressWarnings("unchecked")
	public CompilationUnit parseJsonContentIntoJavaFile(String content, CustomRelationshipTemplate template) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.addImport(CustomRelation.class);
		compilationUnit.addImport(List.class);
        var fields = customFieldService.findByAppliesTo(template.getAppliesTo());
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(content, HashMap.class);
            
            parseFields(jsonMap, compilationUnit, template, fields);

            compilationUnit.getClassByName((String) jsonMap.get("id"))
	    		.ifPresent(cl -> {
		            // Generate source
	    			var sourceField = cl.addField(template.getStartNode().getCode(), "source", Modifier.Keyword.PRIVATE);
	    			sourceField.addAnnotation(JsonIgnore.class);

	    			// Generate target
	    			var targetField = cl.addField(template.getEndNode().getCode(), "target", Modifier.Keyword.PRIVATE);
	    			targetField.addAnnotation(JsonIgnore.class);
	    			
	    			cl.addConstructor(Modifier.Keyword.PUBLIC)
    					.setBody(JavaParser.parseBlock("{\n\tthis.source = source;\n\tthis.target=target;\n}"))
	    				.addParameter(template.getStartNode().getCode(), "source")
	    				.addParameter(template.getEndNode().getCode(), "target");
	    			
	    			sourceField.createGetter();
	    			sourceField.createSetter();
	    			
	    			targetField.createGetter();
	    			targetField.createSetter();
	    		});
            
            compilationUnit.getClassByName((String) jsonMap.get("id"))
    		.ifPresent(cl -> {
                cl.tryAddImportToParentCompilationUnit(CustomRelation.class);
    			cl.tryAddImportToParentCompilationUnit(JsonIgnore.class);

                cl.addImplementedType("CustomRelation<" + template.getStartNode().getCode() + "," + template.getEndNode().getCode() + ">");
    			cl.getMethodsByName("getUuid")
    				.stream()
    				.findFirst()
    				.ifPresent(method -> method.addAnnotation(Override.class));
    			
    			var getCetCode = cl.addMethod("getCrtCode", Keyword.PUBLIC);
    			getCetCode.addAnnotation(Override.class);
    			getCetCode.setType(String.class);
    			var getCetCodeBody = new BlockStmt();
    			getCetCodeBody.getStatements().add(new ReturnStmt('"' + template.getCode() + '"'));
    			getCetCode.setBody(getCetCodeBody);
    		});
            
        } catch (IOException e) {
        	log.error("Failed to generate class for CRT {}", template, e);
        }
        
        return compilationUnit;
    }

    public CompilationUnit parseJsonContentIntoJavaFile(String content, CustomEntityTemplate template) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.addImport(CustomEntity.class);
		compilationUnit.addImport(List.class);

        var fields = customFieldService.findByAppliesTo(template.getAppliesTo());
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(content, HashMap.class);
            parseFields(jsonMap, compilationUnit, template, fields);
            
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
    			cl.tryAddImportToParentCompilationUnit(JsonIgnore.class);

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
    			
    	        if(StringUtils.isNotBlank(template.getIsEqualFn())) {
    	        	cl.addMethod("isEqual", Keyword.PUBLIC)
	        			.addAnnotation(Override.class)
    	        		.setType(boolean.class)
    	        		.addParameter(new Parameter()
    	        			.setType("CustomEntity")
    	        			.setName("other"))
	        			.setBody(JavaParser.parseBlock(template.getIsEqualFn()));
    	        }
    		});
            
        } catch (IOException e) {
        	log.error("Failed to generate class for CET {}", template, e);
        }
        
        return compilationUnit;
    }

    private void parseFields(Map<String, Object> jsonMap, CompilationUnit compilationUnit, CustomModelObject template, Map<String, CustomFieldTemplate> fieldsDefinition) {
        compilationUnit.setPackageDeclaration("org.meveo.model.customEntities");
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass((String) jsonMap.get("id")).setPublic(true);
        Collection<FieldDeclaration> fds = new ArrayList<>();
        
        // Generate default constructor
        classDeclaration.addConstructor(Modifier.Keyword.PUBLIC);

        if(template instanceof CustomEntityTemplate) {
        	CustomEntityTemplate cet = (CustomEntityTemplate) template;
        	if (cet.getNeo4JStorageConfiguration() != null && cet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {

            	// Handle primitive entity if value field is not defined
            	FieldDeclaration fd = new FieldDeclaration();
            	VariableDeclarator variableDeclarator = new VariableDeclarator();
            	variableDeclarator.setName("value");

            	switch (cet.getNeo4JStorageConfiguration().getPrimitiveType()) {
    	        	case DATE:
    	        		variableDeclarator.setType("Instant");
    	        		compilationUnit.addImport(Instant.class);
    	        		break;
    	        	case DOUBLE:
    	        		variableDeclarator.setType("Double");
    	        		compilationUnit.addImport(Double.class);
    	        		break;
    	        	case LONG:
    	        		variableDeclarator.setType("Long");
    	        		compilationUnit.addImport(Long.class);
    	        		break;
    	        	case STRING:
    	        		variableDeclarator.setType("String");
    	        		compilationUnit.addImport(String.class);
    	        		break;
    	        	default:
    	        		variableDeclarator.setType("Object");
    	        		break;
            	}

            	if(fieldsDefinition.get("value") == null) {
            		fd.setModifiers(Modifier.Keyword.PRIVATE);
            		fd.addVariable(variableDeclarator);
            		fd.addSingleMemberAnnotation(JsonProperty.class, "required = true");
            		compilationUnit.addImport(JsonProperty.class);
            		classDeclaration.addMember(fd);
            		((ArrayList<FieldDeclaration>) fds).add(fd);
            	}

            	// Generate constructor with the value
            	classDeclaration.addConstructor(Modifier.Keyword.PUBLIC)
    	        	.addParameter(new Parameter(variableDeclarator.getType(), "value"))
    	        	.setBody(JavaParser.parseBlock("{\n this.value = value; \n}"));

            } else {
            	VariableDeclarator variableDeclarator = new VariableDeclarator();
            	variableDeclarator.setType("String");
            	
            	// Generate constructor with the value
            	classDeclaration.addConstructor(Modifier.Keyword.PUBLIC)
    	        	.addParameter(new Parameter(variableDeclarator.getType(), "uuid"))
    	        	.setBody(JavaParser.parseBlock("{\n this.uuid = uuid; \n}"));
            }
        }
        
        if (classDeclaration != null) {
            FieldDeclaration field = new FieldDeclaration();
            VariableDeclarator variable = new VariableDeclarator();
            variable.setName("uuid");
            variable.setType("String");
            field.setModifiers(Modifier.Keyword.PRIVATE);
            field.addVariable(variable);
            classDeclaration.addMember(field);
            ((ArrayList<FieldDeclaration>) fds).add(field);
            if (jsonMap.containsKey("storages")) {
                compilationUnit.addImport(DBStorageType.class);
                FieldDeclaration fd = new FieldDeclaration();
                fd.addAnnotation(JsonIgnore.class);
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
                    
                     CustomFieldTemplate cft = fieldsDefinition.get(code);
                     if (cft.getStorageType().equals(CustomFieldStorageTypeEnum.MATRIX)) {
                    	 fd = getMatrixField(cft, compilationUnit);
                    	 fds.add(fd);
                    	 classDeclaration.addMember(fd);
                    	 continue;
                     }
                    
                    // Add @JsonProperty annotation
					if (values.containsKey("nullable") && !Boolean.parseBoolean(values.get("nullable").toString())) {
						fd.addSingleMemberAnnotation(JsonProperty.class, "required = true");
						compilationUnit.addImport(JsonProperty.class);
					}
					
                    if (values.get("type") != null) {
                        if (values.get("type").equals("array")) {
                            compilationUnit.addImport("java.util.List");
                            Map<String, Object> value = (Map<String, Object>) values.get("items");
                            if (value.containsKey("$ref")) {
            					// Handle entity references
            					var fieldDefinition = fieldsDefinition.get(code);
            					if(fieldDefinition != null && fieldDefinition.getRelationship() != null) {
            						String crtCode = fieldDefinition.getRelationship().getCode();
            						var relationFields = customFieldService.findByAppliesTo(fieldDefinition.getRelationship().getAppliesTo());
            						// If CRT has no relation, directly use the target node as field type
            						if(relationFields == null || relationFields.isEmpty()) {
										fd.addSingleMemberAnnotation(Relation.class, '"' + crtCode + '"');
	                					compilationUnit.addImport(Relation.class);
            						} else {
            							var fieldDeclaration = classDeclaration.addPrivateField("List<" + crtCode + ">", code);
            		                    ((ArrayList<FieldDeclaration>) fds).add(fieldDeclaration);
            							continue;
            						}
            					}
            					
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
                            
                            vd.setInitializer(JavaParser.parseExpression("new ArrayList<>()"));
                            compilationUnit.addImport(ArrayList.class);
                        } else if (values.get("type").equals("object")) {
                            compilationUnit.addImport("java.util.Map");
                            Map<String, Object> patternProperties = (Map<String, Object>) values.get("patternProperties");
                            Map<String, Object> properties = (Map<String, Object>) (patternProperties != null ? patternProperties.get("^.*$") : values.get("properties"));
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
                            
                            vd.setInitializer(JavaParser.parseExpression("new HashMap<>()"));
                            compilationUnit.addImport(HashMap.class);
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
                        } else {
                            String type = (String) values.get("type");
                            type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
                            vd.setType(type);
                        }

                    } else if (values.get("$ref") != null) {
    					var fieldDefinition = fieldsDefinition.get(code); 
    					if(fieldDefinition != null && fieldDefinition.getRelationship() != null) {
							var relationFields = customFieldService.findByAppliesTo(fieldDefinition.getRelationship().getAppliesTo());
							String crtCode = fieldDefinition.getRelationship().getCode();
							// If CRT has no relation, directly use the target node as field type
							if(relationFields == null || relationFields.isEmpty()) {
		    					// Add @Relation annotation
								fd.addSingleMemberAnnotation(Relation.class, '"' + crtCode + '"');
	        					compilationUnit.addImport(Relation.class);
							} else {
								var fieldDeclaration = classDeclaration.addPrivateField(crtCode, code);
			                    ((ArrayList<FieldDeclaration>) fds).add(fieldDeclaration);
								continue;
							}
    					}
    					
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
    
    private FieldDeclaration getMatrixField(CustomFieldTemplate cft, CompilationUnit compilationUnit) {
   	 	compilationUnit.addImport(MeveoMatrix.class);

    	FieldDeclaration fieldDeclaration = new FieldDeclaration();
    	VariableDeclarator vd = new VariableDeclarator();
        vd.setName(cft.getCode());
        fieldDeclaration.addVariable(vd);
        fieldDeclaration.setModifiers(Keyword.PRIVATE);
        
        switch (cft.getFieldType()) {
		case BOOLEAN:
			vd.setType("MeveoMatrix<Boolean>");
			compilationUnit.addImport(Boolean.class);
			break;
		case CHILD_ENTITY:
			//TODO
			break;
		case DATE:
			vd.setType("MeveoMatrix<Instant>");
			compilationUnit.addImport(Instant.class);
			break;
		case DOUBLE:
			vd.setType("MeveoMatrix<Double>");
			compilationUnit.addImport(Double.class);
			break;
		case LONG:
			vd.setType("MeveoMatrix<Long>");
			compilationUnit.addImport(Long.class);
			break;
		case BINARY:
		case ENTITY:
		case EXPRESSION:
		case LIST:
		case LONG_TEXT:
		case MULTI_VALUE:
		case SECRET:
		case STRING:
		case TEXT_AREA:
			vd.setType("MeveoMatrix<String>");
			break;
		default:
			break;
        
        }
        
        return fieldDeclaration;
    }
}
