package com.example.fadi.networkinfoapi24;

import com.android.internal.telephony.RILConstants;

/**
 * Represent a cellular technology, used to force the phone to switch to that technology.
 */
public enum NetworkTechnology {
    /**
     * Choose the best operator available.
     * <p>
     * Does not correspond to {@link RILConstants#NETWORK_MODE_GLOBAL} because it's only 2G/3G.
     * Corresponds to {@link RILConstants#NETWORK_MODE_LTE_GSM_WCDMA}
     */
    GLOBAL {
        @Override
        public int getRILConstant() {
            return RILConstants.NETWORK_MODE_LTE_GSM_WCDMA;
        }
    },

    /**
     * GSM or 2G
     */
    GSM {
        @Override
        public int getRILConstant() {
            return RILConstants.NETWORK_MODE_GSM_ONLY;
        }
    },

    /**
     * WCDMA or 3G
     */
    WCDMA {
        @Override
        public int getRILConstant() {
            return RILConstants.NETWORK_MODE_WCDMA_ONLY;
        }
    },

    /**
     * LTE or 4G
     */
    LTE {
        @Override
        public int getRILConstant() {
            return RILConstants.NETWORK_MODE_LTE_ONLY;
        }
    };

    public abstract int getRILConstant();

    @Override
    public String toString() {
        return name();
    }
}
