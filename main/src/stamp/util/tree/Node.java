package stamp.util.tree;

import java.lang.UnsupportedOperationException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author brycecr
 */
public class Node<T> {

    protected ArrayList<Node<T>> children = null;
    protected Node<T> parent = null;
    protected T data = null;

    public Node() {
    }

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

    protected void setParent(Node<T> newParent) {
        parent = newParent;
    }

    public void replaceChild(Node<T> toReplace, T replacement) {
        Node<T> newNode = new Node<T>(replacement, this);

        if (children == null) {
            return;
        }

        int ind = children.indexOf(toReplace);
        if (ind == -1) {
            return; // TODO throw exception?
        }

        children.set(ind, newNode);
        newNode.addChild(toReplace);
        toReplace.setParent(newNode);
    }

    /**
     * @return ArrayList of children of this node
     */
    public ArrayList<Node<T>> getChildren() {
        return children;
    }

    public Node<T> addChild(Node<T> newchild) {
        if (children == null) {
            children = new ArrayList<Node<T>>();
        }
        children.add(newchild);
        return newchild;
    }

    public Node<T> addChild(T newData) {
        Node<T> newChild = new Node<T>(newData, this);
        return addChild(newChild);
    }

    public void addParent(Node<T> newparent) throws UnsupportedOperationException {
        if (parent == null) {
            parent = newparent;
        } else {
            throw new UnsupportedOperationException("Tree: Adding new "
                    + "parent to Node with non-null parent.");
        }
    }

    public T getData() {
        return data;
    }

    public boolean hasChildren() {
        return !(children == null || children.size() == 0);
    }

    public NodeIterator iterator() {
        return new NodeIterator();
    }

    class NodeIterator implements Iterator<Node<T>> {

        Iterator<Node<T>> itr = null;
    
        public NodeIterator() {
            itr = children.iterator();
            for (Node<T> n : children) {
                System.err.println(n.getData());
            }
        }

        public boolean hasNext() {
            return itr.hasNext();
        }
    
        public Node<T> next() {
            return itr.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        //Does not implement remove, currently
    }
}
