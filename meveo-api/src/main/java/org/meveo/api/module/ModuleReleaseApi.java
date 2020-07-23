/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.api.module;

import static org.meveo.commons.utils.FileUtils.addDirectoryToZip;
import static org.meveo.commons.utils.FileUtils.addToZipFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.meveo.admin.util.ModuleUtil;
import org.meveo.api.ApiService;
import org.meveo.api.ApiUtils;
import org.meveo.api.ApiVersionedService;
import org.meveo.api.CustomFieldTemplateApi;
import org.meveo.api.EntityCustomActionApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.ModuleReleaseDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.export.ExportFormat;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.ModuleItem;
import org.meveo.model.VersionedEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.ModuleRelease;
import org.meveo.model.module.ModuleReleaseItem;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.util.EntityCustomizationUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * @author Clément Bareth
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Wassim Drira
 * @since 6.3.0
 * @version 6.9.0
 */
@Stateless
public class ModuleReleaseApi {

	private static boolean initalized = false;

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomFieldTemplateApi customFieldTemplateApi;

	@Inject
	private EntityCustomActionApi entityCustomActionApi;

	@Inject
	private ParamBeanFactory paramBeanFactory;
	
	@Inject
	private MeveoModulePatchApi modulePatchApi;

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;

	public ModuleReleaseApi() {
		if (!initalized) {
			registerModulePackage("org.meveo.model");
			initalized = true;
		}
	}

	public void registerModulePackage(String packageName) {
		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> moduleItemClasses = reflections.getTypesAnnotatedWith(ModuleItem.class);

		for (Class<?> aClass : moduleItemClasses) {
			MeveoModuleItemInstaller.MODULE_ITEM_TYPES.put(aClass.getSimpleName(), aClass);
			log.debug("Registering module item type {} from class {}", aClass.getSimpleName(), aClass);
		}
	}

