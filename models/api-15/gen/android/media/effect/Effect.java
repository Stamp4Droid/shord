package android.media.effect;
public abstract class Effect
{
public  Effect() { throw new RuntimeException("Stub!"); }
public abstract  java.lang.String getName();
public abstract  void apply(int inputTexId, int width, int height, int outputTexId);
public abstract  void setParameter(java.lang.String parameterKey, java.lang.Object value);
public  void setUpdateListener(android.media.effect.EffectUpdateListener listener) { throw new RuntimeException("Stub!"); }
public abstract  void release();
}
