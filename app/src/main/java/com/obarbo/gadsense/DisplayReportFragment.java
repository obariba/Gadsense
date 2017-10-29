package com.obarbo.gadsense;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.services.adsense.model.AdsenseReportsGenerateResponse;

import java.util.List;

/**
 * Created by note on 2017/10/26.
 */

public class DisplayReportFragment extends Fragment {
    private UiController displayReportController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ScrollView sv = new ScrollView(getActivity());
        TableLayout tl = new TableLayout(getActivity());
        sv.addView(tl);

        if (displayReportController == null) {
            return sv;
        }
        AdsenseReportsGenerateResponse response = displayReportController.getReportResponse();

        TableLayout.LayoutParams tabletRowParams = new TableLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tabletRowParams.setMargins(10, 10, 10, 10);

        TableRow.LayoutParams tvParams = new TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(10, 10, 10, 10);

        List<Headers> headers = response.getHeaders();
        TableRow tr = new TableRow(getActivity());
        tl.addView(tr);

        for (Headers header : headers) {
            TextView tv = new TextView(getActivity());
            tv.setText(header.getName());
            tr.setLayoutParams(tabletRowParams);
            tr.addView(tv);
        }
        if (response.getRows() != null && !response.getRows().isEmpty()) {
            for (List<String> row : response.getRows()) {
                TableRow trow = new TableRow(getActivity());
                tl.addView(trow);
                for (String cell : row) {
                    TextView tv = new TextView(getActivity());
                    tv.setText(cell);
                    trow.addView(tv);
                    tv.setLayoutParams(tvParams);
                    tv.setPadding(15, 5, 15, 5);
                    tv.setBackgroundColor(Color.WHITE);
                }
            }
        }
        return sv;
    }
    public void setUIController(UiController displayReportController) {
        this.displayReportController = displayReportController;
    }
}
