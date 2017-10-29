package com.obarbo.gadsense.api;

import android.util.Log;

import com.google.api.services.adsense.model.Account;
import com.google.api.services.adsense.model.AdClient;
import com.google.api.services.adsense.model.AdUnit;
import com.google.api.services.adsense.model.CustomChannel;
import com.obarbo.gadsense.AppStatus;
import com.obarbo.gadsense.inventory.Inventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by note on 2017/10/27.
 */

public class AsyncLoadInventory extends CommonAsyncTask {

    private final String rootAccountId;
    private Inventory inventory;

    private AsyncLoadInventory(AsyncTaskController activity, ApiController apiController,
                               String accountId) {
        super(activity, apiController);
        rootAccountId = accountId;
    }

    @Override
    protected void doInBackground() throws IOException {
        inventory = new Inventory();
        List<String> accountIds = new ArrayList<String>();
        accountIds.add(rootAccountId);
        processAccount(rootAccountId);

        //Sub accounts:
        List<Account> subAccounts = apiController.getAdsenseService().accounts()
                .get(rootAccountId).setTree(true).execute().getSubAccounts();
        if (subAccounts != null) {
            for (Account account : subAccounts) {
                if (!accountIds.contains(account.getId())) {
                    accountIds.add(account.getId());
                    processAccount(account.getId());
                }
            }
        }
        inventory.setAccounts(accountIds);
        apiController.onInventoryFetched(inventory);
    }

    @Override
    protected AppStatus getPostStatus() {
        return AppStatus.SHOWING_INVENTORY;
    }

    public static void run(AsyncTaskController activity, ApiController apiController,
                           String accountId) {
        AsyncLoadInventory task = new AsyncLoadInventory(activity, apiController, accountId);
        activity.setActiveTask(task);
        task.execute();
    }

    private void processAccount(String accountId) throws IOException {
        if (isCancelled()) {
            return;
        }

        List<AdClient> adClients = apiController.getAdsenseService().accounts().adclients().list(accountId).setMaxResults(10).execute().getItems();

        List<String> adClientIds = new ArrayList<String>();
        for (AdClient adClient : adClients) {
            adClientIds.add(adClient.getId());
            Log.d("LoadInventory", "AdClient:" + adClient.getId());
            List<AdUnit> adUnits = apiController.getAdsenseService().accounts().adunits()
                    .list(accountId, adClient.getId()).setMaxResults(10).execute().getItems();
            List<String> adUnitNames = new ArrayList<String>();
            if (adUnits != null) {
            for (AdUnit adUnit : adUnits) {
                adUnitNames.add(adUnit.getName());
            }
        }
        inventory.setAdUnits(adClient.getId(), adUnitNames);

        List<CustomChannel> customChannels = apiController.getAdsenseService().accounts()
                .customchannels().list(accountId, adClient.getId()).setMaxResults(10).execute()
                .getItems();
        List<String> customChannelNames = new ArrayList<String>();
        if (customChannels != null) {
            for (CustomChannel customChannel : customChannels) {
                customChannelNames.add(customChannel.getName());
            }
        }
        inventory.setCusomChannels(adClient.getId(), customChannelNames);
    }
    inventory.setAdClients(accountId, adClientIds);
    }
}
