package java.io;

class FilterWriter {

    @STAMP(flows = { @Flow(from = "out", to = "this") })
    protected FilterWriter(java.io.Writer out) { }

}