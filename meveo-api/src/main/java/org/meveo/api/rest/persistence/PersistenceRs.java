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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64Encoder;
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
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.persistence.scheduler.AtomicPersistencePlan;
import org.meveo.persistence.scheduler.CyclicDependencyException;
import org.meveo.persistence.scheduler.PersistedItem;
import org.meveo.persistence.scheduler.ScheduledPersistenceService;
import org.meveo.persistence.scheduler.SchedulingService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.storage.FileSystemService;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.ApiOperation;

/**
 * @author Clement Bareth
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
@Path("/{repository}/persistence")
public class PersistenceRs {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PersistenceRs.class);

    @Inject
    private SchedulingService schedulingService;

    @Inject
    private ScheduledPersistenceService<CrossStorageService> scheduledPersistenceService;

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

    @PathParam("repository")
    private String repositoryCode;

    @Inject
    private FileSystemService fileSystemService;

    private java.nio.file.Path tempDir;

    @POST
    @Path("/{cetCode}/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("List data for a given CET")
    public List<Map<String, Object>> list(@HeaderParam("Base64-Encode") @ApiParam("Base 64 encode") boolean base64Encode, @PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @ApiParam("Pagination configuration information") PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException, IOException {
        final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
        if (customEntityTemplate == null) {
            throw new NotFoundException("Custom entity template with code " + cetCode + " does not exists");
        }

        if (paginationConfiguration == null) {
            paginationConfiguration = new PaginationConfiguration();
        }

        Repository repository = repositoryService.findByCode(repositoryCode);
        List<Map<String, Object>> data = crossStorageService.find(repository, customEntityTemplate, paginationConfiguration);

        for (Map<String, Object> values : data) {
            convertFiles(customEntityTemplate, values, base64Encode);
        }

        return data;
    }

    @DELETE
    @Path("/{cetCode}/{uuid}")
    public Response delete(@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @PathParam("uuid") @ApiParam("uuid") String uuid) throws BusinessException {
        final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
        if (customEntityTemplate == null) {
            throw new NotFoundException("Custom entity template with code " + cetCode + " does not exists");
        }

        final Repository repository = repositoryService.findByCode(repositoryCode);
        if (repository == null) {
            throw new NotFoundException("Repository with code " + repositoryCode + " does not exists");
        }

        crossStorageService.remove(repository, customEntityTemplate, uuid);

        return Response.noContent().build();
    }

    @GET
    @Path("/{cetCode}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> get(@HeaderParam("Base64-Encode") @ApiParam("Base 64 encode") boolean base64Encode, @PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @PathParam("uuid") @ApiParam("uuid") String uuid) throws EntityDoesNotExistsException, IOException {
        final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
        if (customEntityTemplate == null) {
            throw new NotFoundException();
        }

        final Repository repository = repositoryService.findByCode(repositoryCode);
        Map<String, Object> values = crossStorageService.find(repository, customEntityTemplate, uuid);

        convertFiles(customEntityTemplate, values, base64Encode);

        return values;
    }

    @PUT
    @Path("/{cetCode}/{uuid}")
    public void update(@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @PathParam("uuid") @ApiParam("uuid") String uuid, @ApiParam("Map of body") Map<String, Object> body) throws BusinessException, BusinessApiException, IOException, EntityDoesNotExistsException {
        final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
        if (customEntityTemplate == null) {
            throw new NotFoundException();
        }

        CustomEntityInstance cei = new CustomEntityInstance();
        cei.setCetCode(cetCode);
        cei.setCet(cache.getCustomEntityTemplate(cetCode));
        cei.setUuid(uuid);
        customFieldInstanceService.setCfValues(cei, cetCode, body);

        final Repository repository = repositoryService.findByCode(repositoryCode);
        crossStorageService.update(repository, cei);
    }

    @SuppressWarnings("unchecked")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public List<PersistedItem> persist(MultipartFormDataInput input) throws IOException, CyclicDependencyException {

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
                        throw new IllegalArgumentException("You must provide a file name for the part " + formPart.getKey() + ". " +
                                "For exemple, the data-part name should be " + formPart.getKey() + "[filename=myFile.txt]");
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

                dtos.stream()
                        .filter(dto -> dto.getName().equals(dataPartEntry.getKey().getEntityName()))
                        .findFirst()
                        .ifPresent(dto -> dto.getProperties().put(dataPartEntry.getKey().getEntityProperty(), file));

            } else {

                for (DataPart dataPart : dataPartEntry.getValue()) {
                    InputStream inputStream = dataPart.getInputStream();

                    File file = new File(tempDir.toString(), dataPart.getFileName());
                    FileUtils.copyInputStreamToFile(inputStream, file);

                    dtos.stream()
                            .filter(dto -> dto.getName().equals(dataPartEntry.getKey().getEntityName()))
                            .findFirst()
                            .ifPresent(dto -> {
                                List<File> property = (List<File>) dto.getProperties().computeIfAbsent(dataPartEntry.getKey().getEntityProperty(), s -> new ArrayList<File>());
                                property.add(file);
                            });

                }

            }
        }

        return persist(dtos);
    }

    @SuppressWarnings("unchecked")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public List<PersistedItem> persist(Collection<PersistenceDto> dtos) throws CyclicDependencyException, IOException {

        // Deserialize binaries
        for (PersistenceDto dto : dtos) {
            decodeBase64Files(dto);
        }

        /* Extract the entities */
        final List<Entity> entities = dtos.stream()
                .filter(persistenceDto -> persistenceDto.getDiscriminator().equals(EntityOrRelation.ENTITY))
                .map(persistenceDto -> new Entity.Builder()
                        .type(persistenceDto.getType())
                        .name(persistenceDto.getName())
                        .properties(persistenceDto.getProperties())
                        .build())
                .collect(Collectors.toList());

        /* Extract the relationships */
        final List<EntityRelation> relations = dtos.stream()
                .filter(persistenceDto -> persistenceDto.getDiscriminator().equals(EntityOrRelation.RELATION))
                .map(persistenceDto -> {
                    final Optional<Entity> source = entities.stream()
                            .filter(entity -> entity.getName().equals(persistenceDto.getSource()))
                            .findAny();
                    final Optional<Entity> target = entities.stream().filter(entity -> entity.getName().equals(persistenceDto.getTarget()))
                            .findAny();
                    if (source.isPresent() && target.isPresent()) {
                        return new EntityRelation.Builder()
                                .type(persistenceDto.getType())
                                .source(source.get())
                                .target(target.get())
                                .properties(persistenceDto.getProperties())
                                .build();
                    }
                    LOGGER.warn("Relationship of type {} between {} and {} will not be persisted because of missing source or target", persistenceDto.getType(), persistenceDto.getSource(), persistenceDto.getTarget());
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /* Create the persistence schedule */
        List<EntityOrRelation> entityOrRelations = new ArrayList<>(entities);
        entityOrRelations.addAll(relations);
        AtomicPersistencePlan atomicPersistencePlan = schedulingService.schedule(entityOrRelations);

        try {

            /* Persist the entities and return 201 created response */
            return scheduledPersistenceService.persist(repositoryCode, atomicPersistencePlan);

        } catch (BusinessException | ELException | IOException | BusinessApiException | EntityDoesNotExistsException e) {

            /* An error happened */
            throw new ServerErrorException(Response.serverError().entity(e).build());
        }

    }

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
     * @param customEntityTemplate Template of the values
     * @param values               Actual values containing the file paths
     * @param base64               Whether to encode files as base 64 string
     */
    private void convertFiles(CustomEntityTemplate customEntityTemplate, Map<String, Object> values, boolean base64) throws IOException {
        Map<String, CustomFieldTemplate> customFieldTemplates = cache.getCustomFieldTemplates(customEntityTemplate.getAppliesTo());

        if(base64) {
            for (Map.Entry<String, Object> entry : new HashMap<>(values).entrySet()) {
                String key = entry.getKey();
                if(customFieldTemplates.get(key) == null){
                    continue;
                }

                Object value = entry.getValue();
                if (value instanceof File) {
                    byte[] fileContent = FileUtils.readFileToByteArray((File) value);
                    String encodedString = Base64.getEncoder().encodeToString(fileContent);
                    values.put(key, encodedString);

                } else if(value instanceof String && customFieldTemplates.get(key).getFieldType() == CustomFieldTypeEnum.BINARY) {
                    byte[] fileContent = FileUtils.readFileToByteArray(new File((String) value));
                    String encodedString = Base64.getEncoder().encodeToString(fileContent);
                    values.put(key, encodedString);

                } else if (value instanceof List && !((List) value).isEmpty()) {
                    List<String> encodedStrings = new ArrayList<>();
                    for(Object obj : (List) value){
                        if(obj instanceof File){
                            byte[] fileContent = FileUtils.readFileToByteArray((File) obj);
                            String encodedString = Base64.getEncoder().encodeToString(fileContent);
                            encodedStrings.add(encodedString);

                        } else if(obj instanceof String && customFieldTemplates.get(key).getFieldType() == CustomFieldTypeEnum.BINARY) {
                            byte[] fileContent = FileUtils.readFileToByteArray(new File((String) obj));
                            String encodedString = Base64.getEncoder().encodeToString(fileContent);
                            values.put(key, encodedString);
                        }
                    }

                    if(!encodedStrings.isEmpty()){
                        values.put(key, encodedStrings);
                    }
                }
            }

        } else {
            customFieldTemplates.values()
                    .stream()
                    .filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY))    // Only get binary fields
                    .filter(f -> values.get(f.getCode()) != null)                        // Filter on present values
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
     * Build an URL allowing to download a given file for a given entity in the FileSysytem
     *
     * @param customEntityTemplate Template of the entity
     * @param values               Actual values of the entity
     * @param binaryField          Field holding the file reference
     */
    private String buildFileUrl(CustomEntityTemplate customEntityTemplate, Map<String, Object> values, CustomFieldTemplate binaryField) {
        String uuid = values.get("uuid") != null ? (String) values.get("uuid") : (String) values.get("meveo_uuid");

        return new StringBuilder("/api/rest/fileSystem/binaries/")
                .append(repositoryCode).append("/")
                .append(customEntityTemplate.getCode()).append("/")
                .append(uuid).append("/")
                .append(binaryField.getCode())
                .toString();
    }

    /**
     * This service build an iterable list of CET/CRT by using cartesian product of example values of its CFTs.
     */
    @POST
    @Path("/{cetCode}/examples")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("List data for a given CET")
    public List<Map<String, String>> listExamples(@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @ApiParam("Pagination configuration") PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException {
        List<Map<String, String>> listExamples = customEntityTemplateService.listExamples(cetCode, paginationConfiguration);
        Collections.shuffle(listExamples);
        return listExamples;
    }

    @PostConstruct
    private void init() {
    	try {
    		tempDir = Files.createTempDirectory("dataUpload");
    	} catch(Exception e) {
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
        } catch(Exception e) {
        	LOGGER.error("Error destroying PeristenceRs instance", e);
        }
    }

}
