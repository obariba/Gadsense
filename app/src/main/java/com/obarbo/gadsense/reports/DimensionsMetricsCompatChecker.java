package com.obarbo.gadsense.reports;

import android.support.annotation.Nullable;

import com.google.api.services.adsense.model.ReportingMetadataEntry;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by note on 2017/10/29.
 */

public class DimensionsMetricsCompatChecker implements Serializable {

    private final List<ReportingMetadataEntry> mAllMetrics;
    private final List<ReportingMetadataEntry> mAllDimensions;

    public DimensionsMetricsCompatChecker(List<ReportingMetadataEntry> metrics,
                                          List<ReportingMetadataEntry> dimensions) {
        mAllMetrics = metrics;
        mAllDimensions = dimensions;
    }

    public static boolean isDimensionsCompatibleWithMetrics(
            ReportingMetadataEntry dimension, List<String> metrics) {
        return dimension.getCompatibleMetrics().containsAll(metrics);
    }

    public static boolean isMetricCompatibleDimensions(ReportingMetadataEntry metric,
                                                       List<String> dimensions) {
        return metric.getCompatibleDimensions().containsAll(dimensions);
    }

    public boolean areMetricsAndDimensionsCompatible(List<String> metrics,
                                                     List<String> dimensions) {
        return (areDimensionsCompatible(dimensions) && areMetricsCompatible(metrics));
    }

    public boolean isMetricCompatibleWithDimensions(String metric,
                                                    List<String> dimensions) {
        ReportingMetadataEntry entryMetric = getMetadataEntryMetric(metric);
        if (entryMetric == null) {
            return false;
        }
        return isMetricCompatibleWithDimensions(entryMetric, dimensions);
    }

    public boolean areMetricsCompatible(List<String> metrics) {
        for (String metricName : metrics) {
            ReportingMetadataEntry metric = getMetadataEntryMetric(metricName);
            if (!metric.getCompatibleMetrics().containsAll(metrics)) {
                return false;
            }
        }
        return true;
    }

    public boolean areDimensionsCompatible(List<String> dimensions) {
        return areDimensionsCompatible(null, dimensions);
    }

    public boolean isDimensionsCompatibleWithDimensions(
            ReportingMetadataEntry dimension, List<String> dimensions) {
        return areDimensionsCompatible(dimension, dimensions);
    }

    public boolean isDimensionCompatibleWithDimensions(String dimensionId, List<String> dimensions) {
        ReportingMetadataEntry entryDimension = getMetadataEntryDimension(dimensionId);
        if (entryDimension == null) {
            return false;
        }
        return isDimensionsCompatibleWithDimensions(entryDimension, dimensions);
    }

    public boolean isDimensionCompatibleWithMetrics(String dimensionId, List<String> metrics) {
        return isDimensionsCompatibleWithMetrics(getMetadataEntryDimension(dimensionId), metrics);
    }

    public ReportingMetadataEntry getMetadataEntryDimension(String dimensionId) {
        for (ReportingMetadataEntry dimension : mAllDimensions) {
            if (dimension.getId().equals(dimensionId)) {
                return dimension;
            }
        }
        return null;
    }

    public ReportingMetadataEntry getMetadataEntryMetric(String metricId) {
        for (ReportingMetadataEntry metric : mAllMetrics) {
            if (metric.getId().equals(metricId)) {
                return metric;
            }
        }
        return null;
    }

    private static void keepIntersection(List<String> originalList, List<String> otherList) {
        Iterator<String> iter = originalList.iterator();
        while (iter.hasNext()) {
            if (!otherList.contains(iter.next())) {
                iter.remove();
            }
        }
    }

    private boolean areDimensionsCompatible(@Nullable ReportingMetadataEntry dimension,
                                            List<String> dimensions) {
        List<String> compatibilityGroups;

        if (dimension == null) {
            compatibilityGroups = getMetadataEntryDimension(dimensions.get(0)).getCompatibleDimensions();
        } else {
            compatibilityGroups = dimension.getCompatibleDimensions();
        }

        for (String dimensionName : dimensions) {
            ReportingMetadataEntry otherDimension = getMetadataEntryDimension(dimensionName);
            keepIntersection(compatibilityGroups, otherDimension.getCompatibleDimensions());
        }
        return !compatibilityGroups.isEmpty();
    }
}
