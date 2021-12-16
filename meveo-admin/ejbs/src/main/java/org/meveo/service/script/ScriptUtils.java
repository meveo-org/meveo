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

package org.meveo.service.script;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.meveo.model.scripts.Accessor;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.ScriptInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.JavadocBlockTag;

/**
 * Utility class for script.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * 
 * @see ScriptInstance
 * @version 6.9.0
 */
public class ScriptUtils {

	private static Logger logger = LoggerFactory.getLogger(ScriptUtils.class);

	public static ClassAndValue findTypeAndConvert(String type, String value) {
		ClassAndValue classAndValue = new ClassAndValue();

		// Try to find boxed type
		switch (type) {
		case "int":
			classAndValue.setClass(int.class);
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(0);
				break;
			}
		case "Integer":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(null);
				break;
			}
			classAndValue.setValue(Integer.parseInt(value));
			break;

		case "double":
			classAndValue.setClass(double.class);
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(0.0);
				break;
			}
		case "Double":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(null);
				break;
			}
			classAndValue.setValue(Double.parseDouble(value));
			break;

		case "long":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(0L);
				break;
			}
			classAndValue.setClass(long.class);
		case "Long":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(null);
				break;
			}
			classAndValue.setValue(Long.parseLong(value));
			break;

		case "byte":
			classAndValue.setClass(byte.class);
		case "Byte":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(null);
				break;
			}
			classAndValue.setValue(Byte.parseByte(value));
			break;

		case "short":
			classAndValue.setClass(short.class);
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(0);
				break;
			}
		case "Short":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(null);
				break;
			}
			classAndValue.setValue(Short.parseShort(value));
			break;

		case "float":
			classAndValue.setClass(float.class);
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(0.0f);
				break;
			}
		case "Float":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(null);
				break;
			}
			classAndValue.setValue(Float.parseFloat(value));
			break;

		case "boolean":
			classAndValue.setClass(boolean.class);
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(false);
				break;
			}
		case "Boolean":
			if (StringUtils.isBlank(value)) {
				classAndValue.setValue(null);
				break;
			}
			classAndValue.setValue(Boolean.parseBoolean(value));
			break;

		default:
			classAndValue.setValue(value);
			logger.warn("Type {} not handled for string parsing", type);
		}

		// Case where the class if boxed or we don't handle it yet
		if (classAndValue.clazz == null) {
			classAndValue.clazz = classAndValue.value.getClass();
		}

		return classAndValue;
	}

	public static class ClassAndValue {
		private Object value;
		private Class<?> clazz;

		public Object getValue() {
			return value;
		}

		public Class<?> getTypeClass() {
			return clazz;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public void setClass(Class<?> setterClass) {
			this.clazz = setterClass;
		}
	}

	/**
	 * @param methods
	 * @return
	 */
	public static List<Accessor> getGetters(final List<MethodDeclaration> methods) {
		return methods.stream().filter(e -> e.getNameAsString().startsWith(Accessor.GET) || e.getNameAsString().startsWith(Accessor.IS))
				.filter(e -> e.getAnnotationByClass(JsonIgnore.class).isEmpty())
				.filter(e -> e.getModifiers().stream().anyMatch(modifier -> modifier.getKeyword().equals(Modifier.Keyword.PUBLIC))).filter(e -> e.getParameters().isEmpty())
				.map(methodDeclaration -> {
					Accessor getter = new Accessor();
					String accessorFieldName;
					if (methodDeclaration.getNameAsString().startsWith(Accessor.GET)) {
						accessorFieldName = methodDeclaration.getNameAsString().substring(3);
					} else {
						accessorFieldName = methodDeclaration.getNameAsString().substring(2);
					}
					getter.setName(Character.toLowerCase(accessorFieldName.charAt(0)) + accessorFieldName.substring(1));
					getter.setMethodName(methodDeclaration.getNameAsString());
					getter.setType(methodDeclaration.getTypeAsString());
					methodDeclaration.getComment().ifPresent(comment -> comment.ifJavadocComment(javadocComment -> {
						javadocComment.parse().getBlockTags().stream().filter(e -> e.getType() == JavadocBlockTag.Type.RETURN).findFirst()
								.ifPresent(javadocBlockTag -> getter.setDescription(javadocBlockTag.getContent().toText()));
					}));
					return getter;
				}).collect(Collectors.toList());
	}

	/**
	 * @param methods
	 * @return
	 */
	public static List<Accessor> getSetters(final List<MethodDeclaration> methods) {
		return methods.stream().filter(e -> e.getNameAsString().startsWith(Accessor.SET))
				.filter(e -> e.getAnnotationByClass(JsonIgnore.class).isEmpty())
				.filter(e -> e.getModifiers().stream().anyMatch(modifier -> modifier.getKeyword().equals(Modifier.Keyword.PUBLIC))).filter(e -> e.getParameters().size() == 1)
				.map(methodDeclaration -> {
					Accessor setter = new Accessor();
					String accessorFieldName = methodDeclaration.getNameAsString().substring(3);
					setter.setName(Character.toLowerCase(accessorFieldName.charAt(0)) + accessorFieldName.substring(1));
					setter.setType(methodDeclaration.getParameter(0).getTypeAsString());
					setter.setMethodName(methodDeclaration.getNameAsString());
					methodDeclaration.getComment().ifPresent(comment -> comment.ifJavadocComment(javadocComment -> {
						javadocComment.parse().getBlockTags().stream().filter(e -> e.getType() == JavadocBlockTag.Type.PARAM).findFirst()
								.ifPresent(javadocBlockTag -> setter.setDescription(javadocBlockTag.getContent().toText()));
					}));
					return setter;
				}).collect(Collectors.toList());
	}

	public static String findScriptVariableType(Function function, String variableName) {

		String result = "object";
		if (function instanceof CustomScript) {
			CustomScript customScript = (CustomScript) function;
			CompilationUnit compilationUnit;

			try {
				compilationUnit = JavaParser.parse(customScript.getScript());
				final ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit.getChildNodes().stream().filter(e -> e instanceof ClassOrInterfaceDeclaration)
						.map(e -> (ClassOrInterfaceDeclaration) e).findFirst().get();

				final List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMembers().stream().filter(e -> e instanceof MethodDeclaration)
						.map(e -> (MethodDeclaration) e).collect(Collectors.toList());

				final List<Accessor> getters = getGetters(methods);

				Optional<Accessor> returnMethod = getters.stream().filter(e -> e.getName().equals(variableName)).findAny();

				if (returnMethod.isPresent()) {
					result = returnMethod.get().getType().toLowerCase();
				}

			} catch (Exception e) {
			}
		}

		return result;
	}
}
