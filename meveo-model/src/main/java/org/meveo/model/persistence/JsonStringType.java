package org.meveo.model.persistence;

import java.util.Properties;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.usertype.DynamicParameterizedType;

public class JsonStringType extends AbstractSingleColumnStandardBasicType<Object> implements DynamicParameterizedType {

    private static final long serialVersionUID = -7393846020207110466L;

    public JsonStringType() {
        super(JsonStringSqlTypeDescriptor.INSTANCE, new JsonTypeDescriptor());
    }

    @Override
    public String getName() {
        return JsonTypes.JSON;
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    @Override
    public void setParameterValues(Properties parameters) {
        ((JsonTypeDescriptor) getJavaTypeDescriptor()).setParameterValues(parameters);
    }


}
