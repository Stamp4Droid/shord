package android.net.wifi.p2p;
public class WifiP2pManager
{
public static interface ChannelListener
{
public abstract  void onChannelDisconnected();
}
public static interface ActionListener
{
public abstract  void onSuccess();
public abstract  void onFailure(int reason);
}
public static interface PeerListListener
{
public abstract  void onPeersAvailable(android.net.wifi.p2p.WifiP2pDeviceList peers);
}
public static interface ConnectionInfoListener
{
public abstract  void onConnectionInfoAvailable(android.net.wifi.p2p.WifiP2pInfo info);
}
public static interface GroupInfoListener
{
public abstract  void onGroupInfoAvailable(android.net.wifi.p2p.WifiP2pGroup group);
}
public static class Channel
{
Channel() { throw new RuntimeException("Stub!"); }
}
WifiP2pManager() { throw new RuntimeException("Stub!"); }
public  android.net.wifi.p2p.WifiP2pManager.Channel initialize(android.content.Context srcContext, android.os.Looper srcLooper, android.net.wifi.p2p.WifiP2pManager.ChannelListener listener) { throw new RuntimeException("Stub!"); }
public  void discoverPeers(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pManager.ActionListener listener) { throw new RuntimeException("Stub!"); }
public  void connect(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pConfig config, android.net.wifi.p2p.WifiP2pManager.ActionListener listener) { throw new RuntimeException("Stub!"); }
public  void cancelConnect(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pManager.ActionListener listener) { throw new RuntimeException("Stub!"); }
public  void createGroup(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pManager.ActionListener listener) { throw new RuntimeException("Stub!"); }
public  void removeGroup(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pManager.ActionListener listener) { throw new RuntimeException("Stub!"); }
public  void requestPeers(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pManager.PeerListListener listener) { throw new RuntimeException("Stub!"); }
public  void requestConnectionInfo(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener listener) { throw new RuntimeException("Stub!"); }
public  void requestGroupInfo(android.net.wifi.p2p.WifiP2pManager.Channel c, android.net.wifi.p2p.WifiP2pManager.GroupInfoListener listener) { throw new RuntimeException("Stub!"); }
public static final java.lang.String WIFI_P2P_STATE_CHANGED_ACTION = "android.net.wifi.p2p.STATE_CHANGED";
public static final java.lang.String EXTRA_WIFI_STATE = "wifi_p2p_state";
public static final int WIFI_P2P_STATE_DISABLED = 1;
public static final int WIFI_P2P_STATE_ENABLED = 2;
public static final java.lang.String WIFI_P2P_CONNECTION_CHANGED_ACTION = "android.net.wifi.p2p.CONNECTION_STATE_CHANGE";
public static final java.lang.String EXTRA_WIFI_P2P_INFO = "wifiP2pInfo";
public static final java.lang.String EXTRA_NETWORK_INFO = "networkInfo";
public static final java.lang.String WIFI_P2P_PEERS_CHANGED_ACTION = "android.net.wifi.p2p.PEERS_CHANGED";
public static final java.lang.String WIFI_P2P_THIS_DEVICE_CHANGED_ACTION = "android.net.wifi.p2p.THIS_DEVICE_CHANGED";
public static final java.lang.String EXTRA_WIFI_P2P_DEVICE = "wifiP2pDevice";
public static final int ERROR = 0;
public static final int P2P_UNSUPPORTED = 1;
public static final int BUSY = 2;
}
