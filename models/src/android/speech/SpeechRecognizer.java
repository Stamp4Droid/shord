import edu.stanford.stamp.annotation.Inline;

public class SpeechRecognizer
{
	@Inline
	public void setRecognitionListener(android.speech.RecognitionListener listener) 
	{
		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		list.add(taintedString());
		android.os.Bundle b = new android.os.Bundle();
		b.putStringArrayList(RESULTS_RECOGNITION, list);
		listener.onResults(b);
	}

	public void startListening(android.content.Intent recognizerIntent) {
	}

	@STAMP(flows={@Flow(from="$AUDIO",to="@return")})
	private String taintedString()
	{
		return new String();
	}
}
