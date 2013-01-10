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
package org.manaty.telecom.mediation.process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.meveo.model.mediation.NumberingPlan;
import org.meveo.model.mediation.ZonningPlan;
import org.meveo.model.mediation.ZonningPlan.CDRTypeEnum;
import org.meveo.model.mediation.ZonningPlan.DirectionEnum;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.CDRType.CDRSubtype;
import org.manaty.telecom.mediation.ConfigurationException;
import org.manaty.telecom.mediation.MedinaConfig;
import org.manaty.telecom.mediation.cache.NumberingPlanCache;
import org.manaty.telecom.mediation.context.MediationContext;
import org.meveo.commons.utils.StringUtils;
import org.manaty.utils.CDRUtils;
import org.manaty.utils.SQLUtil;

/**
 * Zone resolving step.
 * 
 * @author Donatas Remeika
 * @created Mar 6, 2009
 */
public class ZonningStep extends AbstractProcessStep {

	private static final String UNKNOWN_ZONE = "UNKNOWN_ZONE";
	private static final String CUSTOMER_ON_NET = "MOBILE_SUPER_ON_NET";
	private static final String SMS_SUPER_ON_NET_ZONE_ID = "SMS_NATIONAUX_SUPER_ON_NET";
	private static final String ON_NET_ZONE_ID = "MOBILE_ON_NET";
	private static final String SMS_ON_NET_ZONE_ID = "SMS_NATIONAUX_ON_NET";
	
	protected static final String DEFAULT_ZONE = MedinaConfig.getDefaultZone();

	/**
	 * Cache for zone data.
	 */
	private static final Map<String, ZonningPlan> zoneCache = new ConcurrentHashMap<String, ZonningPlan>();

	public ZonningStep(AbstractProcessStep nextStep) {
		super(nextStep);
	}

	/**
	 * Does Zonning logic.
	 */
	@Override
	protected boolean execute(MediationContext context) {
		// Check origin zone!

		String originPLMN = context.getCDR().getOriginPLMN();
		if (originPLMN == null || originPLMN.length() == 0) {
			originPLMN = DEFAULT_PLMN;
		}
		context.setOriginPlmn(originPLMN);

		String originZone = getOriginZone(context);

		context.setOriginZone(originZone);

		// Check Target zone!

		// If usage is DATA target is the same as origin
		if (context.getType().getCDRSubType() == CDRSubtype.DATA) {
			context.setTargetZone(originZone);
			return true;
		} else {
			context.setTargetZone(getTargetZone(context));
			return true;
		}
	}

	/**
	 * Gets origin zone depending if ticket incoming/outgoing.
	 * 
	 * @param context
	 *            Mediation context.
	 * @return Origin zone.
	 */
	private String getOriginZone(MediationContext context) {
		CDRType type = context.getType();

//		if (CDRUtils.isIncomingTicket(context.getCDR())
//				&& CDRUtils.isRoamingTicket(context.getCDR())
//				&& "gue".equalsIgnoreCase(context.getCDR().getNature())) {
		if (CDRUtils.isIncomingTicket(context.getCDR()) && (
				(CDRUtils.isRoamingTicket(context.getCDR()) && "gue".equalsIgnoreCase(context.getCDR().getNature()))
					|| StringUtils.isBlank(CDRUtils.getPhoneNumber(context.getCDR(), context.getIncoming())))) {
			// if incoming roaming ticket or incoming ticket without phone number
			return UNKNOWN_ZONE;
		} else if ((type.getCDRSubType() == CDRSubtype.SMS || type.getCDRSubType() == CDRSubtype.VOICE) && context.getIncoming()) {
			return getZoneFromNumberingPlanAndSetSpecialNumberIfNeeded(context);
		} else {
			// for data and outgoing tickets zone is found from origin plmn
			return getZoneFromZonningPlan(context);
		}
	}

	/**
	 * Gets target zone depending if ticket incoming/outgoing.
	 * 
	 * @param context
	 *            Mediation context.
	 * @return Target zone.
	 */
	private String getTargetZone(MediationContext context) {
		CDRType type = context.getType();
		// incoming tickets
		if ((type.getCDRSubType() == CDRSubtype.SMS || type.getCDRSubType() == CDRSubtype.VOICE)
				&& context.getIncoming()) {
			return getZoneFromZonningPlan(context);
		// outgoing tickets
		} else {
			if (CDRUtils.isSSPTicket(context.getCDR())) {
				return SPP_TICKET_TARGET_ZONE;
			} else {
				return getZoneFromNumberingPlanAndSetSpecialNumberIfNeeded(context);
			}
		}
	}

