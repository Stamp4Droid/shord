package android.speech.tts;
public interface SynthesisCallback
{
public abstract  int getMaxBufferSize();
public abstract  int start(int sampleRateInHz, int audioFormat, int channelCount);
public abstract  int audioAvailable(byte[] buffer, int offset, int length);
public abstract  int done();
public abstract  void error();
}
