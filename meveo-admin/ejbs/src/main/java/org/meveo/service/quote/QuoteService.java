package org.meveo.service.quote;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.model.quote.Quote;
import org.meveo.service.base.BusinessService;

import javax.ejb.Stateless;

@Stateless
public class QuoteService extends BusinessService<Quote> {


    @Override
    public void create(Quote quote) throws BusinessException {

        if (quote.getQuoteItems() == null || quote.getQuoteItems().isEmpty()) {
            throw new ValidationException("At least one quote line item is required");
        }

        super.create(quote);
    }

    @Override
    public Quote update(Quote quote) throws BusinessException {

        if (quote.getQuoteItems() == null || quote.getQuoteItems().isEmpty()) {
            throw new ValidationException("At least one quote line item is required");
        }

        return super.update(quote);
    }
}