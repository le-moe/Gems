package com.example.fadi.networkinfoapi24.measurements;

import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;

import com.example.fadi.networkinfoapi24.NetworkOperator;
import com.example.fadi.networkinfoapi24.NetworkTechnology;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the measurement of one network cell.
 *
 * @see com.example.fadi.networkinfoapi24.tasks.CellTask
 */
public class CellMeasurement extends Measurement {

    public final NetworkOperator operator;
    public final NetworkTechnology technology;
    public final Integer cid;
    public final Integer mcc;
    public final Integer mnc;
    public final Integer arfcn;
    public final Integer dbm;
    public final Integer rsrp;
    public final Integer rsrq;
    public final Integer rssnr;
    public final Integer cqi;
    public final Integer pci;
    public final Integer psc;
    public final boolean registered;


    /**
     * @param operator   The operator used.
     * @param technology The technology used.
     * @param cid        The id of the cell.
     * @param mcc        The Mobile Country Code. 3-digits code used to identify a country.
     * @param mnc        The Mobile Network Code. 2-digits code used in combinaison with a MCC to
     *                   uniquely identify a mobile network operator.
     * @param arfcn      The Absolute Radio-Frequency Channel Number.
     * @param dbm        The strength of the signal in dBm.
     * @param rsrp       The Reference Signals Received Power. It is a measurement of the received
     *                   power level in an LTE cell network. (LTE only)
     * @param rsrq       The Reference Signals Received Quality. (LTE only)
     * @param rssnr      The Reference Signal Signal To Noise Radio. (LTE only)
     * @param cqi        The Channel Quality Indicator. (LTE only)
     * @param pci        The Physical Cell Id.
     * @param psc        The 9-bit UMTS Primary Scrambling Code described in TS 25.331. (WCDMA only)
     * @param registered Indicates if the cell is registered.
     */
    public CellMeasurement(NetworkOperator operator,
                           NetworkTechnology technology,
                           Integer cid,
                           Integer mcc,
                           Integer mnc,
                           Integer arfcn,
                           Integer dbm,
                           Integer rsrp,
                           Integer rsrq,
                           Integer rssnr,
                           Integer cqi,
                           Integer pci,
                           Integer psc,
                           boolean registered) {
        this.operator = operator;
        this.technology = technology;
        this.cid = cid;
        this.mcc = mcc;
        this.mnc = mnc;
        this.arfcn = arfcn;
        this.dbm = dbm;
        this.rsrp = rsrp;
        this.rsrq = rsrq;
        this.rssnr = rssnr;
        this.cqi = cqi;
        this.pci = pci;
        this.psc = psc;
        this.registered = registered;
    }

    /**
     * Creates a {@link CellMeasurement} from measured {@link CellInfo} data.
     *
     * @param cellInfo The data measured.
     * @param operator The network operator.
     * @return a new instance of {@link CellMeasurement} based on the given data.
     */
    public static CellMeasurement fromCellInfo(CellInfo cellInfo, NetworkOperator operator) {
        if (cellInfo instanceof CellInfoGsm)
            return CellMeasurement.fromCellInfoGsm((CellInfoGsm) cellInfo, operator);
        if (cellInfo instanceof CellInfoWcdma)
            return CellMeasurement.fromCellInfoWcdma((CellInfoWcdma) cellInfo, operator);
        if (cellInfo instanceof CellInfoLte)
            return CellMeasurement.fromCellInfoLte((CellInfoLte) cellInfo, operator);
        throw new UnsupportedOperationException("This CellInfo subtype is not supported");
    }

    /**
     * Creates a {@link CellMeasurement} from measured {@link CellInfoGsm} data.
     *
     * @param cellInfo The data measured.
     * @param operator The network operator.
     * @return a new instance of {@link CellMeasurement} based on the given data.
     */
    private static CellMeasurement fromCellInfoGsm(CellInfoGsm cellInfo, NetworkOperator operator) {
        CellIdentityGsm identity = cellInfo.getCellIdentity();
        CellSignalStrengthGsm signalStrength = cellInfo.getCellSignalStrength();
        return new CellMeasurement(
                operator,
                NetworkTechnology.GSM,
                identity.getCid(),
                identity.getMcc(),
                identity.getMnc(),
                identity.getArfcn(),
                signalStrength.getDbm(),
                null,
                null,
                null,
                null,
                null,
                null,
                cellInfo.isRegistered());
    }

