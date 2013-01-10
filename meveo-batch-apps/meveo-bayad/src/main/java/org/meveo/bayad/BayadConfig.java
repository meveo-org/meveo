/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.bayad;

import java.util.HashMap;
import java.util.Map;

import org.meveo.commons.utils.ParamBean;

/**
 * Configuration bayad.
 * 
 * @author anasseh
 * @created 02.12.2010
 */
public class BayadConfig {

	private static final String BAYAD_PROPERTIES_FILENAME = "bayad.properties";

	private static final String INVOICES_SOURCE_FILES_DIR = "bayad.invoices.sourceFilesDir";
	private static final String INVOICES_REJECTED_FILES_DIR = "bayad.invoices.rejectedFilesDir";
	private static final String INVOICES_ACCEPTED_FILES_DIR = "bayad.invoices.acceptedFilesDir";
	private static final String INVOICES_IGNORED_FILES_DIR = "bayad.invoices.ignoredFilesDir";
	private static final String INVOICES_ERROR_FILES_DIR = "bayad.invoices.errorFilesDir";
	private static final String INVOICES_FILE_EXT_TO_PROCESS = "bayad.invoices.fileExt";
	private static final String INVOICES_PROCESSING_EXT = "bayad.invoices.processingExt";
	private static final String INVOICES_ERROR_FILE_PREFIX = "bayad.invoices.errorFilePrefix";
	private static final String INVOICES_ERROR_FILE_EXT = "bayad.invoices.errorFileExt";
	private static final String INVOICES_DATE_FORMAT = "bayad.invoices.dateFormat";

	private static final String TIP_SOURCE_FILES_DIR = "bayad.tip.sourceFilesDir";
	private static final String TIP_REJECTED_FILES_DIR = "bayad.tip.rejectedFilesDir";
	private static final String TIP_ACCEPTED_FILES_DIR = "bayad.tip.acceptedFilesDir";
	private static final String TIP_ERROR_FILES_DIR = "bayad.tip.errorFilesDir";
	private static final String TIP_FILE_EXT_TO_PROCESS = "bayad.tip.fileExt";
	private static final String TIP_PROCESSING_EXT = "bayad.tip.processingExt";
	private static final String TIP_ERROR_FILE_PREFIX = "bayad.tip.errorFilePrefix";
	private static final String TIP_ERROR_FILE_EXT = "bayad.tip.errorFileExt";
	private static final String TIP_DATE_FORMAT = "bayad.tip.dateFormat";
	private static final String TIP_SCANNING_INTERVAL = "bayad.tip.scanningInterval";
	private static final String TIP_CHECK_OCC_CODE = "bayad.tip.checkOccCode";
	private static final String TIP_OCC_CODE = "bayad.tip.occCode";

	private static final String INVOICES_OCC_CODE = "bayad.invoices.occCode";
	private static final String DDREQUEST_OCC_CODE = "bayad.ddrequest.occCode";
	private static final String DDREQUEST_HEADER_DDMODE = "bayad.ddrequest.header.DDmode";
	private static final String DDREQUEST_HEADER_REFRENCE = "bayad.ddrequest.header.reference";

	private static final String DDREQUEST_LINE_DDMODE = "bayad.ddrequest.line.DDmode";
	private static final String DDREQUEST_LINE_LABEL = "bayad.ddrequest.line.label";
	private static final String DDREQUEST_DATE_VALUE_AFTER = "bayad.ddrequest.dateValueAfterNbDays";

	private static final String DDREQUEST_FOOTER_DDMODE = "bayad.ddrequest.footer.DDmode";

	private static final String DDREQUEST_OUTPUT_DIR = "bayad.ddrequest.outputDir";
	private static final String DDREQUEST_FILE_NAME_EXTENSION = "bayad.ddrequest.fileName.extension";
	private static final String DDREQUEST_FILE_NAME_PREFIX = "bayad.ddrequest.fileName.prefix";
	private static final String DDREQUEST_ADD_LAST_EMPTY_LINE = "bayad.ddrequest.addLastEmptyLine";
	private static final String DDREQUEST_IS_TRUNCATE_STRING = "bayad.ddrequest.isTruncateString";
	
	private static final String BANK_FILE_SOURCE_FILES_DIR = "bayad.bankFile.sourceFilesDir";
	private static final String BANK_FILE_REJECTED_FILES_DIR = "bayad.bankFile.rejectedFilesDir";
	private static final String BANK_FILE_ACCEPTED_FILES_DIR = "bayad.bankFile.acceptedFilesDir";
	private static final String BANK_FILE_ERROR_FILES_DIR = "bayad.bankFile.errorFilesDir";
	private static final String BANK_FILE_FILE_EXT_TO_PROCESS = "bayad.bankFile.fileExt";
	private static final String BANK_FILE_PROCESSING_EXT = "bayad.bankFile.processingExt";
	private static final String BANK_FILE_ERROR_FILE_PREFIX = "bayad.bankFile.errorFilePrefix";
	private static final String BANK_FILE_ERROR_FILE_EXT = "bayad.bankFile.errorFileExt";
	private static final String BANK_FILE_DATE_FORMAT = "bayad.bankFile.dateFormat";
	private static final String BANK_FILE_SCANNING_INTERVAL = "bayad.bankFile.scanningInterval";
	private static final String BANK_FILE_CHECK_OCC_CODE = "bayad.bankFile.checkOccCode";
	private static final String BANK_FILE_OCC_CODE = "bayad.bankFile.occCode";

