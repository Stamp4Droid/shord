class StringWriter {

    @STAMP(flows = { @Flow(from = "chars", to = "this") })
    public void write(char[] chars, int offset, int count) {
    }

    @STAMP(flows = { @Flow(from = "oneChar", to = "this") })
    public void write(int oneChar) {
    }

    @STAMP(flows = { @Flow(from = "str", to = "this") })
    public void write(java.lang.String str) {
    }

    @STAMP(flows = { @Flow(from = "str", to = "this") })
    public void write(java.lang.String str, int offset, int count) {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.StringBuffer getBuffer() {
        return new StringBuffer();
    }
}

