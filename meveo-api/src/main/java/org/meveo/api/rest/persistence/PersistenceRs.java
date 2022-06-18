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

package org.meveo.api.rest.persistence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.PersistenceDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.rest.RestUtils;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.interfaces.Entity;
import org.meveo.interfaces.EntityOrRelation;
import org.meveo.interfaces.EntityRelation;
import org.meveo.model.BaseEntity;
import org.meveo.model.admin.User;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.persistence.CrossStorageTransaction;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.scheduler.AtomicPersistencePlan;
import org.meveo.persistence.scheduler.CyclicDependencyException;
import org.meveo.persistence.scheduler.OrderedPersistenceService;
import org.meveo.persistence.scheduler.PersistedItem;
import org.meveo.persistence.scheduler.SchedulingService;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.security.PasswordUtils;
import org.meveo.service.admin.impl.UserService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.hierarchy.impl.UserHierarchyLevelService;
import org.meveo.service.storage.RepositoryService;
import org.meveo.util.PersistenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Clement Bareth
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.13
 */
@Path("/{repository}/persistence")
@Api("PersistenceRs")
public class PersistenceRs {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PersistenceRs.class);

	@Inject
	private SchedulingService schedulingService;

	@Inject
	private OrderedPersistenceService<CrossStorageService> scheduledPersistenceService;

	@Inject
	private CrossStorageService crossStorageService;

	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	@CurrentUser
	private MeveoUser currentUser;

	@Inject
	private UserHierarchyLevelService userHierarchyLevelService;

	@Inject
	private UserService userService;
	
	@Inject
	private CrossStorageTransaction crossStorageTx;
	
	@Inject
	private Logger log;
	
	@Inject
	private CustomEntityInstanceService customEntityInstanceService;
	
	@Inject
	private CustomTableService customTableService;

	@PathParam("repository")
	private String repositoryCode;

	private java.nio.file.Path tempDir;

	@POST
	@Path("/{cetCode}/list")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation("List data for a given CET")
	public Response list(@HeaderParam("Base64-Encode") @ApiParam("Base 64 encode") boolean base64Encode,
			@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode,
			@QueryParam("withCount") @ApiParam("If true returns the count of entities") Boolean withCount,
			@QueryParam("singleValue") @ApiParam("Whether to return only one value") Boolean singleValue,
			@ApiParam("Pagination configuration information") PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException, IOException {
		final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
		if (customEntityTemplate == null) {
			throw new NotFoundException("Custom entity template with code " + cetCode + " does not exists");
		}
		
		if (!currentUser.hasRole(customEntityTemplate.getReadPermission())) {
			throw new ForbiddenException();
		}

		if (paginationConfiguration == null) {
			paginationConfiguration = new PaginationConfiguration();
		}

		Repository repository = repositoryService.findByCode(repositoryCode);

		hasAccessToRepository(repository);

		List<Map<String, Object>> data = crossStorageService.find(repository, customEntityTemplate, paginationConfiguration);

		for (Map<String, Object> values : data) {
			convertFiles(customEntityTemplate, values, base64Encode);
		}

		List<Map<String, Object>> entities = data.stream().map(this::serializeJpaEntities).collect(Collectors.toList());
		
		if (customEntityTemplate.getAvailableStorages() == null || customEntityTemplate.getAvailableStorages().isEmpty()) {
			return Response.ok(new PersistenceListResult()).build();
		}
		
		if (withCount != null && withCount) {
			Long totalCount = 0L;
			
			if (customEntityTemplate.getSqlStorageConfiguration().isStoreAsTable()) {
				totalCount = customTableService.count(repository.getSqlConfigurationCode(), SQLStorageConfiguration.getDbTablename(customEntityTemplate), paginationConfiguration);
						
			} else {
				totalCount = customEntityInstanceService.count(paginationConfiguration);
			}
			
			PersistenceListResult result = new PersistenceListResult();
			result.setCount(totalCount.intValue());
			result.setResult(entities);
			return Response.ok(result).build();

		} else if(singleValue != null && singleValue) {
			if(entities.isEmpty()) {
				return Response.status(404).build();
			} else {
				return Response.ok(entities.get(0)).build();
			}
		}
		
		return Response.ok(entities).build();
	}

	@DELETE
	@Path("/{cetCode}/{uuid}")
	@ApiOperation(value = "Delete persistence by cet code")
	public Response delete(@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @PathParam("uuid") @ApiParam("uuid") String uuid)
			throws BusinessException, EntityDoesNotExistsException {
		final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
		if (customEntityTemplate == null) {
			throw new NotFoundException("Custom entity template with code " + cetCode + " does not exists");
		}

		final Repository repository = repositoryService.findByCode(repositoryCode);
		
		hasAccessToRepository(repository);
		
		if (repository == null) {
			throw new NotFoundException("Repository with code " + repositoryCode + " does not exists");
		}

		crossStorageService.remove(repository, customEntityTemplate, uuid);

		return Response.noContent().build();
	}
	
	@GET
	@Path("/{cetCode}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Search through entity")
	public Response get(@HeaderParam("Base64-Encode") @ApiParam("Base 64 encode") boolean base64Encode,
			@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode,
			@QueryParam("singleValue") Boolean singleValue,
			@BeanParam PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException, IOException {
		
		return list(base64Encode, cetCode, false, singleValue, paginationConfiguration);
	}

	@GET
	@Path("/{cetCode}/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Search entity by uuid")
	public Map<String, Object> getByUuid(@HeaderParam("Base64-Encode") @ApiParam("Base 64 encode") boolean base64Encode,
			@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @PathParam("uuid") @ApiParam("uuid") String uuid,
			@HeaderParam("See-Decrypted") boolean seeDecrypted, 
			@BeanParam PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException, IOException {

		final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);

		if (customEntityTemplate == null) {
			throw new NotFoundException("Template " + cetCode + " does not exists");
		}

		if (!currentUser.hasRole(customEntityTemplate.getReadPermission())) {
			throw new ForbiddenException();
		}

		final Repository repository = repositoryService.findByCode(repositoryCode);
		
		hasAccessToRepository(repository);
		
		Set<String> fields = new HashSet<>(paginationConfiguration.getFetchFields());
		Map<String, Set<String>> subFields = PersistenceUtils.extractSubFields(fields);

		Map<String, Object> values = crossStorageService.findById(repository, 
				customEntityTemplate, 
				uuid,
				fields,
				subFields,
				true);

		if (values.size() == 1 && values.containsKey("uuid")) {
			throw new NotFoundException(cetCode + " with uuid " + uuid + " does not exists");
		}

		convertFiles(customEntityTemplate, values, base64Encode);
		values = serializeJpaEntities(values);

		if (seeDecrypted && currentUser.hasRole(customEntityTemplate.getDecrpytPermission())) {
			var cei = CEIUtils.fromMap(values, customEntityTemplate);
			var hash = CEIUtils.getHash(cei, cache.getCustomFieldTemplates(customEntityTemplate.getAppliesTo()));
			for (var entry : values.entrySet()) {
				if (entry.getValue() instanceof String) {
					String strVal = (String) entry.getValue();
					if (strVal.startsWith("🔒")) {
						entry.setValue(PasswordUtils.decryptNoSecret(hash, strVal));
					}
				}
			}
		}

		return values;
	}

	@PUT
	@Path("/{cetCode}/{uuid}")
	@ApiOperation(value = "Update persistence")
	public void update(@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @PathParam("uuid") @ApiParam("uuid") String uuid,
			@ApiParam("Map of body") Map<String, Object> body) throws BusinessException, BusinessApiException, IOException, EntityDoesNotExistsException {
		final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
		if (customEntityTemplate == null) {
			throw new NotFoundException();
		}

		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCetCode(cetCode);
		cei.setCet(cache.getCustomEntityTemplate(cetCode));
		cei.setUuid(uuid);
		customFieldInstanceService.setCfValues(cei, cetCode, body);
		cei.setCfValuesOld((CustomFieldValues) SerializationUtils.clone(cei.getCfValues()));

		final Repository repository = repositoryService.findByCode(repositoryCode);
		
		hasAccessToRepository(repository);
		
		crossStorageService.update(repository, cei);
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "List persistence")
	public List<PersistedItem> persist(MultipartFormDataInput input) throws IOException, CyclicDependencyException, EntityDoesNotExistsException {

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		InputPart dtosPart = uploadForm.remove("data").get(0);
		GenericType<Collection<PersistenceDto>> dtosType = new GenericType<Collection<PersistenceDto>>() {
		};

		Collection<PersistenceDto> dtos = dtosPart.getBody(dtosType);

		Map<EntityProperty, List<DataPart>> dataParts = new HashMap<>();

		// Build data part list
		for (Map.Entry<String, List<InputPart>> formPart : uploadForm.entrySet()) {
			for (InputPart inputPart : formPart.getValue()) {
				String[] splittedKey = formPart.getKey().split("\\.", 2);
				String entityName = splittedKey[0];

				String restingPart = splittedKey[1];
				String propertyName = restingPart.split("\\[")[0];

				EntityProperty entityProperty = new EntityProperty(entityName, propertyName);

				String fileName;
				String parameters = StringUtils.substringBetween(restingPart, "[", "]");

				boolean base64Encoded = false;

				if (StringUtils.isNotBlank(parameters)) {
					fileName = parameters.replaceFirst(".*filename=([^;]*).*", "$1");
					if (StringUtils.isBlank(fileName)) {
						throw new IllegalArgumentException("You must provide a file name for the part " + formPart.getKey() + ". " + "For exemple, the data-part name should be "
								+ formPart.getKey() + "[filename=myFile.txt]");
					}

					base64Encoded = Pattern.compile(".*encoding=base64.*", Pattern.CASE_INSENSITIVE).matcher(parameters).matches();
				} else {
					fileName = RestUtils.getFileName(inputPart);
				}

				InputStream inputStream = inputPart.getBody(InputStream.class, null);

				if (base64Encoded) {
					inputStream = new Base64InputStream(inputStream);
				}

				dataParts.computeIfAbsent(entityProperty, k -> new ArrayList<>()).add(new DataPart(inputStream, fileName));
			}
		}

		// Add the files to the DTOs
		for (Map.Entry<EntityProperty, List<DataPart>> dataPartEntry : dataParts.entrySet()) {

			if (dataPartEntry.getValue().size() == 1) {

				File file = new File(tempDir.toString(), dataPartEntry.getValue().get(0).getFileName());
				FileUtils.copyInputStreamToFile(dataPartEntry.getValue().get(0).getInputStream(), file);

				dtos.stream().filter(dto -> dto.getName().equals(dataPartEntry.getKey().getEntityName())).findFirst()
						.ifPresent(dto -> dto.getProperties().put(dataPartEntry.getKey().getEntityProperty(), file));

			} else {

				for (DataPart dataPart : dataPartEntry.getValue()) {
					InputStream inputStream = dataPart.getInputStream();

					File file = new File(tempDir.toString(), dataPart.getFileName());
					FileUtils.copyInputStreamToFile(inputStream, file);

					dtos.stream().filter(dto -> dto.getName().equals(dataPartEntry.getKey().getEntityName())).findFirst().ifPresent(dto -> {
						List<File> property = (List<File>) dto.getProperties().computeIfAbsent(dataPartEntry.getKey().getEntityProperty(), s -> new ArrayList<File>());
						property.add(file);
					});

				}

			}
		}

		return persist(PersistenceMode.graph, dtos);
	}
	
	@POST
	@Path("/gzip")
	@Consumes(MediaType.APPLICATION_JSON)
	public void persistGzip(@GZIP List<PersistenceDto> dtos) throws EntityDoesNotExistsException, CyclicDependencyException, IOException, BusinessApiException, BusinessException, ELException {
		var repository = repositoryService.findByCode(repositoryCode);
		if(repository == null) {
			throw new NotFoundException("Repository " + repositoryCode + " does not exist");
		}
		hasAccessToRepository(repository);
		
		// Deserialize binaries
		for (PersistenceDto dto : dtos) {
			decodeBase64Files(dto);
		}
		
		AtomicPersistencePlan atomicPersistencePlan = getSchedule(dtos);
		scheduledPersistenceService.persist(repositoryCode, atomicPersistencePlan);
	}

	private AtomicPersistencePlan getSchedule(Collection<PersistenceDto> dtos) throws CyclicDependencyException {
		/* Extract the entities */
		final List<Entity> entities = dtos.stream().filter(persistenceDto -> persistenceDto.getDiscriminator().equals(EntityOrRelation.ENTITY))
				.map(persistenceDto -> new Entity.Builder().type(persistenceDto.getType()).name(persistenceDto.getName()).properties(persistenceDto.getProperties()).build())
				.collect(Collectors.toList());

		/* Extract the relationships */
		final List<EntityRelation> relations = dtos.stream().filter(persistenceDto -> persistenceDto.getDiscriminator().equals(EntityOrRelation.RELATION)).map(persistenceDto -> {
			final Optional<Entity> source = entities.stream().filter(entity -> entity.getName().equals(persistenceDto.getSource())).findAny();
			final Optional<Entity> target = entities.stream().filter(entity -> entity.getName().equals(persistenceDto.getTarget())).findAny();
			if (source.isPresent() && target.isPresent()) {
				return new EntityRelation.Builder().type(persistenceDto.getType()).source(source.get()).target(target.get()).properties(persistenceDto.getProperties()).build();
			}
			LOGGER.warn("Relationship of type {} between {} and {} will not be persisted because of missing source or target", persistenceDto.getType(), persistenceDto.getSource(),
					persistenceDto.getTarget());
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toList());

		/* Create the persistence schedule */
		List<EntityOrRelation> entityOrRelations = new ArrayList<>(entities);
		entityOrRelations.addAll(relations);
		AtomicPersistencePlan atomicPersistencePlan = schedulingService.schedule(entityOrRelations);
		return atomicPersistencePlan;
	}
	
	@POST
	@Path("/{cetCode}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PersistedItem> peristMany(@PathParam("cetCode") String cetCode, String body) throws EntityDoesNotExistsException, CyclicDependencyException {
		Collection<Map<String, Object>> dtos = JacksonUtil.fromString(body, new TypeReference<Collection<Map<String, Object>>>() {});
		dtos.forEach(dto -> dto.put("cetCode", cetCode));
		return persist(PersistenceMode.list, dtos);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persist many entities")
	public List<PersistedItem> persist(
			@HeaderParam("Persistence-Mode") @DefaultValue("graph") PersistenceMode persistenceMode, 
			Object body) throws CyclicDependencyException, EntityDoesNotExistsException {
		
		final Repository repository = repositoryService.findByCode(repositoryCode);
		hasAccessToRepository(repository);
		
		if(persistenceMode.equals(PersistenceMode.graph)) {
			Collection<PersistenceDto> dtos = null;
			if(body instanceof Collection) {
				if(!((Collection) body).isEmpty()) {
					Object firstItem = ((Collection) body).iterator().next();
					if (firstItem instanceof PersistenceDto) {
						dtos = (Collection<PersistenceDto>) body;
					}
				}
			}
			
			if (dtos == null) {
				dtos = JacksonUtil.convert(body, new TypeReference<Collection<PersistenceDto>>() {});
			}
			AtomicPersistencePlan atomicPersistencePlan = getSchedule(dtos);
	
			try {
				/* Persist the entities and return 201 created response */
				return scheduledPersistenceService.persist(repositoryCode, atomicPersistencePlan);
	
			} catch (BusinessException | ELException | IOException e) {
				/* An error happened */
				throw new ServerErrorException(Response.serverError().entity(e).build());
			}
			
		} else {
			List<PersistedItem> persistedItems = new ArrayList<>();
			Collection<Map<String, Object>> dtos = JacksonUtil.convert(body, new TypeReference<Collection<Map<String, Object>>>() {});
			
			for(Map<String, Object> dto : dtos) {
				try {
					CustomEntityInstance cei = CEIUtils.pojoToCei(dto);
					PersistenceActionResult result = crossStorageService.createOrUpdate(repository, cei);
					
					PersistedItem item = new PersistedItem(result.getBaseEntityUuid(), dto);
					persistedItems.add(item);
					
				} catch (BusinessException | IOException e) {
					/* An error happened */
					Response response = Response.serverError().entity(e).build();
					throw new ServerErrorException(response);
				}
			}

			return persistedItems;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void decodeBase64Files(PersistenceDto dto) throws IOException {
		for (Map.Entry<String, Object> propEntry : new HashSet<>(dto.getProperties().entrySet())) {
			if (propEntry.getValue() instanceof Map) {
				Map<String, Object> propertyAsMap = (Map<String, Object>) propEntry.getValue();
				if (isBase64EncodedFile(propertyAsMap)) {
					File tmpFile = getFile(propertyAsMap);
					dto.getProperties().put(propEntry.getKey(), tmpFile);
				}

			} else if (propEntry.getValue() instanceof List) {
				if (!((List) propEntry.getValue()).isEmpty()) {
					List<File> files = new ArrayList<>();
					for (Object o : (List) propEntry.getValue()) {
						if (o instanceof Map) {
							Map<String, Object> propertyAsMap = (Map<String, Object>) o;
							if (isBase64EncodedFile(propertyAsMap)) {
								File tmpFile = getFile(propertyAsMap);
								files.add(tmpFile);
							}
						}
					}

					if (!files.isEmpty()) {
						dto.getProperties().put(propEntry.getKey(), files);
					}
				}
			}
		}
	}

	private boolean isBase64EncodedFile(Map<String, Object> propertyAsMap) {
		return propertyAsMap.containsKey("filename") && propertyAsMap.containsKey("contentBase64");
	}

	private File getFile(Map<String, Object> propertyAsMap) throws IOException {
		File tmpFile = new File(tempDir.toString(), (String) propertyAsMap.get("filename"));
		Base64.Decoder dec = Base64.getDecoder();
		byte[] contentBase64s = dec.decode((String) propertyAsMap.get("contentBase64"));
		FileUtils.writeByteArrayToFile(tmpFile, contentBase64s);
		return tmpFile;
	}

	/**
	 * Replace the hard drive file paths by URL that permit to download them
	 * 
	 * @param customEntityTemplate Template of the values
	 * @param values               Actual values containing the file paths
	 * @param base64               Whether to encode files as base 64 string
	 */
	@SuppressWarnings("rawtypes")
	private void convertFiles(CustomEntityTemplate customEntityTemplate, Map<String, Object> values, boolean base64) throws IOException {
		Map<String, CustomFieldTemplate> customFieldTemplates = cache.getCustomFieldTemplates(customEntityTemplate.getAppliesTo());

		if (base64) {
			for (Map.Entry<String, Object> entry : new HashMap<>(values).entrySet()) {
				String key = entry.getKey();
				if (customFieldTemplates.get(key) == null) {
					continue;
				}

				Object value = entry.getValue();
				if (value instanceof File) {
					byte[] fileContent = FileUtils.readFileToByteArray((File) value);
					String encodedString = Base64.getEncoder().encodeToString(fileContent);
					values.put(key, encodedString);

				} else if (value instanceof String && customFieldTemplates.get(key).getFieldType() == CustomFieldTypeEnum.BINARY) {
					byte[] fileContent = FileUtils.readFileToByteArray(new File((String) value));
					String encodedString = Base64.getEncoder().encodeToString(fileContent);
					values.put(key, encodedString);

				} else if (value instanceof List && !((List) value).isEmpty()) {
					List<String> encodedStrings = new ArrayList<>();
					for (Object obj : (List) value) {
						if (obj instanceof File) {
							byte[] fileContent = FileUtils.readFileToByteArray((File) obj);
							String encodedString = Base64.getEncoder().encodeToString(fileContent);
							encodedStrings.add(encodedString);

						} else if (obj instanceof String && customFieldTemplates.get(key).getFieldType() == CustomFieldTypeEnum.BINARY) {
							byte[] fileContent = FileUtils.readFileToByteArray(new File((String) obj));
							String encodedString = Base64.getEncoder().encodeToString(fileContent);
							values.put(key, encodedString);
						}
					}

					if (!encodedStrings.isEmpty()) {
						values.put(key, encodedStrings);
					}
				}
			}

		} else {
			customFieldTemplates.values().stream().filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY)) // Only get binary fields
					.filter(f -> values.get(f.getCode()) != null) // Filter on present values
					.forEach(binaryField -> {
						Object binaryFieldValue = values.get(binaryField.getCode());
						if (binaryFieldValue instanceof String) {
							String url = buildFileUrl(customEntityTemplate, values, binaryField);
							values.put(binaryField.getCode(), url);

						} else if (binaryFieldValue instanceof Collection) {
							List<String> urls = new ArrayList<>();
							for (int index = 0; index < ((Collection<?>) binaryFieldValue).size(); index++) {
								String url = buildFileUrl(customEntityTemplate, values, binaryField);
								url += "?index=" + index;
								urls.add(url);
							}
							values.put(binaryField.getCode(), urls);
						}
					});
		}
	}

	/**
	 * Build an URL allowing to download a given file for a given entity in the
	 * FileSysytem
	 *
	 * @param customEntityTemplate Template of the entity
	 * @param values               Actual values of the entity
	 * @param binaryField          Field holding the file reference
	 */
	private String buildFileUrl(CustomEntityTemplate customEntityTemplate, Map<String, Object> values, CustomFieldTemplate binaryField) {
		String uuid = values.get("uuid") != null ? (String) values.get("uuid") : (String) values.get("meveo_uuid");

		return new StringBuilder("/api/rest/fileSystem/binaries/").append(repositoryCode).append("/").append(customEntityTemplate.getCode()).append("/").append(uuid).append("/")
				.append(binaryField.getCode()).toString();
	}

	/**
	 * This service build an iterable list of CET/CRT by using cartesian product of
	 * example values of its CFTs.
	 */
	@POST
	@Path("/{cetCode}/examples")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation("List data for a given CET")
	public List<Map<String, String>> listExamples(@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode,
			@ApiParam("Pagination configuration") PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException {
		List<Map<String, String>> listExamples = customEntityTemplateService.listExamples(cetCode, paginationConfiguration);
		Collections.shuffle(listExamples);
		return listExamples;
	}

	private Map<String, Object> serializeJpaEntities(Map<String, Object> values) {
		var convertedValues = new HashMap<>(values);
		values.forEach((k, v) -> {
			if (v instanceof BaseEntity) {
				convertedValues.put(k, ((BaseEntity) v).getId());
			}
		});
		return convertedValues;
	}

	@PostConstruct
	private void init() {
		try {
			tempDir = Files.createTempDirectory("dataUpload");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PreDestroy
	private void onDestroy() {
		try {

			Files.list(tempDir).forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException e) {
					LOGGER.warn("{} cannot be deleted", path.toString(), e);
				}
			});

			if (Files.list(tempDir).count() == 0) {
				Files.delete(tempDir);
			}
		} catch (Exception e) {
			LOGGER.error("Error destroying PeristenceRs instance", e);
		}
	}

	private boolean hasAccessToRepository(Repository repository) throws EntityDoesNotExistsException {

		if(repository == null) {
			throw new EntityDoesNotExistsException(Repository.class, repositoryCode);
		}
		
		User user = userService.findByUsername(currentUser.getUserName());
		if (user.getUserLevel() != null && repository.getUserHierarchyLevel() != null
				&& !userHierarchyLevelService.isInHierarchy(repository.getUserHierarchyLevel(), user.getUserLevel())) {
			throw new ClientErrorException("User level does not have access to the repository.", Response.Status.FORBIDDEN);
		}
		

		return true;
	}

}
