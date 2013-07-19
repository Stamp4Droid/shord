package android.text;

class Html {

    @STAMP(flows = { @Flow(from = "source", to = "@return") })
    public static android.text.Spanned fromHtml(java.lang.String source) {
        return new android.text.SpannableString(source);
    }

    @STAMP(flows = { @Flow(from = "source", to = "@return") })
    public static android.text.Spanned fromHtml(java.lang.String source, android.text.Html.ImageGetter imageGetter, android.text.Html.TagHandler tagHandler) {
        return new android.text.SpannableString(source);
    }
}

