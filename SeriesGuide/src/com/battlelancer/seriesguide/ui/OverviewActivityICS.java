
package com.battlelancer.seriesguide.ui;

import android.annotation.TargetApi;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;

import com.battlelancer.seriesguide.items.Series;
import com.battlelancer.seriesguide.util.DBUtils;

import java.nio.charset.Charset;

public class OverviewActivityICS extends OverviewActivity implements CreateNdefMessageCallback {

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register for Android Beam
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessageCallback(this, this);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        final Series show = DBUtils.getShow(this, String.valueOf(mShowId));
        // send id, also title and overview (both can be empty)
        NdefMessage msg = new NdefMessage(new NdefRecord[] {
                createMimeRecord(
                        "application/com.battlelancer.seriesguide.beam", String.valueOf(mShowId)
                                .getBytes()),
                createMimeRecord("application/com.battlelancer.seriesguide.beam", show.getTitle()
                        .getBytes()),
                createMimeRecord("application/com.battlelancer.seriesguide.beam", show
                        .getOverview()
                        .getBytes())
        });
        return msg;
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     * 
     * @param mimeType
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
}
