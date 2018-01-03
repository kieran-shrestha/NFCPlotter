package com.shortedwire.kiran.nfcplotter;

import android.nfc.NdefMessage;
import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by kiran on 1/3/2018.
 */

public class BuildTagViews {
    public BuildTagViews() {
    }

    public String nDefPayload(NdefMessage[] msgs){
        if (msgs == null || msgs.length == 0)
            return String.valueOf('0');

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }
        return text;
    }

//    public int getPayloadLength(NdefMessage[] msgs){
//        byte[] payload = msgs[0].getRecords()[0].getPayload();
//        return payload.length;
//
//    }
}
