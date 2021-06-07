package com.example.fadi.networkinfoapi24;

import android.app.Notification;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SubscriptionManager;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.RILConstants;
import com.example.fadi.networkinfoapi24.Activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class Utilities {

    public static final String GLOBAL = "Global";
    public static final String GSM_ONLY = "GSM only";
    public static final String GSM_UMTS = "GSM + UMTS";
    public static final String LTE_GSM_WCDMA = "LTE + GSM + WCDMA";
    public static final String LTE_ONLY = "LTE only";
    public static final String WCDMA_ONLY = "WCDMA only";
    public static final String WCDMA_PREF = "WCDMA pref";
    public static final String ORANGE = "Orange";
    public static final String MOBISTAR = "mobistar";
    public static final String PROXIMUS = "Proximus";
    public static final String BASE = "Base";
    public static final String NULL = "0";
    public static final String[] NETWORKS_LIST = {ORANGE, PROXIMUS, BASE, "My new network", NULL, "Something else"};

    public static void startNotification(Context context, String title, String text) {
        Notification notification = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_name)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManagerCompat.from(context).notify(new Random().nextInt(), notification);
    }

    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * Convert the String s indicating the network technology type to its RILConstant equivalent
     *
     * @param s
     * @return
     */
    public static int stringToInt(String s) {
        switch (s) {
            case WCDMA_PREF:
                return RILConstants.NETWORK_MODE_WCDMA_PREF;
            case GSM_ONLY:
                return RILConstants.NETWORK_MODE_GSM_ONLY;
            case WCDMA_ONLY:
                return RILConstants.NETWORK_MODE_WCDMA_ONLY;
            case GSM_UMTS:
                return RILConstants.NETWORK_MODE_GSM_UMTS;
            case GLOBAL:
                return RILConstants.NETWORK_MODE_GLOBAL;
            case LTE_GSM_WCDMA:
                return RILConstants.NETWORK_MODE_LTE_GSM_WCDMA;
            case LTE_ONLY:
                return RILConstants.NETWORK_MODE_LTE_ONLY;
            default:
                return RILConstants.NETWORK_MODE_LTE_GSM_WCDMA;
        }
    }

    /**
     * Check if mobile data is enabled.
     * The code is a copy from getMobileDataEnabled method in ConnectivityManager
     *
     * @return
     */
    public static boolean isMobileDataEnabled() {
        IBinder b = ServiceManager.getService(Context.TELEPHONY_SERVICE);
        if (b != null) {
            try {
                ITelephony it = ITelephony.Stub.asInterface(b);
                int subId = SubscriptionManager.getDefaultSubscriptionId();
                android.util.Log.d("ConnectivityManager", "getMobileDataEnabled()+ subId=" + subId);
                boolean retVal = it.getDataEnabled(subId);
                android.util.Log.d("ConnectivityManager", "getMobileDataEnabled()- subId=" + subId
                        + " retVal=" + retVal);
                return retVal;
            } catch (RemoteException e) {
            }
        }
        android.util.Log.d("ConnectivityManager", "getMobileDataEnabled()- remote exception retVal=false");
        return false;
    }

    /**
     * Enable/ disable mobile data using setDataEnabled method in TelephonyManager
     *
     * @param value
     */
    public static void setDataEnabled(boolean value) {
        IBinder b = ServiceManager.getService(Context.TELEPHONY_SERVICE);
        if (b != null) {
            try {
                ITelephony it = ITelephony.Stub.asInterface(b);
                int subId = SubscriptionManager.getDefaultSubscriptionId();
                it.setDataEnabled(subId, value);
            } catch (RemoteException e) {
            }
        }
    }

    /**
     * Convert the RILConstant representing the network technology type to a String
     *
     * @param i
     * @return
     */
    public static String intToString(int i) {
        switch (i) {
            case RILConstants.NETWORK_MODE_WCDMA_PREF:
                return WCDMA_PREF;
            case RILConstants.NETWORK_MODE_GSM_ONLY:
                return GSM_ONLY;
            case RILConstants.NETWORK_MODE_WCDMA_ONLY:
                return WCDMA_ONLY;
            case RILConstants.NETWORK_MODE_GSM_UMTS:
                return GSM_UMTS;
            case RILConstants.NETWORK_MODE_GLOBAL:
                return GLOBAL;
            case RILConstants.NETWORK_MODE_LTE_GSM_WCDMA:
                return LTE_GSM_WCDMA;
            case RILConstants.NETWORK_MODE_LTE_ONLY:
                return LTE_ONLY;
            default:
                return LTE_GSM_WCDMA;
        }
    }

}
