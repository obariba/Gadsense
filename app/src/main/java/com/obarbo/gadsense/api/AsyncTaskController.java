package com.obarbo.gadsense.api;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.obarbo.gadsense.AppStatus;

/**
 * Created by note on 2017/10/25.
 */

public interface AsyncTaskController {
    void showProgressBar(AsyncTask<Void, Void, Boolean> task, boolean visible);
    void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode);
    void handleRecoverableError(UserRecoverableAuthIOException userRecoverableException);
    void setStatus(AppStatus postStatus);
    void setActiveTask(AsyncTask<Void, Void, Boolean> task);

}
