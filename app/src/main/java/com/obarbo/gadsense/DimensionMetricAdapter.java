package com.obarbo.gadsense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.List;

/**
 * Created by note on 2017/10/26.
 */

public class DimensionMetricAdapter extends ArrayAdapter<UiReportingItem> {

    private final Context context;
    private final int resourceID;
    private final List<UiReportingItem> textItems;
    private final boolean isMetric;
    private DimensionMetricChangeListener changeListener;

    public DimensionMetricAdapter(Context context, int resourceID, List<UiReportingItem> textItems,
                                  boolean isMetric) {
        super(context, resourceID, textItems);

        this.context = context;
        this.resourceID = resourceID;
        this.textItems = textItems;
        this.isMetric = isMetric;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resourceID, parent, false);
        CheckBox cb = (CheckBox) rowView.findViewById(R.id.custom_report_checkbox);
        cb.setText(textItems.get(position).getId());
        cb.setChecked(textItems.get(position).isChecked());
        cb.setTag(position);
        cb.setEnabled(textItems.get(position).isEnabled());
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View cb) {
                checkboxChanged((Integer) ((CheckBox) cb).getTag(), isMetric, ((CheckBox) cb).isChecked());
            }
        });
        return rowView;
    }

    public void setChangeListener(DimensionMetricChangeListener listener) {
        changeListener = listener;
    }

    private void checkboxChanged(int position, boolean isMetric, boolean isChecked) {
        if (changeListener != null) {
            changeListener.onSelected(position, isMetric, isChecked);
        }
    }

}
