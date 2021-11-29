package org.meveo.util.view;

import org.primefaces.model.FilterMeta;

import java.util.Map;
import java.util.stream.Collectors;

public class FilterModel {

    private Map<String, FilterMeta> filterMeta;
    private Map<String, Object> filter;

    public FilterModel(Map<String, FilterMeta> filterMeta) {
        this.filterMeta = filterMeta;
        if (filterMeta != null) {
            this.filter = filterMeta
                    .entrySet()
                    .stream()
                    .collect(Collectors
                            .toMap(Map.Entry::getKey, entry -> (Object) entry.getValue()));
        }

    }

    public Map<String, FilterMeta> getFilterMeta() {
        return filterMeta;
    }

    public void setFilterMeta(Map<String, FilterMeta> filterMeta) {
        this.filterMeta = filterMeta;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }
}
