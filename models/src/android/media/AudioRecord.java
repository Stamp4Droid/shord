public class AudioRecord {
    @STAMP(flows = {@Flow(from="$AUDIO",to="audioData")})
	public int read(short[] audioData, int offsetInShort, int sizeInShorts) { return 0; }
}