    /**
     * Creates a {@link CellMeasurement} from measured {@link CellInfoWcdma} data.
     *
     * @param cellInfo The data measured.
     * @param operator The network operator.
     * @return a new instance of {@link CellMeasurement} based on the given data.
     */
    private static CellMeasurement fromCellInfoWcdma(CellInfoWcdma cellInfo, NetworkOperator operator) {
        CellIdentityWcdma identity = cellInfo.getCellIdentity();
        CellSignalStrengthWcdma signalStrength = cellInfo.getCellSignalStrength();
        return new CellMeasurement(
                operator,
                NetworkTechnology.WCDMA,
                identity.getCid(),
                identity.getMcc(),
                identity.getMnc(),
                identity.getUarfcn(),
                signalStrength.getDbm(),
                null,
                null,
                null,
                null,
                null,
                identity.getPsc(),
                cellInfo.isRegistered());
    }

    /**
     * Creates a {@link CellMeasurement} from measured {@link CellInfoLte} data.
     *
     * @param cellInfo The data measured.
     * @param operator The network operator.
     * @return a new instance of {@link CellMeasurement} based on the given data.
     */
    private static CellMeasurement fromCellInfoLte(CellInfoLte cellInfo, NetworkOperator operator) {
        CellIdentityLte identity = cellInfo.getCellIdentity();
        CellSignalStrengthLte signalStrength = cellInfo.getCellSignalStrength();
        int rsrp, rsrq, rssnr, cqi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Since API 26, these values are directly obtainable.
            rsrp = signalStrength.getRsrp();
            rsrq = signalStrength.getRsrq();
            rssnr = signalStrength.getRssnr();
            cqi = signalStrength.getCqi();
        } else {
            // Some parameters are only available by parsing the toString String in CellSignalStrengthLte class
            // These parameters can be directly accessed from CellSignalStrengthLte since API 26
            // https://developer.android.com/reference/android/telephony/CellSignalStrengthLte
            String s = signalStrength.toString();
            String s2 = "";
            String[] tokens = s.split(" ");
            for (String token : tokens) {
                s2 += token + "=";
            }
            tokens = s2.split("=");
            int index;

            index = Arrays.asList(tokens).indexOf("rsrp");
            rsrp = Integer.parseInt(tokens[index + 1]);
            index = Arrays.asList(tokens).indexOf("rsrq");
            rsrq = Integer.parseInt(tokens[index + 1]);
            index = Arrays.asList(tokens).indexOf("rssnr");
            rssnr = Integer.parseInt(tokens[index + 1]);
            index = Arrays.asList(tokens).indexOf("cqi");
            cqi = Integer.parseInt(tokens[index + 1]);
        }

        return new CellMeasurement(
                operator,
                NetworkTechnology.LTE,
                identity.getCi(),
                identity.getMcc(),
                identity.getMnc(),
                identity.getEarfcn(),
                signalStrength.getDbm(),
                rsrp,
                rsrq,
                rssnr,
                cqi,
                identity.getPci(),
                null,
                cellInfo.isRegistered());
    }

    @Override
    public String toString() {
        List<String> displayedFields = new ArrayList<>(Arrays.asList(
                "technology",
                "cid",
                "mcc",
                "mnc",
                "dbm",
                "registered",
                "arfcn")
        );

        switch (technology) {
            case LTE:
                displayedFields.add("rsrp");
                displayedFields.add("rsrq");
                break;
            case WCDMA:
                displayedFields.add("psc");
                break;

        }

        // Automatically display the name and the value of each field using reflection.

        StringBuilder sb = new StringBuilder();

        for (String fieldName : displayedFields) {
            try {
                Field field = getClass().getField(fieldName);
                Object object = field.get(this);
                sb.append(fieldName);
                sb.append(" : ");
                sb.append(object);
                sb.append("\n");

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();

    }
}
