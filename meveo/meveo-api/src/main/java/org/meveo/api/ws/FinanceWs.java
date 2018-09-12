package org.meveo.api.ws;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.finance.ReportExtractDto;
import org.meveo.api.dto.response.finance.ReportExtractResponseDto;
import org.meveo.api.dto.response.finance.ReportExtractsResponseDto;
import org.meveo.api.dto.response.finance.RunReportExtractDto;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @author Edward P. Legaspi
 * @lastModifiedVersion 5.0
 */
@WebService
public interface FinanceWs extends IBaseWs {

    @WebMethod
    ActionStatus createReportExtract(@WebParam(name = "reportExtract") ReportExtractDto postData);

    @WebMethod
    ActionStatus updateReportExtract(@WebParam(name = "reportExtract") ReportExtractDto postData);

    @WebMethod
    ActionStatus createOrUpdateReportExtract(@WebParam(name = "reportExtract") ReportExtractDto postData);

    @WebMethod
    ActionStatus removeReportExtract(@WebParam(name = "reportExtractCode") String reportExtractCode);

    @WebMethod
    ReportExtractsResponseDto listReportExtract();

    @WebMethod
    ReportExtractResponseDto findReportExtract(@WebParam(name = "reportExtractCode") String reportExtractCode);

    @WebMethod
    ActionStatus runReportExtract(@WebParam(name = "runReport") RunReportExtractDto postData);

}