	private static final String DDREQUEST_SCANNING_INTERVAL = "bayad.ddrequest.scanningInterval";
	private static final String INVOICES_SCANNING_INTERVAL = "bayad.invoices.scanningInterval";
	private static final String DUNNING_SCANNING_INTERVAL = "bayad.dunning.scanningInterval";
	private static final String WORKING_THREADS = "bayad.workingThreadCount";

	private static final String DUNNING_LOT_FILENAME = "bayad.dunning.lotFileName";
	private static final String DUNNING_LOT_FILENAME_EXT = "bayad.dunning.lotFileName.extention";
	private static final String DUNNING_BALANCE_FLAG = "bayad.dunning.blanceFlag";
	private static final String DUNNING_OCC_CODE = "bayad.dunning.occCode";

	private static final String DUNNING_LOT_OUTPUT_DIR = "bayad.dunning.lotOutputDir";

	private static final String MEVEO_PROVIDER_URL = "meveo.providerUrl";
	private static final String MEVEO_CUSTOMER_ACCOUNT_SERVICE_JNDI_NAME = "meveo.customerAccountService.jndiName";
	private static final String MEVEO_MATCHING_CODE_SERVICE_JNDI_NAME = "meveo.matchingCodeService.jndiName";

	private static final String USER_SYSTEM_ID = "bayad.userSystemId";
	private static final String DECIMAL_FORMAT = "bayad.decimalFormat";

	private static final String PERSISTENCE_UNIT_NAME = "bayad.persistenceUnitName";
	private static final String DB_USERNAME = "hibernate.connection.username";
	private static final String DB_PASSWORD = "hibernate.connection.password";
	private static final String DB_URL = "hibernate.connection.url";
	private static final String DB_HBM2DDL = "hibernate.hbm2ddl.auto";
	private static final String DB_DRIVER_CLASS = "hibernate.connection.driver_class";
	private static final String DB_SHOW_SQL = "hibernate.show_sql";
	private static final String DB_FORMAT_SQL = "hibernate.format_sql";
	private static final String DB_DIALECT = "hibernate.dialect";

