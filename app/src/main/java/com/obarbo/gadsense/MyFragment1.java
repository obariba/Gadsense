package com.obarbo.gadsense;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyFragment1 extends Fragment {
    public static final String ARG_SECTION_NUMBER = "section_number";

    public MyFragment1() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.page1, container, false);
        return rootView;
    }
}
