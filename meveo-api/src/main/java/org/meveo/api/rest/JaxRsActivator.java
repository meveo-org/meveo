package org.meveo.api.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.interceptors.encoding.GZIPDecodingInterceptor;
import org.jboss.resteasy.plugins.interceptors.encoding.GZIPEncodingInterceptor;
import org.meveo.api.rest.cache.CacheRsImpl;
import org.meveo.api.rest.custom.impl.CustomTableRsImpl;
import org.meveo.api.rest.custom.impl.CustomTableRsRelationImpl;
import org.meveo.api.rest.custom.impl.GraphQLRsImpl;
import org.meveo.api.rest.custom.impl.Neo4JPersistenceRs;
import org.meveo.api.rest.filter.PragmaRemover;
import org.meveo.api.rest.filter.RESTCorsRequestFilter;
import org.meveo.api.rest.filter.RESTCorsResponseFilter;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.monitoring.MonitoringRs;
import org.meveo.api.rest.persistence.PersistenceRs;
import org.meveo.api.rest.swagger.SwaggerApiDefinition;
import org.meveo.service.communication.impl.SseManager;
import org.meveo.util.Version;
import org.reflections.Reflections;

import io.swagger.jaxrs.listing.SwaggerSerializers;

/**
 * @author Edward P. Legaspi
 **/
@ApplicationPath("/api/rest")
public class JaxRsActivator extends Application {

	private Set<Class<?>> resources;

	public JaxRsActivator() {
		if (resources == null) {
			Reflections reflections = new Reflections("org.meveo.api.rest");
			Set<Class<? extends BaseRs>> allClasses = reflections.getSubTypesOf(BaseRs.class);

			resources = new HashSet<>(allClasses);
			resources.add(RESTCorsRequestFilter.class);
			resources.add(RESTCorsResponseFilter.class);
			resources.add(PragmaRemover.class);
			resources.add(JaxRsExceptionMapper.class);
			resources.add(JacksonJsonProvider.class);
			resources.add(Neo4JPersistenceRs.class);
			resources.add(CustomTableRsImpl.class);
			resources.add(CustomTableRsRelationImpl.class);
			resources.add(PersistenceRs.class);
			resources.add(SwaggerSerializers.class);
			resources.add(SwaggerApiDefinition.class);
			resources.add(GraphQLRsImpl.class);
			resources.add(CacheRsImpl.class);
            resources.add(SseManager.class);
            resources.add(GZIPDecodingInterceptor.class);
            resources.add(GZIPEncodingInterceptor.class);
            resources.add(MonitoringRs.class);
		}


		MeveoBeanConfig beanConfig = new MeveoBeanConfig();
		beanConfig.setSchemes(new String[] { "http", "https" });
		beanConfig.setBasePath("/api/rest");
		beanConfig.setVersion(Version.appVersion);
		beanConfig.setTitle("Meveo");
		beanConfig.setScan(true);
		beanConfig.setPrettyPrint(true);
		beanConfig.setClasses(resources);
		beanConfig.setScannerId("meveo");
		beanConfig.setConfigId("meveo");
		beanConfig.setUsePathBasedConfig(true);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

}
