package android.nfc.tech;

import android.nfc.NdefMessage;

public class Ndef {

    @STAMP(flows = {@Flow(from="msg",to="!NDEFMESSAGE")})
    public void writeNdefMessage(android.nfc.NdefMessage msg) {

    }

}