	public static long getInvoicesScanningInterval() {
		String value = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_SCANNING_INTERVAL);
		return Long.parseLong(value);
	}

	public static long getDunningScanningInterval() {
		String value = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DUNNING_SCANNING_INTERVAL);
		return Long.parseLong(value);
	}

	/**
	 * Get maximum number of working threads.
	 */
	public static int getThreadCount() {
		String value = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(WORKING_THREADS);
		return Integer.parseInt(value);
	}

	public static String getInvoicesSourceFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_SOURCE_FILES_DIR);
	}

	public static String getInvoicesRejectedFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_REJECTED_FILES_DIR);
	}

	public static String getInvoicesAcceptedFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_ACCEPTED_FILES_DIR);
	}

	public static String getInvoicesFileProcessingExtension() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_PROCESSING_EXT);
	}

	public static String getInvoicesErrorFileExtension() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_ERROR_FILE_EXT);
	}

	public static String getInvoicesFileExtensions() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_FILE_EXT_TO_PROCESS);
	}

	public static String getDateFormatInvoicesFile() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_DATE_FORMAT);
	}

	public static String getInvoiceOccCode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_OCC_CODE);
	}

	public static String getDDRequestHeaderDDMode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_HEADER_DDMODE);
	}

	public static String getDDRequestHeaderReference() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_HEADER_REFRENCE);
	}

	public static String getDDRequestLineDDMode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_LINE_DDMODE);
	}

	public static String getDDRequestLineLabel() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_LINE_LABEL);
	}

	public static String getDDRequestFooterDDMode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_FOOTER_DDMODE);
	}

	public static String getDunningLotFileName() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DUNNING_LOT_FILENAME);
	}

	public static String getDunningLotOutputDir() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DUNNING_LOT_OUTPUT_DIR);
	}

	public static String getDunningLotFileNameExtention() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DUNNING_LOT_FILENAME_EXT);
	}

	public static Long getUserSystemId() {
		return new Long(ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(USER_SYSTEM_ID));
	}

	/**
	 * Get persistence unit name.
	 * 
	 */
	public static String getPersistenceUnitName() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(PERSISTENCE_UNIT_NAME);
	}

	/**
	 * Get overridden persistence configuration.
	 */
	public static Map<String, String> getPersistenceProperties() {
		String username = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_USERNAME);
		String password = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_PASSWORD);
		String url = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_URL);
		String hbm2ddl = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_HBM2DDL);
		String driverClass = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_DRIVER_CLASS);
		String showSql = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_SHOW_SQL);
		String formatSql = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_FORMAT_SQL);
		String dialect = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DB_DIALECT);

		Map<String, String> params = new HashMap<String, String>();
		if (username != null) {
			params.put(DB_USERNAME, username);
		}
		if (password != null) {
			params.put(DB_PASSWORD, password);
		}
		if (url != null) {
			params.put(DB_URL, url);
		}
		if (hbm2ddl != null) {
			params.put(DB_HBM2DDL, hbm2ddl);
		}
		if (driverClass != null) {
			params.put(DB_DRIVER_CLASS, driverClass);
		}
		if (showSql != null) {
			params.put(DB_SHOW_SQL, showSql);
		}
		if (formatSql != null) {
			params.put(DB_FORMAT_SQL, showSql);
		}
		if (dialect != null) {
			params.put(DB_DIALECT, dialect);
		}
		return params;
	}

	public static String getInvoicesErrorFilePrefix() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_ERROR_FILE_PREFIX);
	}

	public static String getInvoicesErrorDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_ERROR_FILES_DIR);
	}

	public static String getInvoicesIgnoredFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(INVOICES_IGNORED_FILES_DIR);
	}

	public static String getDDRequestOutputDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_OUTPUT_DIR);
	}

	public static String getDDRequestFileNamePrefix() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_FILE_NAME_PREFIX);
	}

	public static String getDDRequestFileNameExtension() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_FILE_NAME_EXTENSION);
	}

	public static String getDirectDebitOccCode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_OCC_CODE);
	}

	public static int getDateValueAfter() {
		return Integer.parseInt(ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_DATE_VALUE_AFTER));
	}

	public static String getMeveoProviderUrl() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(MEVEO_PROVIDER_URL);
	}

	public static String getMeveoCustomerAccountServiceJndiName() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(MEVEO_CUSTOMER_ACCOUNT_SERVICE_JNDI_NAME);
	}

	public static long getDDRequestScanningInterval() {
		String value = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_SCANNING_INTERVAL);
		return Long.parseLong(value);
	}

	public static String getMeveoMatchingCodeServiceJndiName() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(MEVEO_MATCHING_CODE_SERVICE_JNDI_NAME);
	}

	public static String getDDRequestAddLastEmptyLine() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_ADD_LAST_EMPTY_LINE);
	}

	public static int getDunningBlanceFlag() {
		return Integer.parseInt(ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DUNNING_BALANCE_FLAG, "1"));
	}

	public static String getDunningOccCode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DUNNING_OCC_CODE);
	}

	public static String getDDRequestIsTruncateString() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DDREQUEST_IS_TRUNCATE_STRING);
	}

	public static String getDecimalFormat() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(DECIMAL_FORMAT);
	}

	public static long getTIPScanningInterval() {
		String value = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_SCANNING_INTERVAL);
		return Long.parseLong(value);
	}

	public static String getTIPSourceFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_SOURCE_FILES_DIR);
	}

	public static String getTIPFileExtensions() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_FILE_EXT_TO_PROCESS);
	}

	public static String getTIPAcceptedFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_ACCEPTED_FILES_DIR);
	}

	public static String getTIPRejectedFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_REJECTED_FILES_DIR);
	}

	public static String getTIPErrorDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_ERROR_FILES_DIR);
	}

	public static String getTIPErrorFilePrefix() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_ERROR_FILE_PREFIX);
	}

	public static String getTIPErrorFileExtension() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_ERROR_FILE_EXT);
	}

	public static String getTIPDateFormat() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_DATE_FORMAT);
	}

	public static String getCheckOccCode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_CHECK_OCC_CODE);
	}

	public static String getTIPOccCode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(TIP_OCC_CODE);
	}
	
	public static long getBankFileScanningInterval() {
		String value = ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_SCANNING_INTERVAL);
		return Long.parseLong(value);
	}

	public static String getBankFileSourceFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_SOURCE_FILES_DIR);
	}

	public static String getBankFileFileExtensions() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_FILE_EXT_TO_PROCESS);
	}

	public static String getBankFileAcceptedFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_ACCEPTED_FILES_DIR);
	}

	public static String getBankFileRejectedFilesDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_REJECTED_FILES_DIR);
	}

	public static String getBankFileErrorDirectory() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_ERROR_FILES_DIR);
	}

	public static String getBankFileErrorFilePrefix() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_ERROR_FILE_PREFIX);
	}

	public static String getBankFileErrorFileExtension() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_ERROR_FILE_EXT);
	}

	public static String getBankFileDateFormat() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_DATE_FORMAT);
	}

	public static String getBankFileOccCode() {
		return ParamBean.getInstance(BAYAD_PROPERTIES_FILENAME).getProperty(BANK_FILE_OCC_CODE);
	}
	
}
