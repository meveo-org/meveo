package org.meveo.admin.action.admin;

import org.meveo.admin.action.BaseBean;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.IEntity;
import org.meveo.service.base.BaseEntityService;
import org.meveo.service.base.local.IPersistenceService;
import org.primefaces.model.LazyDataModel;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ViewScoped
@Named
public class GenericEntityPickerBean extends BaseBean<IEntity> {

    private static final long serialVersionUID = 115130709397837651L;

    private Class<? extends IEntity> selectedEntityClass;

    @Inject
    private BaseEntityService baseEntityService;

    private static final ConcurrentHashMap<Class<? extends Annotation>, Collection<Class<?>>> classesByAnnotation = new ConcurrentHashMap<>();

    /**
     * Get a list of classes that contain the given annotation
     * 
     * @param annotation Annotation classname
     * @return A list of classes
     */
    public List<Class<?>> getEntityClasses(String annotation) {
        try {

            final Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) Class.forName(annotation);
            classesByAnnotation.computeIfAbsent(annotationClass, a -> ReflectionUtils.getClassesAnnotatedWith(a, ""));

            List<Class<?>> classes = new ArrayList<>(classesByAnnotation.get(annotationClass));
            classes.sort(Comparator.comparing(Class::getName));
            return classes;

        } catch (ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public Class<? extends IEntity> getSelectedEntityClass() {
        return selectedEntityClass;
    }

    public void setSelectedEntityClass(Class<? extends IEntity> selectedEntityClass) {
        this.selectedEntityClass = selectedEntityClass;
        setClazz((Class<IEntity>) selectedEntityClass);
        baseEntityService.setEntityClass((Class<IEntity>) selectedEntityClass);
    }

    @Override
    protected IPersistenceService<IEntity> getPersistenceService() {
        return baseEntityService;
    }

    @Override
    public LazyDataModel<IEntity> getLazyDataModel() {
        if (selectedEntityClass == null) {
            return null;
        } else {
            return super.getLazyDataModel();
        }
    }

    @Override
    protected String getDefaultSort() {
        return "code";
    }
}