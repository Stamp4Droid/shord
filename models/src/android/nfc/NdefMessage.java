package android.npc;

public class NdefMessage {

    @STAMP(flows={@Flow(from="data",to="this")})
    public NdefMessage(byte[] data) {

    }

}

