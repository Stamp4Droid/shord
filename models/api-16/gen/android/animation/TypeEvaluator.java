package android.animation;
public interface TypeEvaluator<T>
{
public abstract  T evaluate(float fraction, T startValue, T endValue);
}
