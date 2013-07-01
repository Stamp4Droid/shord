package android.speech;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class SpeechRecognizer {

    SpeechRecognizer() {
        throw new RuntimeException("Stub!");
    }

    public static boolean isRecognitionAvailable(android.content.Context context) {
        throw new RuntimeException("Stub!");
    }

    public static android.speech.SpeechRecognizer createSpeechRecognizer(android.content.Context context) {
        throw new RuntimeException("Stub!");
    }

    public static android.speech.SpeechRecognizer createSpeechRecognizer(android.content.Context context, android.content.ComponentName serviceComponent) {
        throw new RuntimeException("Stub!");
    }

    public void stopListening() {
        throw new RuntimeException("Stub!");
    }

    public void cancel() {
        throw new RuntimeException("Stub!");
    }

    public void destroy() {
        throw new RuntimeException("Stub!");
    }

    public static final java.lang.String RESULTS_RECOGNITION = "results_recognition";

    public static final java.lang.String CONFIDENCE_SCORES = "confidence_scores";

    public static final int ERROR_NETWORK_TIMEOUT = 1;

    public static final int ERROR_NETWORK = 2;

    public static final int ERROR_AUDIO = 3;

    public static final int ERROR_SERVER = 4;

    public static final int ERROR_CLIENT = 5;

    public static final int ERROR_SPEECH_TIMEOUT = 6;

    public static final int ERROR_NO_MATCH = 7;

    public static final int ERROR_RECOGNIZER_BUSY = 8;

    public static final int ERROR_INSUFFICIENT_PERMISSIONS = 9;

    private android.speech.RecognitionListener myListener;

    public void setRecognitionListener(android.speech.RecognitionListener listener) {
        myListener = listener;
    }

    public void startListening(android.content.Intent recognizerIntent) {
        myListener.onResults(taintedBundle());
    }

    @STAMP(flows = { @Flow(from = "$AUDIO", to = "@return") })
    private android.os.Bundle taintedBundle() {
        return new android.os.Bundle();
    }
}

