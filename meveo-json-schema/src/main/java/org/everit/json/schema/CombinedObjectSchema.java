package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * @author clement.bareth
 */
public class CombinedObjectSchema extends ObjectSchema {

	private CombinedSchema combinedSchema;

	/**
	 * Constructor.
	 *
	 * @param builder the builder object containing validation criteria
	 */
	public CombinedObjectSchema(Builder builder) {
		super(builder);
	}
	
	public CombinedSchema getCombinedSchema() {
		return combinedSchema;
	}

	public void setCombinedSchema(CombinedSchema combinedSchema) {
		this.combinedSchema = combinedSchema;
	}

	@Override
	protected void describePropertiesTo(JSONPrinter writer) {
		if(combinedSchema != null) combinedSchema.describePropertiesTo(writer);
		super.describePropertiesTo(writer);
	}

	public static class Factory {

		public static CombinedObjectSchema get(ObjectSchema.Builder objectSchemaBuilder, CombinedSchema combinedSchema){
			final CombinedObjectSchema schema = new CombinedObjectSchema(objectSchemaBuilder);
			schema.combinedSchema = combinedSchema;
			return schema;
		}

	}

}
