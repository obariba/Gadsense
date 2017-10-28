package com.obarbo.gadsense;

import android.view.View;

import com.google.api.services.adsense.model.AdsenseReportsGenerateResponse;
import com.google.api.services.adsense.model.ReportingMetadataEntry;
import com.obarbo.gadsense.inventory.Inventory;

import java.util.List;

/**
 * Created by note on 2017/10/25.
 */

public interface UiController {
    public void loadReport(List<String> dimensions, List<String> metrics);
    public void setDate(int year, int month, int day, boolean isFrom);
    public AdsenseReportsGenerateResponse getReportResponse();
    public void onDateBtClicked(View button);
    public List<ReportingMetadataEntry> getDimensions();
    public List<ReportingMetadataEntry> getMetrics();
    public Inventory getInventory();

}
