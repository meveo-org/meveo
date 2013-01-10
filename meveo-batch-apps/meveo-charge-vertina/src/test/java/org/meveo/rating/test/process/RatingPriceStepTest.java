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
package org.meveo.rating.test.process;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.meveo.commons.utils.DateUtils;
import org.meveo.model.catalog.PricePlanMatrix;
import org.meveo.rating.process.RatingPriceStep;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for price rating.
 * 
 * @author Ignas Lelys
 * @created Jan 18, 2011
 *
 */
public class RatingPriceStepTest {

    @Test(groups = { "unit" })
    public void testRatePriceEventCode() {
        RatingPriceStep step = new RatingPriceStep(null, null);
        
        PricePlanMatrix pricePlan1 = new PricePlanMatrix();
        pricePlan1.setEventCode("code1");

        PricePlanMatrix pricePlan2 = new PricePlanMatrix();
        pricePlan2.setEventCode("code1");

       // PricePlanMatrix pricePlan3 = new PricePlanMatrix();
       // pricePlan3.setEventCode("code3");
        
        List<PricePlanMatrix> pricePlans = Arrays.asList(pricePlan1, pricePlan2);
        
        PricePlanMatrix price1 = step.ratePrice(pricePlans, "code1", null, null, null, null, null);
        Assert.assertEquals(price1, pricePlan1);
        //PricePlanMatrix price3 = step.ratePrice(pricePlans, "code3", null, null, null, null, null);
        //Assert.assertEquals(price3, pricePlan3);
        //PricePlanMatrix priceNoCode = step.ratePrice(pricePlans, "bad_code", null, null, null, null, null);
        //Assert.assertNull(priceNoCode);
        
        //only pricePlan for one given charge code are given to ratePrice
    }
    
    @Test(groups = { "unit" })
    public void testRatePriceSubscriptionDate() {
        RatingPriceStep step = new RatingPriceStep(null, null);
        
        PricePlanMatrix pricePlan1 = new PricePlanMatrix();
        pricePlan1.setEventCode("code");
        pricePlan1.setStartSubscriptionDate(DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0));
        pricePlan1.setEndSubscriptionDate(DateUtils.newDate(2011, Calendar.JANUARY, 31, 0, 0, 0));

        PricePlanMatrix pricePlan2 = new PricePlanMatrix();
        pricePlan2.setEventCode("code");
        pricePlan2.setStartSubscriptionDate(DateUtils.newDate(2011, Calendar.JANUARY, 15, 0, 0, 0));
        pricePlan2.setEndSubscriptionDate(DateUtils.newDate(2011, Calendar.FEBRUARY, 15, 0, 0, 0));

        PricePlanMatrix pricePlan3 = new PricePlanMatrix();
        pricePlan3.setEventCode("code");
        pricePlan3.setStartSubscriptionDate(DateUtils.newDate(2011, Calendar.FEBRUARY, 15, 0, 0, 0));
        pricePlan3.setEndSubscriptionDate(DateUtils.newDate(2011, Calendar.MARCH, 31, 0, 0, 0));
        
        List<PricePlanMatrix> pricePlans = Arrays.asList(pricePlan1, pricePlan2, pricePlan3);
        
