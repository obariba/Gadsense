package com.obarbo.gadsense.api;

import com.obarbo.gadsense.AppStatus;

import java.io.IOException;
import java.util.List;

/**
 * Created by note on 2017/10/27.
 */

public class AsyncFetchReport extends CommonAsyncTask {

    private final String fromDate;
    private final String toDate;
    private final List<String> dimensions;
    private final List<String> metrics;
    private final String accountId;

    private AsyncFetchReport(AsyncTaskController activity, ApiController apiController,
                             String accountId, String fromDate, String toDate, List<String> dimensions,
                             List<String> metrics) {
        super(activity, apiController);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.dimensions = dimensions;
        this.metrics = metrics;
        this.accountId = accountId;
    }

    @Override
    protected void doInBackground() throws IOException {
        apiController.onReportFetched(
                apiController.getAdsenseService().accounts().reports()
                .generete(accountId, fromDate, toDate)
                .setDimension(dimensions)
                .setMetric(metrics)
                .execute());
    }

    @Override
    protected AppStatus getPostStatus() {
        return AppStatus.SHOWING_REPORT;
    }

    public static void run(AsyncTaskController activity, ApiController apicontroller,
                           String accountId, String fromDate, String toDate, List<String> dimensions,
                           List<String> metrics) {
        AsyncFetchReport task = new AsyncFetchReport(activity, apicontroller, accountId, fromDate,
                toDate, dimensions, metrics);
        activity.setActiveTask(task);
        task.execute();
    }

}
