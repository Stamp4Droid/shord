import edu.stanford.stamp.annotation.Inline;

class TextView
{
	public  TextView(android.content.Context context)
	{
		super(context);
	}		

	@STAMP(flows={@Flow(from="this",to="@return")})
	java.lang.CharSequence getText() 
	{ 
		return new String();
	}

        @Inline
	public  void setKeyListener(android.text.method.KeyListener input) 
	{ 
		input.onKeyDown(this, null, 0, null);
		input.onKeyOther(this, null, null);
		input.onKeyUp(this, null, 0, null);
	}

        @Inline
	public  void setOnEditorActionListener(android.widget.TextView.OnEditorActionListener l) 
	{ 
		l.onEditorAction(this, 0, null);
	}

	
}