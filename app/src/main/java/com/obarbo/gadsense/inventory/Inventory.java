package com.obarbo.gadsense.inventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by note on 2017/10/26.
 */

public class Inventory implements Serializable {

    private List<String> mAccounts = new ArrayList<String>();
    private final Map<String, List<String>> mAdClients = new HashMap<String, List<String>>();
    private final Map<String, List<String>> mAdUnits = new HashMap<String, List<String>>();
    private final Map<String, List<String>> mCustomChannels = new HashMap<String, List<String>>();

    public List<String> getmAccounts() {
        return mAccounts;
    }

    public void setAccounts(List<String> mAccounts) {
        this.mAccounts = mAccounts;
    }

    public List<String> getAdClients(String accountId) {
        return mAdClients.get(accountId);
    }

    public void setAdClients(String accountid, List<String> mAdClients) {
        this.mAdClients.put(accountid, mAdClients);
    }

    public List<String> getAdUnits(String adClientId) {
        return mAdUnits.get(adClientId);
    }
    public void setAdUnits(String adClientId, List<String> adUnits) {
        mAdUnits.put(adClientId, adUnits);
    }
    public List<String> getCustomChannels(String adClientId) {
        return mCustomChannels.get(adClientId);
    }

    public void setCusomChannels(String adClientId, List<String> customChannels) {
        mCustomChannels.put(adClientId,customChannels);
    }
}
