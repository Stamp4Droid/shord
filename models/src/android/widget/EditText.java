class EditText
{
	@STAMP(flows={@Flow(from="this",to="@return")})
	public  android.text.Editable getText() 
	{
		return new android.text.SpannableStringBuilder();
	}
}