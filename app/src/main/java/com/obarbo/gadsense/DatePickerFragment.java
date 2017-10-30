package com.obarbo.gadsense;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.DatePicker;

/**
 * Created by note on 2017/10/25.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    private UiController callback;
    private boolean isFrom;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        isFrom = args.getBoolean("isFrom");

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (callback != null) {
            callback.setDate(year, month, day, isFrom);
        }
    }

    public void setCallback(UiController callback) {
        this.callback = callback;
    }

    public void show(FragmentManager supportFragmentManager, String datePickerTag) {
    }
}
