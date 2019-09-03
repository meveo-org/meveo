package org.meveo.validation.constraint.nointersection;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.commons.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

public class NoIntersectionValidator implements ConstraintValidator<NoIntersectionBetween, Object> {

    private static Logger LOGGER = LoggerFactory.getLogger(NoIntersectionValidator.class);

    private NoIntersectionBetween annotation;

    @Override
    public void initialize(NoIntersectionBetween constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {

            // Retrieve collections to compare
            Collection firstCol = (Collection) ReflectionUtils.getPropertyValue(value, annotation.firstCollection());
            Collection secondCol = (Collection) ReflectionUtils.getPropertyValue(value, annotation.secondCollection());

            if(firstCol != null && secondCol != null){
                return !CollectionUtils.containsAny(firstCol, secondCol);
            }

        } catch (IllegalAccessException | ClassCastException e) {
            LOGGER.error("Validation skipped : ", e);
        }

        return true;
    }
}