	/**
	 * Convert MeveoModule or its subclass object to DTO representation.
	 *
	 * @param module Module object
	 * @return MeveoModuleDto object
	 * @throws MeveoApiException meveo api exception.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ModuleReleaseDto moduleToDto(ModuleRelease module) throws MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {

        MeveoModule meveoModule = module.getMeveoModule();

		if (meveoModule.isDownloaded() && !meveoModule.isInstalled()) {
			try {
				ModuleReleaseDto moduleReleaseDto = MeveoModuleUtils.moduleSourceToDto(module);

				if (CollectionUtils.isNotEmpty(module.getMeveoModule().getPatches())) {
					moduleReleaseDto.setPatches(module.getMeveoModule().getPatches().stream().map(e -> modulePatchApi.toDto(e)).collect(Collectors.toList()));
				}

				return moduleReleaseDto;

			} catch (Exception e) {
				log.error("Failed to load module source {} {}", module.getCode(), e);
				throw new MeveoApiException("Failed to load module source");
			}
		}

		Class<? extends ModuleReleaseDto> dtoClass = ModuleReleaseDto.class;

        ModuleReleaseDto moduleReleaseDto;
		try {
			moduleReleaseDto = dtoClass.getConstructor(ModuleRelease.class).newInstance(module);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			log.error("Failed to instantiate Module Dto. No reason for it to happen. ", e);
			throw new RuntimeException("Failed to instantiate Module Dto. No reason for it to happen. ", e);
		}

		if (module.getCode() != null) {
			moduleReleaseDto.setCode(module.getCode());
		}
		
		moduleReleaseDto.setDescription(module.getDescription());

		if (module.getCurrentVersion() != null) {
			moduleReleaseDto.setCurrentVersion(module.getCurrentVersion());
		}

		if (!StringUtils.isBlank(module.getLogoPicture())) {
			try {
				moduleReleaseDto.setLogoPictureFile(ModuleUtil.readModulePicture(currentUser.getProviderCode(), module.getLogoPicture()));
			} catch (Exception e) {
				log.error("Failed to read module files {}, info {}", module.getLogoPicture(), e.getMessage(), e);
			}
		}

		Set<String> moduleFiles = module.getModuleFiles();
		if (moduleFiles != null) {
			for (String moduleFile : moduleFiles) {
				moduleReleaseDto.addModuleFile(moduleFile);
			}
		}

		List<MeveoModuleDependency> dependencies = module.getModuleDependencies();
		if (dependencies != null) {
			for (MeveoModuleDependency moduleDependency : dependencies) {
				moduleReleaseDto.addModuleDependency(moduleDependency);
			}
		}

		List<ModuleReleaseItem> moduleItems = module.getModuleItems();
		if (moduleItems != null) {
			for (ModuleReleaseItem item : moduleItems) {

				try {
					BaseEntityDto itemDto = null;

					if (item.getItemClass().equals(CustomFieldTemplate.class.getName())) {
						// we will only add a cft if it's not a field of a cet
						if (!StringUtils.isBlank(item.getAppliesTo())) {
							String cetCode = EntityCustomizationUtils.getEntityCode(item.getAppliesTo());
							if (customEntityTemplateService.findByCode(cetCode) == null) {
								itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
							}

						} else {
							itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
						}

					} else if (item.getItemClass().equals(EntityCustomAction.class.getName())) {
						itemDto = entityCustomActionApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());

					} else {
						Class clazz = Class.forName(item.getItemClass());
						if (clazz.isAnnotationPresent(VersionedEntity.class)) {
							ApiVersionedService apiService = ApiUtils.getApiVersionedService(item.getItemClass(), true);
							itemDto = apiService.findIgnoreNotFound(item.getItemCode(), item.getValidity() != null ? item.getValidity().getFrom() : null, item.getValidity() != null ? item.getValidity().getTo() : null);

						} else {
							ApiService apiService = ApiUtils.getApiService(clazz, true);
							itemDto = apiService.findIgnoreNotFound(item.getItemCode());
						}
					}
					if (itemDto != null) {
						moduleReleaseDto.addModuleItem(itemDto);

					} else {
						log.warn("Failed to find a module item or not added in case of CFT that is a field of CET {}", item);
					}

				} catch (ClassNotFoundException e) {
					log.error("Failed to find a class", e);
					throw new MeveoApiException("Failed to access field value in DTO: " + e.getMessage());

				} catch (MeveoApiException e) {
					log.error("Failed to transform module item to DTO. Module item {}", item, e);
					throw e;
				}
			}
//		}

//		// Finish converting subclasses of MeveoModule class
//		if (module instanceof BusinessServiceModel) {
//			businessServiceModelToDto((BusinessServiceModel) module, (BusinessServiceModelDto) moduleDto);
//
		}
		
		if (module.getMeveoModule().getPatches() != null && !module.getMeveoModule().getPatches().isEmpty()) {
			moduleReleaseDto.setPatches(module.getMeveoModule().getPatches().stream().map(e -> modulePatchApi.toDto(e)).collect(Collectors.toList()));
		}

		return moduleReleaseDto;
	}

	public ModuleReleaseDto toDto(ModuleRelease entity) {
		try {
			return moduleToDto(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ModuleRelease fromDto(ModuleReleaseDto dto) throws EntityDoesNotExistsException {
		return null;
	}

	public boolean exists(ModuleReleaseDto dto) {
		return false;
	}

	public void copyFileFromFolder(String pathFile, File file) throws FileNotFoundException {
		File[] files = file.listFiles();
		if (files != null) {
			for (File fileFromFolder : files) {
				String name = fileFromFolder.getName();
				String nameFileFromZip = name.split(".zip")[0];
				String path = pathFile + "/" + nameFileFromZip;
				if (!fileFromFolder.isDirectory()) {
					FileInputStream inputStream = new FileInputStream(fileFromFolder);
					copyFile(path, inputStream);
				} else {
					File folder = new File(path);
					if (!folder.exists()) {
						folder.mkdir();
					}
					copyFileFromFolder(path, fileFromFolder);
				}
			}
		}
	}

	public void importZip(String fileName, InputStream inputStream, boolean overwrite) {
		importZip(fileName, inputStream, overwrite);
	}

	private void copyFile(String fileName, InputStream in) {
		try {

			// write the inputStream to a FileOutputStream
			OutputStream out = new FileOutputStream(new File(fileName));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			in.close();
			out.flush();
			out.close();

			log.debug("New file created!");
		} catch (Exception e) {
			log.error("Failed saving file. ", e);
		}
	}

	/**
	 * Compress module and its files into byte array.
	 *
	 * @param exportFile   file to export
	 * @param meveoModules list of meveo modules
	 * @return zip file as byte array
	 * @throws Exception exception.
	 */
	public byte[] createZipFile(String exportFile, List<ModuleRelease> meveoModules) throws Exception {

		Logger log = LoggerFactory.getLogger(FileUtils.class);
		log.info("Creating zip file for {}", exportFile);

		ZipOutputStream zos = null;
		ByteArrayOutputStream baos = null;
		CheckedOutputStream cos = null;

		try {
			baos = new ByteArrayOutputStream();
			cos = new CheckedOutputStream(baos, new CRC32());
			zos = new ZipOutputStream(new BufferedOutputStream(cos));

			// Add modules defintion file
			File sourceFile = new File(exportFile);
			addToZipFile(sourceFile, zos, null);

			// Add files contained in modules
			for (ModuleRelease meveoModule : meveoModules) {
				for (String pathFile : meveoModule.getModuleFiles()) {
					String path = pathFile.startsWith(File.separator) ? pathFile.substring(1) : pathFile;
					int lastIndexOf = path.lastIndexOf(File.separator);
					String baseDir = lastIndexOf > -1 ? path.substring(0, lastIndexOf) : null;
					String chrootDir = paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
					File file = new File(chrootDir, pathFile);
					if (!file.exists()) {
						log.error("File does not exists {}", file);
						continue;
					}

					if (file.isDirectory()) {
						addDirectoryToZip(file, zos, baseDir);
					} else {
						addToZipFile(file, zos, baseDir);
					}
				}
			}

			zos.flush();
			zos.close();
			return baos.toByteArray();

		} finally {
			IOUtils.closeQuietly(zos);
			IOUtils.closeQuietly(cos);
			IOUtils.closeQuietly(baos);
		}
	}

