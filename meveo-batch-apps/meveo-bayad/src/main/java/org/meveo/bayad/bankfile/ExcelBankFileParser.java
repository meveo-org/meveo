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
package org.meveo.bayad.bankfile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.meveo.bayad.BayadConfig;
import org.meveo.commons.utils.DateUtils;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.BankOperation;
import org.meveo.persistence.MeveoPersistence;

public class ExcelBankFileParser {

	public ExcelBankFileParser(){
		
	}

	private ResourceBundle resource = ResourceBundle.getBundle("messages");

	public List<BankOperation>  parseFile(File file) {
		List<BankOperation> bankOps = new ArrayList<BankOperation>();
		Workbook workbook = null;
		try {
			workbook = Workbook
					.getWorkbook(file);
			Sheet sheet = workbook.getSheet(0);

			int startRow = 4;
			int codeOpColumn = 0;
			int dateOpColumn = 1;
			int dateValColumn = 2;
			int lebelColumn = 3;
			int refColumn = 4;
			int debitColumn = 5;
			int creditColumn = 6;


			int i = startRow;
			Cell[] currentRow = null;
			while ((currentRow = sheet.getRow(i)) != null
					&& "'05".equals(currentRow[codeOpColumn].getContents())) {
				BankOperation bankOperation = new BankOperation();
				try {
					bankOperation.setCodeOp(currentRow[codeOpColumn].getContents());
					bankOperation.setCredit(getBigDecimal(currentRow[creditColumn].getContents(), "Credit"));
					bankOperation.setDebit(getBigDecimal(currentRow[debitColumn].getContents(), "Debit"));
					bankOperation.setDateOp(getDate(currentRow[dateOpColumn].getContents(), "DateOp"));
					bankOperation.setDateVal(getDate(currentRow[dateValColumn].getContents(), "DateVal"));
					bankOperation.setInvocieId(getInvoiceId(sheet.getCell(lebelColumn, i).getContents(),
															sheet.getCell(lebelColumn, i + 1).getContents(),
															sheet.getCell(lebelColumn, i + 2).getContents(),
															sheet.getCell(lebelColumn, i + 3).getContents()));
					bankOperation.setLebel1("");
					bankOperation.setLebel2("");
					bankOperation.setLebel3("");
					bankOperation.setRefrence(currentRow[refColumn]
							.getContents());
					bankOperation.setValid(true);
					bankOperation.setFileName(file.getName().replaceAll(BayadConfig.getInvoicesFileProcessingExtension(), ""));
				} catch (Exception e) {
					e.printStackTrace();
					bankOperation.setErrorMessage(e.getMessage());
					bankOperation.setValid(false);
				}
				bankOperation.setProvider(getProvider());
				bankOps.add(bankOperation);

				i += 4;
			}
			System.out.println("bankOps:" + bankOps);

		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (workbook != null)
				workbook.close();
		}
		return bankOps;
	}

	private String getInvoiceId(String contents, String contents2,
			String contents3, String contents4) throws Exception {
		if (contents != null && contents.indexOf("EDFRF") != -1) {
			return contents.substring(contents.indexOf("EDFRF"));
		}
		if (contents2 != null && contents2.indexOf("EDFRF") != -1) {
			return contents2.substring(contents2.indexOf("EDFRF"));
		}
		if (contents3 != null && contents3.indexOf("EDFRF") != -1) {
			return contents3.substring(contents3.indexOf("EDFRF"));
		}
		if (contents4 != null && contents4.indexOf("EDFRF") != -1) {
			return contents4.substring(contents4.indexOf("EDFRF"));
		}
		throw new Exception(resource.getString("bankFile.CannotFoundInvoicId"));
	}

	private Date getDate(String contents, String fieldName)
			throws Exception {
		try {
			return DateUtils.parseDateWithPattern(contents, "dd/MM/yyyy");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(String.format(resource.getString("bankFile.invalidDate"), fieldName));
		}
	}

	private BigDecimal getBigDecimal(String value, String fieldName)
			throws Exception {
		if (value == null) {
			throw new Exception(String.format(resource.getString("bankFile.emptyField"), fieldName));
		}
		if (value.trim().length() == 0) {
			return BigDecimal.ZERO;
		}
		value = value.replaceAll(",", ".");
		try {
			return new BigDecimal(value);
		} catch (Exception e) {
			throw new Exception(String.format(resource.getString("bankFile.invalidNumber"), fieldName));
		}
	}

	private Provider getProvider() {
		Provider provider = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			provider = (Provider) em.createQuery("from " + Provider.class.getSimpleName() + " where code='PROVIDER' ").getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return provider;

	}
}


