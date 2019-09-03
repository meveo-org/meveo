package org.meveo.api.billing;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseApi;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.quote.Quote;
import org.meveo.model.quote.QuoteItem;
import org.meveo.model.quote.QuoteStatusEnum;
import org.meveo.service.catalog.impl.ServiceTemplateService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.quote.QuoteService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.wf.WorkflowService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Wassim Drira
 * @lastModifiedVersion 5.0
 *
 */
@Stateless
public class QuoteApi extends BaseApi {

    @Inject
    private Logger log;

    @Inject
    private QuoteService quoteService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private WorkflowService workflowService;

    @Inject
    private ServiceTemplateService serviceTemplateService;


    @Inject
    private ScriptInstanceService scriptInstanceService;


    /**
     * Initiate workflow on quote. If workflow is enabled on Quote class, then execute workflow. If workflow is not enabled - then process the quote right away.
     * 
     * @param quote Quote
     * 
     * @return worked flow quote
     * @throws BusinessException business exception
     */
    public Quote initiateWorkflow(Quote quote) throws BusinessException {
        if (workflowService.isWorkflowSetup(Quote.class)) {
            quote = (Quote) workflowService.executeMatchingWorkflows(quote);

        } else {
            try {
                quote = processQuote(quote);
            } catch (MeveoApiException e) {
                throw new BusinessException(e);
            }
        }
        return quote;

    }

    /**
     * Process the quote for workflow.
     * 
     * @param quote processed quote.
     * @return processed quote.
     * @throws BusinessException business exception
     * @throws MeveoApiException meveo api exception.
     */
    public Quote processQuote(Quote quote) throws BusinessException, MeveoApiException {

        // Nothing to process in final state
        if (quote.getStatus() == QuoteStatusEnum.CANCELLED || quote.getStatus() == QuoteStatusEnum.ACCEPTED || quote.getStatus() == QuoteStatusEnum.REJECTED) {
            return quote;
        }
        log.info("Processing quote {}", quote.getCode());
        for (QuoteItem quoteItem : quote.getQuoteItems()) {
            processQuoteItem(quote, quoteItem);
        }
        quote.setStatus(QuoteStatusEnum.PENDING);
        for (QuoteItem quoteItem : quote.getQuoteItems()) {
            quoteItem.setStatus(QuoteStatusEnum.PENDING);
        }
        quote = quoteService.update(quote);
        log.trace("Finished processing quote {}", quote.getCode());
        return quote;
    }

    /**
     * Process quote item for workflow
     * 
     * @param quote Quote
     * @param quoteItem Quote item
     * 
     * @throws BusinessException business exception
     * @throws MeveoApiException meveo api exception.
     */
    private void processQuoteItem(Quote quote, QuoteItem quoteItem) throws BusinessException, MeveoApiException {

        log.info("Processing quote item {} {}", quote.getCode(), quoteItem.getItemId());

        log.info("Finished processing quote item {} {}", quote.getCode(), quoteItem.getItemId());
    }



    /**
     * @param quoteId quote id
     * @throws EntityDoesNotExistsException exception when entity is not existed.
     * @throws ActionForbiddenException forbidden exception
     * @throws BusinessException business exception
     */
    public void deleteQuote(String quoteId) throws EntityDoesNotExistsException, ActionForbiddenException, BusinessException {

        Quote quote = quoteService.findByCode(quoteId);

        if (quote.getStatus() == QuoteStatusEnum.IN_PROGRESS || quote.getStatus() == QuoteStatusEnum.PENDING) {
            quoteService.remove(quote);
        }
    }

}
