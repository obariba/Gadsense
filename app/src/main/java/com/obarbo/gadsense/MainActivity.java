package com.obarbo.gadsense;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.adsense.model.AdsenseReportsGenerateResponse;
import com.google.api.services.adsense.model.ReportingMetadataEntry;
import com.google.api.services.urlshortener.UrlshortenerScopes;
import com.obarbo.gadsense.api.ApiController;
import com.obarbo.gadsense.api.AsyncTaskController;
import com.obarbo.gadsense.inventory.Inventory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OAuthHelper.OnAuthListener,
        ActionBar.OnNavigationListener,UiController, AsyncTaskController {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final String DATE_PICKER_TAG = "datePicker";
    private static FragmentManager fragmentManager;

    private String fromDate;
    private String toDate;
    private String publisherAccountId;
    private AppStatus status = AppStatus.GETTING_ACCOUNT_ID;
    private AsyncTask<Void, Void,Boolean> asyncTask;
    private ApiController apiController;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    OAuthHelper mHelper;

    @Override
    public void setActiveTask(AsyncTask<Void, Void, Boolean> task) {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        asyncTask = task;
    }

    @Override
    public void showProgressBar(AsyncTask<Void, Void, Boolean> task, boolean visible) {
        if (asyncTask == task) {
            setProgressBarIndeterminateVisibility(visible);
        }
    }

    @Override
    public void handleRecoverableError(UserRecoverableAuthIOException error) {
        startActivityForResult(error.getIntent(), REQUEST_AUTHORIZATION);
    }

    @Override
    public void onDateBtClicked(View view) {
        DatePickerFragment dpFragment = new DatePickerFragment();
        dpFragment.setCallback(this);
        Bundle args = new Bundle();
        args.putBoolean("isFrom", view.getId() == R.id.from_bt);
        dpFragment.setArguments(args);
        dpFragment.show(getSupportFragmentManager(), DATE_PICKER_TAG);
    }

    @Override
    public List<ReportingMetadataEntry> getDimensions() {
        return apiController.getDimensions();
    }

    @Override
    public List<ReportingMetadataEntry> getMetrics() {
        return apiController.getMetrics();
    }

    @Override
    public Inventory getInventory() {
        return apiController.getInventory();
    }

    @Override
    public void loadReport(List<String> dimensions, List<String> metrics) {
        if (fromDate == null) {
            Toast.makeText(this, "Please choose a start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (toDate == null) {
            Toast.makeText(this, "Please choose an end date", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((dimensions.isEmpty()) || metrics.isEmpty()) {
            Toast.makeText(this, "Please choose at least a dimension and a metric", Toast.LENGTH_SHORT).show();
            return;
        }
        apiController.loadReport(publisherAccountId, fromDate, toDate, dimensions, metrics);
    }

    @Override
    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        Log.d(TAG, String.format("onNavigationItemSelected: %d", position));
        if ((apiController.getAccounts() == null) || (publisherAccountId == null)) {
            status = AppStatus.GETTING_ACCOUNT_ID;
            refreshView();
            return true;
        }
        switch (position) {
            case 0:
                if (status != AppStatus.FETCHING_INVENTORY) {
                    status = AppStatus.FETCHING_INVENTORY;
                    refreshView();
                }
                break;
            case 2:
                if (status != AppStatus.FETCHING_METADATA) {
                    status = AppStatus.FETCHING_METADATA;
                    refreshView();
                }
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }
        return true;
    }

    @Override
    public AdsenseReportsGenerateResponse getReportResponse() {
        return apiController.getReportResponse();
    }

    @Override
    public void setToDate(int year, int month, int day, boolean isFrom) {
        if (isFrom) {
            fromDate = String .format("%d-%02d-%02d", year, month + 1, day);
        } else {
            toDate = String .format("%d-%02d-%02d", year, month + 1, day);
        }
    }

    @Override
    public void setStatus(AppStatus status) {
        this.status = status;
        refreshView();
    }

    @Override
    public boolean onCreatOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_accounts:
                status = AppStatus.GETTING_ACCOUNT_ID;
                chooseDeviceAccount();
                return true;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }
    }

    @Override
    public void onResoreinstanceState(Bundle saveInstanceState) {
        super.onRestoreInstanceState(saveInstanceState);
        if (savedInstanceState.containsKey("publisherAccountId")) {
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("publisherAccounted", publisherAccountId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else  {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    publisherAccountId = null;
                    status = AppStatus.GETTING_ACCOUNT_ID;
                    refreshView();
                } else {
                    chooseDeviceAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (requestCode == Activity.RESULT_OK && data !=null && data.getExtras() != null) {
                String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    apiController.setAccountName(accountName);
                    refreshView();
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragmentManager = getSupportFragmentManager();
        apiController = ApiController.getApiController(this);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        apiController = ApiController.getApiController(this);

        mHelper = new OAuthHelper(this, "oauth2:" + UrlshortenerScopes.URLSHORTENER, this);
        mHelper.startAuth(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        actionBar.setListNavigationCallbacks(
                new ArrayAdapter<String>(actionBar.getThemedContext(), android.R.layout.simple_list_item_1,
                        android.R.id.text1, new String[]{getString(R.string.title_section1),
                        getString(R.string.title_section2), getString(R.string.title_section3)}), this);

                AdView mAdView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
    }

    private void pickPublisherAccount() {
        final List<CharSequence> items = new ArrayList<CharSequence>();
        for (Account account : apiC.getAccounts()) {
            items.add(account.getName());
        }
        CharSequence[] itemArray = items.toArray(new CharSequence[items.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select the Adsense account");
        builder.setSingleChoiceItems(itemArray, -1, null);
        builder.setPositiveButton("Ok", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                if (selectedPosition == -1) {
                    return;
                }
                publisherAccountId = apiController.getAccounts().get(selectedPosition).getId();

                getActionBar().setSelectedNavigationItem(0);
                status = AppStatus.FETCHING_INVENTORY;
                refreshView();
            }
        });
        AlertDialog d = builder.create();
        d.show();
    }

    private void refreshView() {
        Log.d(TAG, String.format("RefredhView status: %s", status.toString()));
        switch (status) {
            case SHOWING_INVENTORY:
                createInventoryFragment();
                return;
            case PICKING_ACCOUNT:
                pickPublisherAccount();
                return;
            case SHOWING_CUSTOM_CONFIG:
                createCustomReportConfigFragment();
                return;
            case FETCHING_SIMPLE_REPORT:
                generateSimpleReport();
                return;
            case FETCHING_REPORT:
                return;
            case SHOWING_REPORT:
                createCustomReportFragment();
                return;
            case FETCHING_METADATA:
                apiController.reset();
                fromDate = null;
                toDate = null;
                apiController.loadMetadata();
                showBlankFragment();
                break;
            case GETTING_ACCOUNT_ID:
                apiController.loadAccounts();
                showBlankFragment();
                break;
            case FETCHING_INVENTORY:
                showBlankFragment();
                apiController.loadInventory(publisherAccountId);
                break;
            default:
                showBlankFragment();
                return;
        }
    }

    private void showBlankFragment() {
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putString("status", status.toString());
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    private void generateSimpleReport() {
        List<String> dimensions = new ArrayList<String>();
        List<String> metrics = new ArrayList<String>();

        dimensions.add("MONTH");
        metrics.add("EARNINGS");
        String fromDate = "today-6m";
        String toDate = "today";
        apiController.loadReport(publisherAccountId, fromDate, toDate, dimensions, metrics);
    }

    private void haveGooglePlayServices() {
        if (apiController.getCredential().getSelectAccountName() == null) {
            chooseDeviceAccount();
        } else {
            refreshView();
        }
    }

    private void chooseDeviceAccount() {
        startActivityForResult(apiController.getCredential().newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean checkGooglePlayServicesAvailable() {
        final int connectionstatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionstatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionstatusCode);
            return false;
        }
        return true;
    }

    private void createCustomReportConfigFragment() {

        CustomReportConfigFragment reportConfig = new CustomReportConfigFragment();
        reportConfig.setUIController(this);
        fragmentManager.beginTransaction().replace(R.id.container, reportConfig).commit();
    }

    private void createCustomReportFragment() {
        DisplayReportFragment fragment = new DisplayReportFragment();
        fragment.setUIController(this);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    private void createInventoryFragment() {
        DisplayInventoryFragment fragment = new DisplayInventoryFragment();
        fragment.setUIController(this);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requesyCode, int resultCode, Intent data) {
        super.onActivityResult(requesyCode, resultCode, data);
        // アカウント選択や認証画面から返ってきた時の処理をOAuthHelperで受け取る

        mHelper.onActivityResult(requesyCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_account) {
            return true;
        }

        if (id == R.id.action_help) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

 /*   @Override
    public void getAuthToken(final String authToken) {

        //* TODO ここにAPIリクエストを書く
        //* TODO ここでは例として、Google Url ShortenerでURLを短縮している

        new AsyncTask<Void, Void, Boolean>(){
            boolean mResult;

            @Override
            protected Boolean doInBackground(Void... voids) {
                mResult = true;
                try {
                    // POST URLの生成
                    Uri.Builder builder = new Uri.Builder();
                    builder.path("https://www.googleapis.com/urlshortener/v1/url");
                    // AccountManagerで取得したAuthTokenをaccess_tokenパラメータにセットする
                    builder.appendQueryParameter("access_token", authToken);
                    String postUrl = Uri.decode(builder.build().toString());

                    JSONObject jsonRequest = new JSONObject();
                    jsonRequest.put("longUrl", "http://www.google.co.jp/");
                    URL url = new URL(postUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-type", "application/json");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    PrintStream ps = new PrintStream(conn.getOutputStream());
                    ps.print(jsonRequest.toString());
                    ps.close();

                    // POSTした結果を取得
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String s;
                    String postResponse = "";
                    while ((s = reader.readLine()) != null){
                        postResponse += s + "\n";
                    }
                    reader.close();
                    Log.v(TAG, postResponse);

                    JSONObject shortenInfo = new JSONObject(postResponse);
                    // エラー判定
                    if(shortenInfo.has("error")) {
                        Log.e(TAG, postResponse);
                        mResult = false;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    mResult = false;
                }
                Log.v(TAG, "shorten finished.");

                return mResult;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result) return;
                Log.v(TAG, "再認証");
                mHelper.startAuth(true);
            }
        }.execute();
    }
*/

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            switch(i){
                case 0:
                    fragment = new MyFragment1();
                    break;
                case 1:
                    fragment = new MyFragment2();
                    break;
                case 2:
                    fragment = new MyFragment3();
                    break;
                case 3:
                    fragment = new MyFragment4();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid section number");
            }
            Bundle args = new Bundle();
            args.putInt(MyFragment1.ARG_SECTION_NUMBER, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toLowerCase();
                case 1:
                    return getString(R.string.title_section2).toLowerCase();
                case 2:
                    return getString(R.string.title_section3).toLowerCase();
                case 3:
                    return getString(R.string.title_section4).toLowerCase();
            }
            return null;
        }
    }

}
