/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.Rlog;

import java.util.Objects;

/**
 * GSM signal strength related information.
 */
public final class CellSignalStrengthGsm extends CellSignalStrength implements Parcelable {

    private static final String LOG_TAG = "CellSignalStrengthGsm";
    private static final boolean DBG = false;

    private static final int GSM_SIGNAL_STRENGTH_GREAT = 12;
    private static final int GSM_SIGNAL_STRENGTH_GOOD = 8;
    private static final int GSM_SIGNAL_STRENGTH_MODERATE = 5;

    @UnsupportedAppUsage
    private int mSignalStrength; // in ASU; Valid values are (0-31, 99) as defined in TS 27.007 8.5
    @UnsupportedAppUsage
    private int mBitErrorRate;   // bit error rate (0-7, 99) as defined in TS 27.007 8.5
    @UnsupportedAppUsage
    private int mTimingAdvance; // range from 0-219 or Integer.MAX_VALUE if unknown

    /** @hide */
    @UnsupportedAppUsage
    public CellSignalStrengthGsm() {
        setDefaultValues();
    }

    /** @hide */
    public CellSignalStrengthGsm(int ss, int ber) {
        this(ss, ber, Integer.MAX_VALUE);
    }

    /** @hide */
    public CellSignalStrengthGsm(int ss, int ber, int ta) {
        mSignalStrength = ss;
        mBitErrorRate = ber;
        mTimingAdvance = ta;
    }

    /** @hide */
    public CellSignalStrengthGsm(CellSignalStrengthGsm s) {
        copyFrom(s);
    }

    /** @hide */
    protected void copyFrom(CellSignalStrengthGsm s) {
        mSignalStrength = s.mSignalStrength;
        mBitErrorRate = s.mBitErrorRate;
        mTimingAdvance = s.mTimingAdvance;
    }

    /** @hide */
    @Override
    public CellSignalStrengthGsm copy() {
        return new CellSignalStrengthGsm(this);
    }

    /** @hide */
    @Override
    public void setDefaultValues() {
        mSignalStrength = Integer.MAX_VALUE;
        mBitErrorRate = Integer.MAX_VALUE;
        mTimingAdvance = Integer.MAX_VALUE;
    }

    /**
     * Retrieve an abstract level value for the overall signal strength.
     *
     * @return a single integer from 0 to 4 representing the general signal quality.
     *     0 represents very poor signal strength while 4 represents a very strong signal strength.
     */
    @Override
    public int getLevel() {
        int level;

        // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
        // asu = 0 (-113dB or less) is very weak
        // signal, its better to show 0 bars to the user in such cases.
        // asu = 99 is a special case, where the signal strength is unknown.
        int asu = mSignalStrength;
        if (asu <= 2 || asu == 99) level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        else if (asu >= GSM_SIGNAL_STRENGTH_GREAT) level = SIGNAL_STRENGTH_GREAT;
        else if (asu >= GSM_SIGNAL_STRENGTH_GOOD)  level = SIGNAL_STRENGTH_GOOD;
        else if (asu >= GSM_SIGNAL_STRENGTH_MODERATE)  level = SIGNAL_STRENGTH_MODERATE;
        else level = SIGNAL_STRENGTH_POOR;
        if (DBG) log("getLevel=" + level);
        return level;
    }

    /**
     * Get the GSM timing advance between 0..219 symbols (normally 0..63).
     * Integer.MAX_VALUE is reported when there is no RR connection.
     * Refer to 3GPP 45.010 Sec 5.8
     * @return the current GSM timing advance, if available.
     */
    public int getTimingAdvance() {
        return mTimingAdvance;
    }

    /**
     * Get the signal strength as dBm
     */
    @Override
    public int getDbm() {
        int dBm;

        int level = mSignalStrength;
        int asu = (level == 99 ? Integer.MAX_VALUE : level);
        if (asu != Integer.MAX_VALUE) {
            dBm = -113 + (2 * asu);
        } else {
            dBm = Integer.MAX_VALUE;
        }
        if (DBG) log("getDbm=" + dBm);
        return dBm;
    }

    /**
     * Get the signal level as an asu value between 0..31, 99 is unknown
     * Asu is calculated based on 3GPP RSRP. Refer to 3GPP 27.007 (Ver 10.3.0) Sec 8.69
     */
    @Override
    public int getAsuLevel() {
        // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
        // asu = 0 (-113dB or less) is very weak
        // signal, its better to show 0 bars to the user in such cases.
        // asu = 99 is a special case, where the signal strength is unknown.
        int level = mSignalStrength;
        if (DBG) log("getAsuLevel=" + level);
        return level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mSignalStrength, mBitErrorRate, mTimingAdvance);
    }

    @Override
    public boolean equals (Object o) {
        CellSignalStrengthGsm s;

        try {
            s = (CellSignalStrengthGsm) o;
        } catch (ClassCastException ex) {
            return false;
        }

        if (o == null) {
            return false;
        }

        return mSignalStrength == s.mSignalStrength && mBitErrorRate == s.mBitErrorRate &&
                        s.mTimingAdvance == mTimingAdvance;
    }

    /**
     * @return string representation.
     */
    @Override
    public String toString() {
        return "CellSignalStrengthGsm:"
                + " ss=" + mSignalStrength
                + " ber=" + mBitErrorRate
                + " mTa=" + mTimingAdvance;
    }

    /** Implement the Parcelable interface */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (DBG) log("writeToParcel(Parcel, int): " + toString());
        dest.writeInt(mSignalStrength);
        dest.writeInt(mBitErrorRate);
        dest.writeInt(mTimingAdvance);
    }

    /**
     * Construct a SignalStrength object from the given parcel
     * where the token is already been processed.
     */
    private CellSignalStrengthGsm(Parcel in) {
        mSignalStrength = in.readInt();
        mBitErrorRate = in.readInt();
        mTimingAdvance = in.readInt();
        if (DBG) log("CellSignalStrengthGsm(Parcel): " + toString());
    }

    /** Implement the Parcelable interface */
    @Override
    public int describeContents() {
        return 0;
    }

    /** Implement the Parcelable interface */
    @SuppressWarnings("hiding")
    public static final Parcelable.Creator<CellSignalStrengthGsm> CREATOR =
            new Parcelable.Creator<CellSignalStrengthGsm>() {
        @Override
        public CellSignalStrengthGsm createFromParcel(Parcel in) {
            return new CellSignalStrengthGsm(in);
        }

        @Override
        public CellSignalStrengthGsm[] newArray(int size) {
            return new CellSignalStrengthGsm[size];
        }
    };

    /**
     * log
     */
    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
