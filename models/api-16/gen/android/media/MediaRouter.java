package android.media;
public class MediaRouter
{
public static class RouteInfo
{
RouteInfo() { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getName() { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getName(android.content.Context context) { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getStatus() { throw new RuntimeException("Stub!"); }
public  int getSupportedTypes() { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteGroup getGroup() { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteCategory getCategory() { throw new RuntimeException("Stub!"); }
public  android.graphics.drawable.Drawable getIconDrawable() { throw new RuntimeException("Stub!"); }
public  void setTag(java.lang.Object tag) { throw new RuntimeException("Stub!"); }
public  java.lang.Object getTag() { throw new RuntimeException("Stub!"); }
public  int getPlaybackType() { throw new RuntimeException("Stub!"); }
public  int getPlaybackStream() { throw new RuntimeException("Stub!"); }
public  int getVolume() { throw new RuntimeException("Stub!"); }
public  void requestSetVolume(int volume) { throw new RuntimeException("Stub!"); }
public  void requestUpdateVolume(int direction) { throw new RuntimeException("Stub!"); }
public  int getVolumeMax() { throw new RuntimeException("Stub!"); }
public  int getVolumeHandling() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static final int PLAYBACK_TYPE_LOCAL = 0;
public static final int PLAYBACK_TYPE_REMOTE = 1;
public static final int PLAYBACK_VOLUME_FIXED = 0;
public static final int PLAYBACK_VOLUME_VARIABLE = 1;
}
public static class UserRouteInfo
  extends android.media.MediaRouter.RouteInfo
{
UserRouteInfo() { throw new RuntimeException("Stub!"); }
public  void setName(java.lang.CharSequence name) { throw new RuntimeException("Stub!"); }
public  void setName(int resId) { throw new RuntimeException("Stub!"); }
public  void setStatus(java.lang.CharSequence status) { throw new RuntimeException("Stub!"); }
public  void setRemoteControlClient(android.media.RemoteControlClient rcc) { throw new RuntimeException("Stub!"); }
public  android.media.RemoteControlClient getRemoteControlClient() { throw new RuntimeException("Stub!"); }
public  void setIconDrawable(android.graphics.drawable.Drawable icon) { throw new RuntimeException("Stub!"); }
public  void setIconResource(int resId) { throw new RuntimeException("Stub!"); }
public  void setVolumeCallback(android.media.MediaRouter.VolumeCallback vcb) { throw new RuntimeException("Stub!"); }
public  void setPlaybackType(int type) { throw new RuntimeException("Stub!"); }
public  void setVolumeHandling(int volumeHandling) { throw new RuntimeException("Stub!"); }
public  void setVolume(int volume) { throw new RuntimeException("Stub!"); }
public  void requestSetVolume(int volume) { throw new RuntimeException("Stub!"); }
public  void requestUpdateVolume(int direction) { throw new RuntimeException("Stub!"); }
public  void setVolumeMax(int volumeMax) { throw new RuntimeException("Stub!"); }
public  void setPlaybackStream(int stream) { throw new RuntimeException("Stub!"); }
}
public static class RouteGroup
  extends android.media.MediaRouter.RouteInfo
{
RouteGroup() { throw new RuntimeException("Stub!"); }
public  void addRoute(android.media.MediaRouter.RouteInfo route) { throw new RuntimeException("Stub!"); }
public  void addRoute(android.media.MediaRouter.RouteInfo route, int insertAt) { throw new RuntimeException("Stub!"); }
public  void removeRoute(android.media.MediaRouter.RouteInfo route) { throw new RuntimeException("Stub!"); }
public  void removeRoute(int index) { throw new RuntimeException("Stub!"); }
public  int getRouteCount() { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteInfo getRouteAt(int index) { throw new RuntimeException("Stub!"); }
public  void setIconDrawable(android.graphics.drawable.Drawable icon) { throw new RuntimeException("Stub!"); }
public  void setIconResource(int resId) { throw new RuntimeException("Stub!"); }
public  void requestSetVolume(int volume) { throw new RuntimeException("Stub!"); }
public  void requestUpdateVolume(int direction) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
}
public static class RouteCategory
{
RouteCategory() { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getName() { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getName(android.content.Context context) { throw new RuntimeException("Stub!"); }
public  java.util.List<android.media.MediaRouter.RouteInfo> getRoutes(java.util.List<android.media.MediaRouter.RouteInfo> out) { throw new RuntimeException("Stub!"); }
public  int getSupportedTypes() { throw new RuntimeException("Stub!"); }
public  boolean isGroupable() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
}
public abstract static class Callback
{
public  Callback() { throw new RuntimeException("Stub!"); }
public abstract  void onRouteSelected(android.media.MediaRouter router, int type, android.media.MediaRouter.RouteInfo info);
public abstract  void onRouteUnselected(android.media.MediaRouter router, int type, android.media.MediaRouter.RouteInfo info);
public abstract  void onRouteAdded(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info);
public abstract  void onRouteRemoved(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info);
public abstract  void onRouteChanged(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info);
public abstract  void onRouteGrouped(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info, android.media.MediaRouter.RouteGroup group, int index);
public abstract  void onRouteUngrouped(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info, android.media.MediaRouter.RouteGroup group);
public abstract  void onRouteVolumeChanged(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info);
}
public static class SimpleCallback
  extends android.media.MediaRouter.Callback
{
public  SimpleCallback() { throw new RuntimeException("Stub!"); }
public  void onRouteSelected(android.media.MediaRouter router, int type, android.media.MediaRouter.RouteInfo info) { throw new RuntimeException("Stub!"); }
public  void onRouteUnselected(android.media.MediaRouter router, int type, android.media.MediaRouter.RouteInfo info) { throw new RuntimeException("Stub!"); }
public  void onRouteAdded(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info) { throw new RuntimeException("Stub!"); }
public  void onRouteRemoved(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info) { throw new RuntimeException("Stub!"); }
public  void onRouteChanged(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info) { throw new RuntimeException("Stub!"); }
public  void onRouteGrouped(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info, android.media.MediaRouter.RouteGroup group, int index) { throw new RuntimeException("Stub!"); }
public  void onRouteUngrouped(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info, android.media.MediaRouter.RouteGroup group) { throw new RuntimeException("Stub!"); }
public  void onRouteVolumeChanged(android.media.MediaRouter router, android.media.MediaRouter.RouteInfo info) { throw new RuntimeException("Stub!"); }
}
public abstract static class VolumeCallback
{
public  VolumeCallback() { throw new RuntimeException("Stub!"); }
public abstract  void onVolumeUpdateRequest(android.media.MediaRouter.RouteInfo info, int direction);
public abstract  void onVolumeSetRequest(android.media.MediaRouter.RouteInfo info, int volume);
}
MediaRouter() { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteInfo getSelectedRoute(int type) { throw new RuntimeException("Stub!"); }
public  void addCallback(int types, android.media.MediaRouter.Callback cb) { throw new RuntimeException("Stub!"); }
public  void removeCallback(android.media.MediaRouter.Callback cb) { throw new RuntimeException("Stub!"); }
public  void selectRoute(int types, android.media.MediaRouter.RouteInfo route) { throw new RuntimeException("Stub!"); }
public  void addUserRoute(android.media.MediaRouter.UserRouteInfo info) { throw new RuntimeException("Stub!"); }
public  void removeUserRoute(android.media.MediaRouter.UserRouteInfo info) { throw new RuntimeException("Stub!"); }
public  void clearUserRoutes() { throw new RuntimeException("Stub!"); }
public  int getCategoryCount() { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteCategory getCategoryAt(int index) { throw new RuntimeException("Stub!"); }
public  int getRouteCount() { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteInfo getRouteAt(int index) { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.UserRouteInfo createUserRoute(android.media.MediaRouter.RouteCategory category) { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteCategory createRouteCategory(java.lang.CharSequence name, boolean isGroupable) { throw new RuntimeException("Stub!"); }
public  android.media.MediaRouter.RouteCategory createRouteCategory(int nameResId, boolean isGroupable) { throw new RuntimeException("Stub!"); }
public static final int ROUTE_TYPE_LIVE_AUDIO = 1;
public static final int ROUTE_TYPE_USER = 8388608;
}
