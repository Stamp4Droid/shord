public class SpeechRecognizer{
	
	private android.speech.RecognitionListener myListener;
	
	public void setRecognitionListener(android.speech.RecognitionListener listener) {
		myListener = listener;
	}

	public void startListening(android.content.Intent recognizerIntent) {
		myListener.onResults(taintedBundle());
	}

	@STAMP(flows={@Flow(from="$AUDIO",to="@return")})
	private android.os.Bundle taintedBundle(){
		return new android.os.Bundle();
	}
}
