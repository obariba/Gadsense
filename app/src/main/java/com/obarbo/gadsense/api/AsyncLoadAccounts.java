package com.obarbo.gadsense.api;

import com.google.api.services.adsense.model.Account;
import com.obarbo.gadsense.AppStatus;

import java.io.IOException;
import java.util.List;

/**
 * Created by note on 2017/10/27.
 */

public class AsyncLoadAccounts extends CommonAsyncTask {

    private AsyncLoadAccounts(AsyncTaskController activity, ApiController apiController) {
        super(activity, apiController);
    }

    @Override
    protected void doInBackground() throws IOException {
        List<Account> items = apiController.getAdsenseService().accounts().list().execute().getItems();
        apiController.onAccountsFetched(items);
    }

    @Override
    protected AppStatus getPostStatus() {
        return AppStatus.PICKING_ACCOUNT;
    }

    public static void run(AsyncTaskController activity, ApiController apiController) {
        AsyncLoadAccounts task = new AsyncLoadAccounts(activity, apiController);
        activity.setActiveTask(task);
        task.execute();
    }
}