	/**
	 * Checks MVNO prefix. If fin
	 * 
	 * @return TargetZone. If not found then return null.
	 */
	private String checkMVNORoutingPrefix(boolean isOutgoing, CDRType type, CDR cdr, String offerCode, boolean isInternationalCall) {
		// load zone from mvno routing if exits and if ticket VOICE/Outgoing
		if (isOutgoing && type.getCDRSubType() == CDRSubtype.VOICE) {
			return NumberingPlanCache.getInstance().getZoneFromRouting(
					offerCode, cdr.getMVNORouting(), isInternationalCall);
			
		}
		return null;
	}
	
	/**
	 * Gets zone from numbering plan or MVNORouting and sets special number if
	 * needed.
	 * 
	 * @param context
	 *            Mediation context.
	 * @return ZoneId.
	 */
	private String getZoneFromNumberingPlanAndSetSpecialNumberIfNeeded(
			MediationContext context) {
		// target zone to return
		String targetZone = null;

		CDR cdr = context.getCDR();
		String offerCode = context.getAccess().getSubscription().getOffer().getCode();
		String phoneNumber = CDRUtils
				.getPhoneNumber(cdr, context.getIncoming());
		boolean isOutgoing = !context.getIncoming();
		CDRType type = context.getType();
		
		// if MVNO prefix not found targetZone = null.
		targetZone = checkMVNORoutingPrefix(isOutgoing, type, cdr, offerCode, context.isInternationalCall());

		// log if zone found
		if (targetZone != null) {
			logger.debug(String.format("MVNO routing found with: mvnoRoutingPrefix = '%s, offerCode = '%s'", 
					cdr.getMVNORouting(), offerCode));
		}
		// if no zone in mvno routing search private numbering plan
		if (targetZone == null) {
			// called number for outgoing, calling number for incoming
			if (phoneNumber == null) {
				throw new IllegalStateException(
						"Phone number was not found. Probably it is incoming ticket, that has no MSISDN in its line.");
			}

			// check for private numbering plan
			targetZone = getNumberingPlanZoneAndSetSpecialNumber(phoneNumber,
					offerCode, type, isOutgoing, context);
			
			// log if zone found
			if (targetZone != null) {
				logger.debug(String.format("Private numbering plan found with: phoneNumber = '%s, offerCode = '%s', type = '%s', isOutgoing = '%s'", 
						phoneNumber, offerCode, type, isOutgoing));
			}
		}

		// if private numbering plan was not found then check for on_net
		if (targetZone == null) {
			// if on_net overwrite zone to ON_NET. Still need to load numbering
			// plan before to
			// check if number is not 'specialType' (SMS_PLUS or DATA_PLUS)
			if (onNetOffers.contains(context.getAccess().getSubscription().getOffer().getCode())) {
				if (context.getOnNET()) {
					targetZone = getOnNETZone(context);
					// load public numbering plan (this method will set special
					// number on context if needed (SMS_PLUS/DATA_PLUS))
					getNumberingPlanZoneAndSetSpecialNumber(phoneNumber, null,
							type, isOutgoing, context);
				}
			}
			// log if zone found
			if (targetZone != null) {
				logger.debug(String.format("ON NET zone found: operator = '%s, onNETField = '%s', offerCode = '%s'", 
						cdr.getOperator(), cdr.getOnNET(), offerCode));
			}
		}

		// if still target zone is not found check for public numbering plan
		if (targetZone == null) {
			targetZone = getNumberingPlanZoneAndSetSpecialNumber(phoneNumber, null,
					type, isOutgoing, context);
			// log if zone found
			if (targetZone != null) {
				logger.debug(String.format("Public numbering plan found with: phoneNumber = '%s, type = '%s', isOutgoing = '%s'", 
						phoneNumber, type, isOutgoing));
			}
		}

		// if no zone is found log and return default zone
		if (targetZone == null) {
			// log if no numbering plan was found
			if (phoneNumber == null || phoneNumber.length() < 4)
				logger
						.error("[ZonningStep] calledNumber.length < 4 => no numbering plan found => we use DEFAULT_ZONE. callNumber="
								+ phoneNumber
								+ ", offerCode="
								+ offerCode
								+ ", CDRType="
								+ type
								+ ", accessPointId="
								+ context.getAccess().getId());

			targetZone = DEFAULT_ZONE;
		}

		return targetZone;
	}

