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
package org.meveo.bayad.ddrequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.service.BayadServices;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.admin.User;
import org.meveo.model.payments.DDRequestLotOp;
import org.meveo.model.payments.DDRequestOpEnum;
import org.meveo.model.payments.DDRequestOpStatusEnum;
import org.meveo.persistence.MeveoPersistence;

/**
 * DDrequest Task
 * 
 * @author anasseh
 * @created 03.12.2010
 */
public class DDRequestTask implements Runnable {

	private static final Logger logger = Logger.getLogger(DDRequestTask.class);

	public DDRequestTask() {
	}

	public void run() {
		logger.info("Starting DDRequestTask tasks...");
		User user = null;
		try {
			user = BayadUtils.getUserBayadSystem();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (user == null) {
			logger.error("Cannot found user system Bayad");
			return;
		}
		List<DDRequestLotOp> ddrequestOps = getDDRequestOps();
		BayadServices bayadServices = new BayadServices();
		EntityManager em = MeveoPersistence.getEntityManager();
		logger.info("ddrequestOps founded:" + ddrequestOps.size());
		for (DDRequestLotOp ddrequestLotOp : ddrequestOps) {

			try {
				if (ddrequestLotOp.getDdrequestOp() == DDRequestOpEnum.CREATE) {
					bayadServices.createDDRquestLot(ddrequestLotOp.getFromDueDate(), ddrequestLotOp.getToDueDate(), user, ddrequestLotOp.getProvider());
				}
				if (ddrequestLotOp.getDdrequestOp() == DDRequestOpEnum.FILE) {
					bayadServices.exportDDRequestLot(ddrequestLotOp.getDdrequestLOT().getId());
				}
				ddrequestLotOp.setStatus(DDRequestOpStatusEnum.PROCESSED);
			} catch (Exception e) {
				e.printStackTrace();
				ddrequestLotOp.setStatus(DDRequestOpStatusEnum.ERROR);
				ddrequestLotOp.setErrorCause(StringUtils.truncate(e.getMessage(), 255, true));
			}
			try {
				if (ddrequestLotOp.getStatus() != DDRequestOpStatusEnum.WAIT) {
					em.getTransaction().begin();
					ddrequestLotOp.getAuditable().setUpdater(user);
					ddrequestLotOp.getAuditable().setUpdated(new Date());
					em.merge(ddrequestLotOp);
					em.getTransaction().commit();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<DDRequestLotOp> getDDRequestOps() {
		List<DDRequestLotOp> ddrequestOps = new ArrayList<DDRequestLotOp>();
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			ddrequestOps = (List<DDRequestLotOp>) em.createQuery("from " + DDRequestLotOp.class.getSimpleName() + " where status=:status")
					.setParameter("status", DDRequestOpStatusEnum.WAIT).getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ddrequestOps;
	}
}
