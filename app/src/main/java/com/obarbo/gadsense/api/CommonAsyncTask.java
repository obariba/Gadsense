package com.obarbo.gadsense.api;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.obarbo.gadsense.AppStatus;
import com.obarbo.gadsense.ErrorUtils;

import java.io.IOException;

/**
 * Created by note on 2017/10/27.
 */

abstract class CommonAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String tag = "CommonAsyncTask";
    protected final ApiController apiController;
    final AsyncTaskController activity;

    protected CommonAsyncTask(AsyncTaskController activity, ApiController apiController) {
        this.activity = activity;
        this.apiController = apiController;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.showProgressBar(this, true);
    }

    @Override
    protected final void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        activity.showProgressBar(this, false);
        if (success) {
            activity.setStatus(getPostStatus());
        }
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        activity.showProgressBar(this, false);
    }

    @Override
    protected final Boolean doInBackground(Void... ignored) {
        try {
            doInBackground();
            return true;
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            activity.handleRecoverableError(userRecoverableException);
        } catch (IOException e) {
            ErrorUtils.logAndShow((Activity) activity, tag, e);
        }
        return false;
    }

    protected abstract void doInBackground() throws IOException;

    protected abstract AppStatus getPostStatus();
}