        PricePlanMatrix priceNull = step.ratePrice(pricePlans, "code", null, DateUtils.newDate(2011, Calendar.APRIL, 1, 0, 0, 0), null, null, null);
        Assert.assertNull(priceNull);
        PricePlanMatrix price1 = step.ratePrice(pricePlans, "code", null, DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 1), null, null, null);
        Assert.assertEquals(price1, pricePlan1);
        PricePlanMatrix priceEqualsToStartDate = step.ratePrice(pricePlans, "code", null, DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), null, null, null);
        Assert.assertEquals(priceEqualsToStartDate, pricePlan1);
        PricePlanMatrix price2 = step.ratePrice(pricePlans, "code", null, DateUtils.newDate(2011, Calendar.FEBRUARY, 14, 23, 59, 59), null, null, null);
        Assert.assertEquals(price2, pricePlan2);
        PricePlanMatrix priceEqualsToEndDate = step.ratePrice(pricePlans, "code", null, DateUtils.newDate(2011, Calendar.MARCH, 31, 0, 0, 0), null, null, null);
        Assert.assertNull(priceEqualsToEndDate);
    }
    
    @Test(groups = { "unit" })
    public void testRatePriceApplicationDateDate() {
        RatingPriceStep step = new RatingPriceStep(null, null);
        
        PricePlanMatrix pricePlan1 = new PricePlanMatrix();
        pricePlan1.setEventCode("code");
        pricePlan1.setStartRatingDate(DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0));
        pricePlan1.setEndRatingDate(DateUtils.newDate(2011, Calendar.JANUARY, 31, 0, 0, 0));

        PricePlanMatrix pricePlan2 = new PricePlanMatrix();
        pricePlan2.setEventCode("code");
        pricePlan2.setStartRatingDate(DateUtils.newDate(2011, Calendar.JANUARY, 15, 0, 0, 0));
        pricePlan2.setEndRatingDate(DateUtils.newDate(2011, Calendar.FEBRUARY, 15, 0, 0, 0));

        PricePlanMatrix pricePlan3 = new PricePlanMatrix();
        pricePlan3.setEventCode("code");
        pricePlan3.setStartRatingDate(DateUtils.newDate(2011, Calendar.FEBRUARY, 15, 0, 0, 0));
        pricePlan3.setEndRatingDate(DateUtils.newDate(2011, Calendar.MARCH, 31, 0, 0, 0));
        
        List<PricePlanMatrix> pricePlans = Arrays.asList(pricePlan1, pricePlan2, pricePlan3);
        
        PricePlanMatrix priceNull = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.APRIL, 1, 0, 0, 0), DateUtils.newDate(2011, Calendar.APRIL, 1, 0, 0, 0), null, null, null);
        Assert.assertNull(priceNull);
        PricePlanMatrix price1 = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 1), DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), null, null, null);
        Assert.assertEquals(price1, pricePlan1);
        PricePlanMatrix priceEqualsToStartDate = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), null, null, null);
        Assert.assertEquals(priceEqualsToStartDate, pricePlan1);
        PricePlanMatrix price2 = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.FEBRUARY, 14, 23, 59, 59), DateUtils.newDate(2011, Calendar.FEBRUARY, 14, 23, 59, 59), null, null, null);
        Assert.assertEquals(price2, pricePlan2);
        PricePlanMatrix priceEqualsToEndDate = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.MARCH, 31, 0, 0, 0), DateUtils.newDate(2011, Calendar.MARCH, 31, 0, 0, 0), null, null, null);
        Assert.assertNull(priceEqualsToEndDate);
    }
    
    @Test(groups = { "unit" })
    public void testRatePriceMinMaxAge() {
        RatingPriceStep step = new RatingPriceStep(null, null);

        PricePlanMatrix pricePlan1 = new PricePlanMatrix();
        pricePlan1.setEventCode("code");
        pricePlan1.setMaxSubscriptionAgeInMonth(2L);

        PricePlanMatrix pricePlan2 = new PricePlanMatrix();
        pricePlan2.setEventCode("code");
        pricePlan2.setMaxSubscriptionAgeInMonth(4L);

        PricePlanMatrix pricePlan3 = new PricePlanMatrix();
        pricePlan3.setEventCode("code");
        pricePlan3.setMinSubscriptionAgeInMonth(4L);
        pricePlan3.setMaxSubscriptionAgeInMonth(8L);
        
        List<PricePlanMatrix> pricePlans = Arrays.asList(pricePlan1, pricePlan2, pricePlan3);
        //FIXME
        /*
        PricePlanMatrix priceTooOld = step.ratePrice(pricePlans, "code", DateUtils.newDate(2010, Calendar.JANUARY, 1, 0, 0, 0), DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), null, null, null);
        Assert.assertNull(priceTooOld);
        PricePlanMatrix price1 = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), DateUtils.newDate(2011, Calendar.FEBRUARY, 1, 0, 0, 0), null, null, null);
        Assert.assertEquals(price1, pricePlan1);
        PricePlanMatrix price2 = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), DateUtils.newDate(2011, Calendar.MARCH, 1, 0, 0, 0), null, null, null);
        Assert.assertEquals(price2, pricePlan2);
        PricePlanMatrix price3 = step.ratePrice(pricePlans, "code", DateUtils.newDate(2011, Calendar.JANUARY, 1, 0, 0, 0), DateUtils.newDate(2011, Calendar.MAY, 1, 0, 0, 0), null, null, null);
        Assert.assertEquals(price3, pricePlan3);
*/
    }
    
    @Test(groups = { "unit" })
    public void testRatePriceCriterias() {
        RatingPriceStep step = new RatingPriceStep(null, null);

        PricePlanMatrix pricePlan1 = new PricePlanMatrix();
        pricePlan1.setEventCode("code");
        pricePlan1.setCriteria1Value("criteria1");

        PricePlanMatrix pricePlan2 = new PricePlanMatrix();
        pricePlan2.setEventCode("code");
        pricePlan2.setCriteria2Value("criteria2");

        PricePlanMatrix pricePlan3 = new PricePlanMatrix();
        pricePlan3.setEventCode("code");
        pricePlan3.setCriteria3Value("criteria3");

        PricePlanMatrix pricePlanAllCriterions = new PricePlanMatrix();
        pricePlanAllCriterions.setEventCode("code");
        pricePlanAllCriterions.setCriteria1Value("c1");
        pricePlanAllCriterions.setCriteria2Value("c2");
        pricePlanAllCriterions.setCriteria3Value("c3");
        
        PricePlanMatrix pricePlanCriteriaNull = new PricePlanMatrix();
        pricePlanCriteriaNull.setEventCode("code");
        pricePlanCriteriaNull.setCriteria1Value(null);
        pricePlanCriteriaNull.setCriteria2Value(null);
        pricePlanCriteriaNull.setCriteria3Value(null);

        

        PricePlanMatrix pricePlanCriteriaWillcard1 = new PricePlanMatrix();
        pricePlanCriteriaWillcard1.setEventCode("code");
        pricePlanCriteriaWillcard1.setCriteria1Value("*");
        pricePlanCriteriaWillcard1.setCriteria2Value("cc2");
        pricePlanCriteriaWillcard1.setCriteria3Value("cc3");

        PricePlanMatrix pricePlanCriteriaWillcard2 = new PricePlanMatrix();
        pricePlanCriteriaWillcard2.setEventCode("code");
        pricePlanCriteriaWillcard2.setCriteria1Value("cc1");
        pricePlanCriteriaWillcard2.setCriteria2Value("*");
        pricePlanCriteriaWillcard2.setCriteria3Value("cc3");

        PricePlanMatrix pricePlanCriteriaWillcard3 = new PricePlanMatrix();
        pricePlanCriteriaWillcard3.setEventCode("code");
        pricePlanCriteriaWillcard3.setCriteria1Value("cc1");
        pricePlanCriteriaWillcard3.setCriteria2Value("cc2");
        pricePlanCriteriaWillcard3.setCriteria3Value("*");

        
        List<PricePlanMatrix> pricePlans = Arrays.asList(pricePlan1, pricePlan2, pricePlan3, pricePlanAllCriterions, pricePlanCriteriaNull,pricePlanCriteriaWillcard1,pricePlanCriteriaWillcard2,pricePlanCriteriaWillcard3);
        
        PricePlanMatrix price1 = step.ratePrice(pricePlans, "code", null, null, "criteria1", null, null);
        Assert.assertEquals(price1, pricePlan1);
        
        PricePlanMatrix price2 = step.ratePrice(pricePlans, "code", null, null, null, "criteria2", null);
        Assert.assertEquals(price2, pricePlan2);
        
        PricePlanMatrix price3 = step.ratePrice(pricePlans, "code", null, null, null, null, "criteria3");
        Assert.assertEquals(price3, pricePlan3);
        
        // all criterions must match
        PricePlanMatrix priceAll = step.ratePrice(pricePlans, "code", null, null, "c1", "c2", "c3");
        Assert.assertEquals(priceAll, pricePlanAllCriterions);
        
        // must be all null criterions
        PricePlanMatrix priceNull = step.ratePrice(pricePlans, "code", null, null, null, null, null);
        Assert.assertEquals(priceNull, pricePlanCriteriaNull);

        // must be all null, because one is not should not found null criterions price plan
        PricePlanMatrix priceNullNotFound = step.ratePrice(pricePlans, "code", null, null, null, "a", null);
        Assert.assertNull(priceNullNotFound);

        // * is wilcard in c1
        PricePlanMatrix priceWil1 = step.ratePrice(pricePlans, "code", null, null, "toto", "cc2", "cc3");
        Assert.assertEquals(priceWil1, pricePlanCriteriaWillcard1);
        
        PricePlanMatrix priceWil2 = step.ratePrice(pricePlans, "code", null, null, "titi", null , "cc3");
        Assert.assertNull(priceWil2);


        // * is wilcard in c2
        PricePlanMatrix priceWil3 = step.ratePrice(pricePlans, "code", null, null, "cc1", "toto", "cc3");
        Assert.assertEquals(priceWil3, pricePlanCriteriaWillcard2);
        
        PricePlanMatrix priceWil4 = step.ratePrice(pricePlans, "code", null, null, "cc1", null , null);
        Assert.assertNull(priceWil4);
        

        // * is wilcard in c3
        PricePlanMatrix priceWil5 = step.ratePrice(pricePlans, "code", null, null, "cc1", "cc2", "toto");
        Assert.assertEquals(priceWil5, pricePlanCriteriaWillcard3);
        
        PricePlanMatrix priceWil6 = step.ratePrice(pricePlans, "code", null, null, null, "cc2" , "titi");
        Assert.assertNull(priceWil6);
    }
}
