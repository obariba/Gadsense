package com.obarbo.gadsense;

/**
 * Created by note on 2017/10/26.
 */

public class UiReportingItem {
    private String id;
    private boolean isChecked;
    private boolean isEnabled;

    public UiReportingItem(String id, boolean isChecked, boolean isEnabled) {
        this.id = id;
        this.isChecked = isChecked;
        this.isEnabled = isEnabled;
    }

    public String getId() {
        return id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isChecked() {
     return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
