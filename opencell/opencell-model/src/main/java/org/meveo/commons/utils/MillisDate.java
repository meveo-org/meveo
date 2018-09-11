package org.meveo.commons.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MillisDate implements JsonDeserializer<Date>,JsonSerializer<Date> {
	
	
    
	@Override
	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {		
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			return  sdf.parse(json.getAsString());
		}catch(Exception e){			
		}
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return  sdf.parse(json.getAsString());
		}catch(Exception e){			
		}		
		return  new Date(new Long(json.getAsString()));
	}

	@Override
	public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {	
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");		
		return  new JsonPrimitive(sdf.format(src));
	}
  }