	public File exportEntities(ExportFormat format, List<ModuleRelease> entities) throws IOException {
		List<ModuleReleaseDto> dtos;

		if (CollectionUtils.isEmpty(entities)) {
			dtos = new ArrayList<>();
		} else {
			dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
		}

		return exportDtos(format, dtos);
	}

	private File exportDtos(ExportFormat format, List<ModuleReleaseDto> dtos) throws IOException {
		if (format == null) {
			throw new IllegalArgumentException("Format must be provided");
		}
		if (CollectionUtils.isEmpty(dtos)) {
			throw new IllegalArgumentException("Module release must be provided");
		}
		ModuleReleaseDto moduleReleaseDto = dtos.get(0);
		File exportFile = new File(moduleReleaseDto.getCode()+ "_version-" + moduleReleaseDto.getCurrentVersion().replace(".", "_") + "." + format.getFormat());

		switch (format) {

			case JSON:
				new ObjectMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT)
						.writeValue(exportFile, dtos);
				break;

			case XML:
				new XmlMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT)
						.writeValue(exportFile, dtos);
				break;

			case CSV:
				CsvMapper csvMapper = (CsvMapper) new CsvMapper()
						.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT);

				CsvSchema schema = csvMapper.schemaFor(ModuleReleaseDto.class).withColumnSeparator(';');
				ObjectWriter myObjectWriter = csvMapper.writer(schema);
				myObjectWriter.writeValue(exportFile, dtos);
				break;
		}

		return exportFile;
	}
	
	public MeveoModuleDto convertToMeveoModule(ModuleReleaseDto releaseDto) {

		MeveoModuleDto moduleDto = new MeveoModuleDto();
		moduleDto.setCode(releaseDto.getCode());
		moduleDto.setDescription(releaseDto.getDescription());
		moduleDto.setLicense(releaseDto.getLicense());
		moduleDto.setLogoPicture(releaseDto.getLogoPicture());
		moduleDto.setScript(releaseDto.getScript());
		moduleDto.setCurrentVersion(releaseDto.getCurrentVersion());
		moduleDto.setMeveoVersionBase(releaseDto.getMeveoVersionBase());
		moduleDto.setMeveoVersionCeiling(releaseDto.getMeveoVersionCeiling());

		if (CollectionUtils.isNotEmpty(releaseDto.getModuleFiles())) {
			List<String> moduleFiles = new ArrayList<>();
			for (String moduleFile : releaseDto.getModuleFiles()) {
				moduleFiles.add(moduleFile);
			}
			moduleDto.setModuleFiles(moduleFiles);
		}

		if (CollectionUtils.isNotEmpty(releaseDto.getModuleItems())) {
			moduleDto.setModuleItems(releaseDto.getModuleItems());

		}
		
		return moduleDto;
	}
}