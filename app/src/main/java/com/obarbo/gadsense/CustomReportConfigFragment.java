package com.obarbo.gadsense;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;

import com.google.api.services.adsense.model.ReportingMetadataEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by note on 2017/10/26.
 */

public class CustomReportConfigFragment extends Fragment implements DimensionMetricChangeListener,
        OnClickListener {

    private DimensionMetricAdapter dimensionAdapter, metricAdapter;
    private ArrayList<UiReportingItem> dimensionsUI;
    private ArrayList<UiReportingItem> metricsUI;
    private UiController customReportConfigReadyController;
    private DimensionsMetricsCompatChecker checker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.custom_report_comfig, container, false);

        TabHost host = (TabHost) rootView.findViewById(R.id.tabhost);
        host,setup();

        TapSpec spec = host.newTabSpec("Dimensions");
        spec.setContent(R.id.dimensions_tab);
        spec.setIndicator("Dimensions");
        host.addTab(spec);

        spec = host.newTabSpec("Metrics");
        spec.setContent(R.id.matrics_tab);
        spec.setIndicator("Metrics");
        host.addTab(spec);

        spec = host.newTabSpec("Dates");
        spec.setContent(R.id.dates_tab);
        spec.setIndicator("Dates");
        host.addTab(spec);

        if (customReportConfigReadyController == null) {
            return rootView;
        }
        List<ReportingMetadataEntry> dimensions = customReportConfigReadyController.getDimensions();
        List<ReportingMetadataEntry> metrics = customReportConfigReadyController.getMetrics();

        checker = new DimensionMetricsCompatChecker(metrics, dimensions);

        ArrayList<String> dimensionsIds = new ArrayList<String>();

        for (ReportingMetadataEntry dimension : dimensions) {
            dimensionsIds.add(dimension.getId());
        }

        ArrayList<String> metricIds = new ArrayList<String>();

        for (ReportingMetadataEntry metric : metrics) {
            metricIds.add(metric.getId());
        }

        dimensionsUI = new ArrayList<UiReportingItem>();
        for (String dimension : dimensionIds) {
            dimensionsUI.add(new UiReportingItem(dimension, false, true));
        }

        dimensionAdapter = new DimensionMetricAdapter(getActivity(), R.layout.custom_report_list_item,
                dimensionsUI, false);
        ListView lvdimension = (ListView) rootView.findViewById(R.id.dimensions_list);
        lvdimension.setAdapter(dimensionAdapter);

        metricsUI = new ArrayList<UiReportingItem>();
        for (String metric : metricIds) {
            metricsUI.add(new UiReportingItem(metric, false, true));
        }

        metricAdapter = new DimensionMetricAdapter(
                getActivity(), R.layout.custom_report_list_item, metricsUI, true);
        ListView lvmetric = (ListView) rootView.findViewById(R.id.metrics_list);
        lvmetric.setAdapter(metricAdapter);

        dimensionAdapter.setChangeListener(this);
        metricAdapter.setChangeListener(this);

        rootView.findViewById(R.id.generate_bt).setOnClickListener(this);
        rootView.findViewById(R.id.from_bt).setOnClickListener(this);
        rootView.findViewById(R.id.to_bt).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.generate_bt:
                if (customReportConfigReadyController != null) {
                    customReportConfigReadyController.loadReport(
                            computeCheckItems(dimensionsUI), computeCheckdItems(metricsUI));
                }
                break;
            case R.id.to_bt:
                if (customReportConfigReadyController != null) {
                    customReportConfigReadyController.onDateBtClicked(v);
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onSelected(int position, boolean isMetric, boolean isChecked) {

        if (isMetric) {
            metricsUI.get(position).serChecked(isChecked);

            List<String> checkedMetrics = computeCheckedItems(metricsUI);

            for (UiReportingItem dimension : dimensionsUI) {
                boolean isCompatible = checker.isDimensionCompatibleWithMetrics(
                        dimension.getId(), checkedMetrics);
                dimension.setEnabled(isCompatible);
            }
            dimensionAdapter.notifyDataSetChanged();
            metricAdapter.notifyDataSetChanged();
        } else {

            dimensionsUI.get(position).setChecked(isChecked);
            List<String> checkedDimensions = computeCheckedItems(dimensionsUI);

            for (UiReportingItem dimension : dimensionsUI) {
                boolean isCompatible = checker.isDimensionCompatibleWithDimensions(
                        dimension.getId(), checkedDimensions);
                dimension.setEnabled(isCompatible);
            }

            for (UiRepotingItem metric : metricsUI) {
                boolean isCompatible = checker.isMetricCompatibleWithDimensions(
                        metric.getId(), checkedDimensions);
                metric.setEnabled(isCompatible);
            }

            dimensionAdapter.notifyDataSetChanged();
            metricAdapter.notifyDayaSetChanged();
        }
    }

    public void setUIController(UiController customReportConfigReadyController) {
        this.customReportConfigReadyController = customReportConfigReadyController;
    }

    private static List<String> computeCheckedItems(List<UiReportingItem> collection) {
        List<String> checkedItems = new ArrayList<String>();
        for (UiReportingItem item : collection) {
            if (item.isChecked()) {
                checkedItems.add(item.getId());
            }
        }
        return checkedItems;
    }
}
