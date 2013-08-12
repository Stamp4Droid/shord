package tree;

import java.util.ArrayList;

/**
 *
 * @author brycecr
 */
public class Node<T> {

	protected ArrayList<Node<T>> children = new ArrayList<Node<T>>();
	protected Node<T> parent = null;
	protected T data;

	public Node(T id) {
		data = id;
	}

	public Node(T id, ArrayList<Node<T>> initchildren) {
		children = initchildren;
		data = id;
	}

	public Node(T id, Node<T> initparent) {
		parent = initparent;
		data = id;
	}

	public Node(T id, Node<T> initparent, ArrayList<Node<T>> initchildren) {
		parent = initparent;
		children = initchildren;
		data = id;
	}

	/**
	 * 
	 * @return parent node of this node. May be null.
	 */
	public Node<T> getParent() {
		return parent;
	}

	/**
	 * @return ArrayList of children of this node
	 */
	public ArrayList<Node<T>> getChildren() {
		assert children != null;
		return children;
	}

	public Node<T> addChild(Node<T> newchild) {
		children.add(newchild);
		return newchild;
	}

	public void addParent(Node<T> newparent) throws InvalidNodeOpException {
		if (parent == null) {
			parent = newparent;
		} else {
			throw new InvalidNodeOpException("Tree: Adding new "
				+ "parent to Node with non-null parent.");
		}
	}

        public T getData() {
            return data;
        }

	private static class InvalidNodeOpException extends Exception {

		public InvalidNodeOpException(String str) {
			super(str);
		}
	}
}