	/**
	 * Gets zone from numbering plan by provided arguments. If numbering plan is
	 * found and it has special number set - set it to context.
	 */
	private String getNumberingPlanZoneAndSetSpecialNumber(String phoneNumber,
			String offerCode, CDRType type, boolean isOutgoing,
			MediationContext context) {

		NumberingPlan numberingPlan = NumberingPlanCache.getInstance()
				.getNumberingPlanFromCache(phoneNumber, offerCode, type, isOutgoing);
		if (numberingPlan != null) {
			if ("SAC".equals(context.getCDR().getCDRType())
					&& numberingPlan.getSpecialNumberType() != null) {
				context.setSpecialNumberType(numberingPlan.getSpecialNumberType());
			}
			return numberingPlan.getZoneId();
		} else {
			return null;
		}
	}

	/**
	 * Get ZonningPlan bean either from cache or database.
	 * 
	 * @param plmn
	 *            PLMN code.
	 * @param offerCode
	 *            Offer code.
	 * @return ZonningPlan object.
	 */
	protected ZonningPlan getZone(String plmn, String offerCode,
			CDRTypeEnum cdrType, DirectionEnum direction,
			PreparedStatement statement) {
		if (plmn == null || offerCode == null) {
			return null;
		}
		String cacheEntryKey = getCacheEntryName(plmn, offerCode, cdrType,
				direction);
		ZonningPlan zone = getFromCache(cacheEntryKey);
		if (zone != null) {
			return zone;
		}
		try {
			statement.setString(1, offerCode);
			statement.setString(2, plmn);
			statement.setString(3, String.valueOf(CDRTypeEnum.ALL));
			statement.setString(4, String.valueOf(cdrType));
			statement.setString(5, String.valueOf(direction));
			statement.setString(6, String.valueOf(DirectionEnum.ALL));
			statement.setString(7, String.valueOf(direction));
			String zoneId = SQLUtil.getStringAndCloseResultSet(statement
					.executeQuery());
			if (zoneId == null) {
				return null;
			}
			zone = new ZonningPlan();
			zone.setZoneId(zoneId);
			zone.setOfferCode(offerCode);
			zone.setPlmnCode(plmn);
			putToCache(cacheEntryKey, zone);
			return zone;
		} catch (SQLException e) {
			throw new ConfigurationException("Could not access database", e);
		}
	}

