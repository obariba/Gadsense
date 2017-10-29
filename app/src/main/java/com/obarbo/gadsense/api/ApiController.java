package com.obarbo.gadsense.api;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.adsense.AdSense;
import com.google.api.services.adsense.AdSenseScopes;
import com.google.api.services.adsense.model.Account;
import com.google.api.services.adsense.model.AdsenseReportsGenerateResponse;
import com.google.api.services.adsense.model.ReportingMetadataEntry;
import com.obarbo.gadsense.inventory.Inventory;


import java.util.Collections;
import java.util.List;

/**
 * Created by note on 2017/10/25.
 */

public class ApiController {
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static ApiController apiController;
    private final AdSense adsenseService;
    private final GoogleAccountCredential credential;
    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    //private final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private Activity activity;
    private List<ReportingMetadataEntry> dimensions;
    private List<ReportingMetadataEntry> metrics;
    private Inventory inventory;
    private List<Account> accounts;
    private AdsenseReportsGenerateResponse reportResponse;

    public ApiController(Activity activity) {
        this.activity = activity;

        credential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Collections.singleton(AdSenseScopes.ADSENSE));
        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        adsenseService = new AdSense.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("AdSense Quickstart for Android").build();

    }
    public static ApiController getApiController(Activity activity) {
        if (apiController == null) {
            apiController = new ApiController(activity);
        }
        apiController.activity = activity;
        return apiController;
    }

    public void onReportFetched(AdsenseReportsGenerateResponse response) {
        reportResponse = response;
    }

    public void onAccountsFetched(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void onInventoryFetched(Inventory inventory) {
        this.inventory = inventory;
    }

    public void onDimensionsFetched(List<ReportingMetadataEntry> items) {
        dimensions = items;
    }

    public void onMetricsFetched(List<ReportingMetadataEntry> items) {
        metrics = items;
    }

    public void reset() {
        metrics = null;
        dimensions = null;
        inventory = null;
        reportResponse = null;
    }

    public void setAccountName(String accountName) {
        credential.setSelectedAccountName(accountName);
        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
    }

    public void loadMetadata() {
        AsyncLoadMetadata.run((AsyncTaskController) activity, this);
    }

    public void loadAccounts() {
        AsyncLoadAccounts.run((AsyncTaskController) activity, this);
    }

    public void loadInventory(String publisherAccountId) {
        AsyncLoadInventory.run((AsyncTaskController) activity, this, publisherAccountId);
    }

    public void loadReport(String accountId, String fromDate, String toDate, List<String> dimensions, List<String> metrics) {
        AsyncFetchReport.run((AsyncTaskController) activity, this, accountId, fromDate, toDate, dimensions, metrics);
    }

    public List<ReportingMetadataEntry> getDimensions() {
        return dimensions;
    }

    public List<ReportingMetadataEntry> getMetrics() {
        return metrics;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public AdsenseReportsGenerateResponse getReportResponse() {
        return reportResponse;
    }

    public AdSense getAdsenseService() {
        return adsenseService;
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }
}
