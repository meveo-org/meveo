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
package org.meveo.bayad.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.crm.Provider;

/**
 * @author anasseh
 * @created 06.12.2010
 * 
 */

public class DDRequestFileBuilder {

	public DDRequestFileBuilder(Provider provider) {
		this.provider = provider;
	}

	private static final Logger logger = Logger.getLogger(DDRequestFileBuilder.class);
	private final static String BREAK_LINE = "\n";
	ResourceBundle resource = ResourceBundle.getBundle("messages");

	private StringBuffer sb = new StringBuffer();
	private Provider provider;

	public DDRequestFileBuilder addHeader(String valueDate, Long ddrequestLotId) throws Exception {

		String directDebitMode = enleverAccent(BayadConfig.getDDRequestHeaderDDMode());
		String issuerNumber = enleverAccent(provider.getBankCoordinates().getIssuerNumber());
		String issuerName = enleverAccent(provider.getBankCoordinates().getIssuerName());
		String reference = enleverAccent(String.format(BayadConfig.getDDRequestHeaderReference(), ddrequestLotId));
		String guichetNumber = enleverAccent(provider.getBankCoordinates().getBranchCode());
		String accountNumber = enleverAccent(provider.getBankCoordinates().getAccountNumber());
		String bankCode = enleverAccent(provider.getBankCoordinates().getBankCode());
		String bankID = enleverAccent(provider.getBankCoordinates().getBankId());

		directDebitMode = checkValue("directDebitModeHeader", directDebitMode, 4);
		issuerNumber = checkValue("issuerNumber", issuerNumber, 6);
		valueDate = checkValue("valueDate", valueDate, 5);
		issuerName = checkValue("issuerName", issuerName, 24);
		reference = checkValue("referenceHeader", reference, 7);
		guichetNumber = checkValue("numGuichetRIB", guichetNumber, 5);
		accountNumber = checkValue("numCompteRIB", accountNumber, 11);
		bankCode = checkValue("codeBank", bankCode, 5);
		bankID = checkValue("bankID", bankID, 14);

		sb.append(directDebitMode);
		sb.append(StringUtils.getStringAsNChar("", 8));
		sb.append(issuerNumber);
		sb.append(StringUtils.getStringAsNChar("", 7));
		sb.append(valueDate);
		sb.append(issuerName);
		sb.append(reference);
		sb.append(StringUtils.getStringAsNChar("", 19));
		sb.append("E");
		sb.append(StringUtils.getStringAsNChar("", 5));
		sb.append(guichetNumber);
		sb.append(accountNumber);
		sb.append(StringUtils.getStringAsNChar("", 2));
		sb.append(bankID);
		sb.append(StringUtils.getStringAsNChar("", 31));
		sb.append(bankCode);
		sb.append(StringUtils.getStringAsNChar("", 6));
		sb.append(BREAK_LINE);
		logger.debug("add header ok");
		return this;
	}

	public DDRequestFileBuilder addLine(String issuerNumber, String reference, String recipientName, String recipientBankName, String recipientNumGuichet,
			String recipientAccountNumber, BigDecimal amount, String recipientCodeBank) throws Exception {

		String directDebitMode = enleverAccent(BayadConfig.getDDRequestLineDDMode());
		String label = enleverAccent(String.format(BayadConfig.getDDRequestLineLabel(), reference));
		directDebitMode = checkValue("directDebitModeLine", directDebitMode, 4);
		issuerNumber = checkValue("issuerNumber", issuerNumber, 6);
		reference = checkValue("referenceLine", reference, 12);
		recipientName = checkValue("recipientName", recipientName, 24);
		recipientBankName = checkValue("recipientBankName", recipientBankName, 20);
		recipientNumGuichet = checkValue("recipientNumGuichet", recipientNumGuichet, 5);
		recipientAccountNumber = checkValue("recipientAccountNumber", recipientAccountNumber, 11);
		label = checkValue("label", label, 31);
		recipientCodeBank = checkValue("recipientCodeBank", recipientCodeBank, 5);

		sb.append(directDebitMode);
		sb.append(StringUtils.getStringAsNChar("", 8));
		sb.append(issuerNumber);
		sb.append(reference);
		sb.append(recipientName);
		sb.append(recipientBankName);
		sb.append(StringUtils.getStringAsNChar("", 1));
		sb.append(StringUtils.getStringAsNChar("", 3));
		sb.append(StringUtils.getStringAsNChar("", 8));
		sb.append(recipientNumGuichet);
		sb.append(recipientAccountNumber);
		sb.append(StringUtils.getLongAsNChar((amount.multiply(new BigDecimal(100))).longValue(), 16));
		sb.append(label);
		sb.append(recipientCodeBank);
		sb.append(StringUtils.getStringAsNChar("", 6));
		sb.append(BREAK_LINE);
		logger.debug("add line ok");
		return this;
	}

	public DDRequestFileBuilder addFooter(BigDecimal totalAmount) throws Exception {
		String directDebitMode = enleverAccent(BayadConfig.getDDRequestFooterDDMode());
		String issuerNumber = enleverAccent(provider.getBankCoordinates().getIssuerNumber());
		directDebitMode = checkValue("directDebitMode", directDebitMode, 4);
		issuerNumber = checkValue("issuerNumber", issuerNumber, 6);

		sb.append(directDebitMode);
		sb.append(StringUtils.getStringAsNChar("", 8));
		sb.append(issuerNumber);
		sb.append(StringUtils.getStringAsNChar("", 84));
		sb.append(StringUtils.getLongAsNChar((totalAmount.multiply(new BigDecimal(100))).longValue(), 16));
		sb.append(StringUtils.getStringAsNChar("", 42));
		if ("true".equalsIgnoreCase(BayadConfig.getDDRequestAddLastEmptyLine())) {
			sb.append(BREAK_LINE);
		}
		logger.debug("add footer ok");
		return this;
	}

	public String toString() {
		return sb.toString();
	}

	public boolean isEmpty() {
		return sb.length() == 0;
	}

	public void toFile(String absolutFfilename) {
		FileWriter fw = null;
		try {
			File tmp = new File(absolutFfilename);
			File createDir = tmp.getParentFile();

			createDir.mkdirs();
			fw = new FileWriter(absolutFfilename, false);
			fw.write(sb.toString());
			fw.close();
			logger.debug("create file ok");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String checkValue(String name, String value, int maxLength) throws Exception {
		if (value == null) {
			throw new Exception(resource.getString("DDRequestFileBuilder." + name + ".isNull"));
		}
		value = enleverAccent(value);
		if (value.length() > maxLength) {
			if ("true".equals(BayadConfig.getDDRequestIsTruncateString())) {
				return StringUtils.truncate(value, maxLength, false);
			} else {
				throw new Exception(String.format(resource.getString("DDRequestFileBuilder." + name + ".maxLength"), maxLength));
			}
		} else {
			return StringUtils.getStringAsNChar(value, maxLength);
		}
	}

	private String enleverAccent(String value) {
		if (StringUtils.isBlank(value)) {
			return value;
		}
		return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
	}
}
