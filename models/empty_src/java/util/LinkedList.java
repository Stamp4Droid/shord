class LinkedList<E>
{
	private E f;

	@STAMP(flows = {@Flow(from="object",to="this")}) 
	public boolean add(E object){
		return true;  
	}
	
	@STAMP(flows = {@Flow(from="object",to="this")}) 
	public void add(int location, E object) {
	}

	@STAMP(flows = {@Flow(from="object",to="this")}) 
	public void addFirst(E object) {
    }

	@STAMP(flows = {@Flow(from="object",to="this")}) 
    public void addLast(E object) {
    }

	@STAMP(flows = {@Flow(from="e",to="this")}) 
    public void push(E e) {
    }
	
	@STAMP(flows = {@Flow(from="object",to="this")}) 
	public E set(int location, E object) {
	    return null;
    }

	@STAMP(flows = {@Flow(from="o",to="this")}) 
    public boolean offer(E o) {
		return true;
    }

    public E poll() {
	return null;
    }

    public E remove() {
	return null;
    }

    public E peek() {
	return null;
    }

    public E element() {
	return null;
    }

}


