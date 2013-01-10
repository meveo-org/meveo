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
package org.myevo.rating.model;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EDR {

    protected static final Format FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    protected static final String SEPARATOR = ";";

    // private String offerCode;
    private String type = "DATA";
    private String userId;
    private String serviceId;
    private String id;
    private String APid;
    private Date consumptionDate;
    private String accessPointId;
    private String IMSI;
    private String MSISDN;
    private Long downloadVolume;
    private Long uploadVolume;
    private Long duration;
    private String originZone;
    private String accessPointNameNI;
    private String plmn;
    private String plmnFromTicket;
    private Boolean roaming;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getConsumptionDate() {
        return consumptionDate;
    }

    public void setConsumptionDate(Date consumptionDate) {
        this.consumptionDate = consumptionDate;
    }

    public Long getDownloadVolume() {
        return downloadVolume;
    }

    public void setDownloadVolume(Long downloadVolume) {
        this.downloadVolume = downloadVolume;
    }

    public Long getUploadVolume() {
        return uploadVolume;
    }

    public void setUploadVolume(Long uploadVolume) {
        this.uploadVolume = uploadVolume;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getOriginZone() {
        return originZone;
    }

    public void setOriginZone(String originZone) {
        this.originZone = originZone;
    }

    public String getAccessPointNameNI() {
        return accessPointNameNI;
    }

    public void setAccessPointNameNI(String accessPointNameNI) {
        this.accessPointNameNI = accessPointNameNI;
    }

    public String getPlmn() {
        return plmn;
    }

    public void setPlmn(String plmn) {
        this.plmn = plmn;
    }

    public String getPlmnFromTicket() {
        return plmnFromTicket;
    }

    public void setPlmnFromTicket(String plmnFromTicket) {
        this.plmnFromTicket = plmnFromTicket;
    }

    public Boolean getRoaming() {
        return roaming;
    }

    public void setRoaming(Boolean roaming) {
        this.roaming = roaming;
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String iMSI) {
        IMSI = iMSI;
    }

    public String getMSISDN() {
        return MSISDN;
    }

    public void setMSISDN(String mSISDN) {
        MSISDN = mSISDN;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getAPid());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getServiceId());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getId());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(FORMAT.format(getConsumptionDate()));
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getAccessPointId());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getIMSI());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getMSISDN());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getDownloadVolume());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getUploadVolume());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getDuration());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getOriginZone());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getAccessPointNameNI());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getPlmn());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getPlmnFromTicket());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(getRoaming());

        return stringBuilder.toString();
    }

    // public String getOfferCode() {
    // return offerCode;
    // }
    //
    // public void setOfferCode(String offerCode) {
    // this.offerCode = offerCode;
    // }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccessPointId() {
        return accessPointId;
    }

    public void setAccessPointId(String accessPointId) {
        this.accessPointId = accessPointId;
    }

    public String getAPid() {
        return APid;
    }

    public void setAPid(String aPid) {
        APid = aPid;
    }

}
