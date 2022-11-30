package org.meveo.api.rest;

import java.util.Set;

import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.DefaultReaderConfig;
import io.swagger.jaxrs.config.ReaderConfig;

public class MeveoBeanConfig extends BeanConfig {
	
	private static final ReaderConfig readerConfig;
	
	static {
		readerConfig = new DefaultReaderConfig();
		((DefaultReaderConfig) readerConfig).setScanAllResources(true);
	}
	
	private Set<Class<?>> classes;
	
	public MeveoBeanConfig() {
		reader = new Reader(reader.getSwagger(), readerConfig);
	}
	
	public void setClasses(Set<Class<?>> classes) {
		this.classes = classes;
	}

	@Override
	public Set<Class<?>> classes() {
		return classes;
	}

}