	/**
	 * Check is msisdn and called number belongs to the same owner (party), if
	 * so returns super on_net zone, otherwise returns simple on_net zone.
	 * 
	 * @param context
	 *            Mediation context.
	 * 
	 * @return Return On_NET zone.
	 */
	private String getOnNETZone(MediationContext context) {
		try {
			if (superOnNetOffers.contains(context.getAccess().getSubscription().getOffer().getCode())) {
				String msisdn = context.getCDR().getMSISDN();
				String callingImsi = context.getCDR().getIMSI();
				String phoneNumber = CDRUtils.getPhoneNumber(context.getCDR(), context.getIncoming());

				// CASE 1: if calling MSISDN == called phoneNumber, then SUPER_ON_NET:
				//         CAUTION: sometimes, calling MSISDN is hidden behind a "33660000000" generic MSISDN. So, here, we can't detect all occurrences of a line calling itself.
				if(msisdn != null && phoneNumber != null && msisdn.equals(phoneNumber)){
					logger.debug(String.format("ZonningStep.getOnNETZone. CASE 1 of SuperOnNetZone (calling MSISDN == called phoneNumber: %s)", phoneNumber));
					return getSuperOnNetZoneID(context.getType());

				} else {
					// Gets partyId of ALLOCATED lines matching calling IMSI or called MSISDN :
					PreparedStatement statementCheckPhoneNumberOwners = context.getProcessor().getStatementCheckPhoneNumberOwners();
					statementCheckPhoneNumberOwners.setString(1, callingImsi);
					statementCheckPhoneNumberOwners.setString(2, phoneNumber);
					List<Object[]> returnedParties = SQLUtil.getListOfValuesAndCloseResultSet(statementCheckPhoneNumberOwners.executeQuery(), Long.class, String.class);
	
					// CASE 2: if calling imsi and called phoneNumber are both found in DB and they belong to the same party, then SUPER_ON_NET.
					if (returnedParties.size() == 2 && returnedParties.get(0)[0].equals(returnedParties.get(1)[0])) {
						logger.debug(String.format("ZonningStep.getOnNETZone. CASE 2 of SuperOnNetZone (calling imsi and called phoneNumber belong to the same party: callingImsi=%s, phoneNumber=%s)", callingImsi, phoneNumber));
						return getSuperOnNetZoneID(context.getType());

					// CASE 3: if only one line is returned (it is the calling line), and if its MSISDN equals to the phoneNumber, then SUPER_ON_NET. 
					//         (the line called itself, but we couldn't detect it at CASE 1 because of the "33660000000" pb).
					} else if (returnedParties.size() == 1 && phoneNumber != null && phoneNumber.equals(returnedParties.get(0)[1])) {
						logger.debug(String.format("ZonningStep.getOnNETZone. CASE 3 of SuperOnNetZone (the line called itself, but we couldn't detect it at CASE 1 because of the 33660000000 pb: msisdn=%s, callingImsi=%s, phoneNumber=%s)", msisdn, callingImsi, phoneNumber));
						return getSuperOnNetZoneID(context.getType());
					}
				}
			}
		} catch (SQLException e) {
			throw new ConfigurationException("Could not access database", e);
		}
		return getOnNetZoneID(context.getType());
	}

	/**
	 * Gets zone using ticket plmn. Returns from zonning plan.
	 * 
	 * @param context
	 *            Mediation context.
	 * @return Zone.
	 */
	private String getZoneFromZonningPlan(MediationContext context) {
		CDRTypeEnum cdrTypeEnum = CDRUtils.convertToCDRTypeEnum(context
				.getType());
		// direction for DATA is not used
        DirectionEnum directionEnum = cdrTypeEnum != CDRTypeEnum.DATA ? CDRUtils.convertToDirectionEnum(context.getIncoming()) : null;
		ZonningPlan originZonePlan = getZone(context.getOriginPlmn(), context
				.getAccess().getSubscription().getOffer().getCode(), cdrTypeEnum, directionEnum,
				context.getProcessor().getStatementFindZone());
		return (originZonePlan == null ? DEFAULT_ZONE : originZonePlan
				.getZoneId());
	}

	/**
	 * Get zone from cache.
	 */
	protected ZonningPlan getFromCache(String name) {
		return zoneCache.get(name);
	}

	/**
	 * Put Zone to cache.
	 */
	protected void putToCache(String name, ZonningPlan zone) {
		zoneCache.put(name, zone);
	}

	/**
	 * Calculate cache entry name by offer and PLMN codes.
	 * 
	 * @param plmn
	 *            PLMN code.
	 * @param offerCode
	 *            Offer code.
	 * @return Generated key.
	 */
	private String getCacheEntryName(String plmn, String offerCode,
			CDRTypeEnum type, DirectionEnum direction) {
		return plmn + "-" + offerCode + "-" + type + "-"
				+ (direction != null ? direction : "");
	}

	/**
	 * This should be triggered by notification about zonning change.
	 */
	public static void clearCache() {
		zoneCache.clear();
	}

	/**
	 * Gets ONNet zone id depending on cdr type.
	 * 
	 * @param type
	 *            CDRType
	 * @return On net zone id.
	 */
	private String getOnNetZoneID(CDRType type) {
		return type == CDRType.SMS ? SMS_ON_NET_ZONE_ID : ON_NET_ZONE_ID;
	}

	/**
	 * Gets SuperONNet zone id depending on cdr type.
	 * 
	 * @param type
	 *            CDRType
	 * @return On net zone id.
	 */
	private String getSuperOnNetZoneID(CDRType type) {
		return type == CDRType.SMS ? SMS_SUPER_ON_NET_ZONE_ID : SUPER_ON_NET_ZONE_ID;
	}
}
