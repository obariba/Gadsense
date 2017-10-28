package com.obarbo.gadsense.api;

import com.obarbo.gadsense.AppStatus;

import java.io.IOException;

/**
 * Created by note on 2017/10/27.
 */

public class AsyncLoadMetadata extends CommonAsyncTask {

    private AsyncLoadMetadata(AsyncTaskController activity, ApiController apiController) {
        super(activity, apiController);
    }

    @Override
    protected void doInBackground() throws IOException {
        apiController.onDimensionsFetched(
                apiController.getAdsenseService().metadata().dimensions().list().execute().getItems());
        apiController.onMetricsFetched(
                apiController.getAdsenseService().metadata().metrics().list().execute().getItems());
    }

    @Override
    protected AppStatus getPostStatus() {
        return AppStatus.SHOWING_CUSTOM_CONFIG;
    }

    public static void run(AsyncTaskController activity, ApiController apiController) {
        AsyncLoadMetadata task = new AsyncLoadMetadata(activity, apiController);
        activity.setActiveTask(task);
        task.execute();
    }
}
