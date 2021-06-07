package com.example.fadi.networkinfoapi24;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.CellNetworkScanResult;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.OperatorInfo;
import com.hypertrack.hyperlog.HyperLog;

import java.util.List;

/**
 * Represents a configuration of the cellular network,
 * i.e. technology (GSM, WCDMA or LTE) and operator.
 */
public class NetworkConfig {
    private static final String TAG = "NetworkConfig";
    private static final int RETRY_COUNT = 5;

    private final TelephonyManager telephonyManager;
    private final int subId = SubscriptionManager.getDefaultSubscriptionId();

    private NetworkOperator operator;
    private NetworkTechnology technology;

    public NetworkConfig(Context context) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public NetworkOperator getOperator() {
        return operator;
    }

    public void setOperator(NetworkOperator operator) {
        if (operator != this.operator) {
            this.operator = operator;
            changeOperator();
        }
    }

    public NetworkTechnology getTechnology() {
        return technology;
    }

    public void setTechnology(NetworkTechnology technology) {
        if (technology != this.technology) {
            this.technology = technology;
            changeTechnology();
        }
    }


    private void changeTechnology() {
        telephonyManager.setPreferredNetworkType(subId, technology.getRILConstant());
    }

    private void changeOperator() {
        String operatorName = telephonyManager.getNetworkOperatorName(subId);
        if (operator.getName().equals(operatorName)) {
            HyperLog.i(TAG, "Already connected to the right operator: " + operatorName);
            return;
        }


        CellNetworkScanResult results = telephonyManager.getCellNetworkScanResults(SubscriptionManager.getDefaultSubscriptionId());
        List<OperatorInfo> operators = results.getOperators();

        if (operators == null || operators.isEmpty()) {
            HyperLog.e(TAG, "Cannot change operators, no operators available");
            return;
        }

        for (OperatorInfo operatorInfo : operators) {
            if (operator.correspondsTo(operatorInfo)) {
                switch (operatorInfo.getState()) {
                    case CURRENT:
                        // Already connected to this operator
                        return;
                    case AVAILABLE:
                        // Change operator
                        setNetworkSelectionModeManual(subId, operatorInfo);
                        return;
                    default:
                        // TODO: Handle the case of no operator available
                        HyperLog.e(TAG, "Desired operator not found");
                        return;
                }
            }
        }

        // TODO: Handle the case of no operator available
        HyperLog.e(TAG, "Desired operator not found");


    }

    /**
     * change the operator using setNetworkSelectionModeManual in ITelephony. Some of
     * the code is a direct copy from setNetworkSelectionModeManual in TelephonyManager, due to
     * a problem using this method directly.
     */
    private void setNetworkSelectionModeManual(int subId, OperatorInfo operatorInfo) {
        try {
            ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            if (telephony != null) {

                for (int i = 0; i < RETRY_COUNT; i++) {
                    boolean success = telephony.setNetworkSelectionModeManual(subId, operatorInfo, false);
                    if (success) {
                        HyperLog.i(TAG, "Change operator to: " + operatorInfo.getOperatorAlphaLong());
                        return;
                    }
                    HyperLog.i(TAG, "Failed to select network. Retrying...");
                }
            }
        } catch (RemoteException ex) {
            HyperLog.e(TAG, "setNetworkSelectionModeManual RemoteException", ex);
        } catch (NullPointerException ex) {
            HyperLog.e(TAG, "setNetworkSelectionModeManual NPE", ex);
        }
    }
}
