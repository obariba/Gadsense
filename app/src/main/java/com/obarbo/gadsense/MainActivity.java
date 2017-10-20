package com.obarbo.gadsense;

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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.api.services.urlshortener.UrlshortenerScopes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OAuthHelper.OnAuthListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    OAuthHelper mHelper;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


                AdView mAdView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);


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

    @Override
    public void getAuthToken(final String authToken) {
        /*
        * TODO ここにAPIリクエストを書く
        * TODO ここでは例として、Google Url ShortenerでURLを短縮している
        */
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
