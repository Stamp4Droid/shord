package stamp.util.tree;

import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 *
 * @author brycecr
 */
public class Tree<T> {

    protected Node<T> root;
    protected final String label;

    public Tree(String l) {
        root = new Node<T>();
        label = l;
    }

    public Tree(T treename) {
        root = new Node<T>(treename);
        label = "AnonTree: root";
    }

    public String getLabel() {
        return label;
    }

    public Node<T> getRoot() {
        return root;
    }

    public boolean isRoot(T data) {
        return root.getData().equals(data);
    }

    public boolean isRoot(Node<T> node) {
        return node != null && isRoot(node.getData());
    }

    public Node<T> getParent(Node<T> child) {
        if (child == null) {
            return null;
        }
        //if (isInTree(child)) { //TODO: implement this part and throw
        //			 //exception on fail
        Node<T> parent = child.getParent();
        if (parent == null) {
            return getRoot();
        }
        return null;
    }

    public Node<T> getSuccessor(Node<T> node, int[] depthDelta) {

        //if (isInTree(child)) { //TODO: implement this part and throw
        //			 //exception on fail
        int depthChange = 0;

        while (!isRoot(node)) {
            Node<T> parent = this.getParent(node);
            ArrayList<Node<T>> siblings = this.getParent(node).getChildren();
            int ind = siblings.indexOf(node);
            if (ind == -1) {
                return null;
            } else if (ind == siblings.size() - 1) {
                depthChange -= 1;
                node = parent;
                continue;
            } else {
                return siblings.get(ind + 1);
            }
        }

        depthDelta[0] = depthChange;
        return getRoot();
    }

    public TreeIterator iterator() {
        return new TreeIterator();
    }

    public class TreeIterator implements Iterator<T> {
    
        protected Node<T> currentNode = null;
        protected Node<T>.NodeIterator currentItr = null;
        protected int depth = 0; //track depth so clients know when level is changed

        public TreeIterator() {
            currentNode = getRoot();
            currentItr = currentNode.iterator();
        }
    
        public boolean hasNext() {
            return !(currentItr == null || ((!currentItr.hasNext()) && isRoot(currentNode)));
        }
        
        public T next() {
            if (!hasNext()) {
                return null; //TODO should throw exception?
            }

            Node<T> nextNode;
            if (currentItr.hasNext()) {
                nextNode = currentItr.next();
            } else {
                // we need to know by how many levels we popped
                int[] depthDelta = new int[1];
                depthDelta[0] = 0; 

                nextNode = getSuccessor(currentNode, depthDelta);
                depth += depthDelta[0] - 1; //we always lose at least 1 level
            }

            if (nextNode.hasChildren()) {
                currentNode = nextNode;
                currentItr = currentNode.iterator();
                depth += 1;
            }

            return nextNode.getData();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public int getDepth() {
            return depth;
        }

            
/*
            if (!hasNext()) {
                return null; //TODO should throw exception?
            }

            assert currentItr == null; // should be checked by hasNext

            if (currentItr.hasNext()) {
                return currentItr.next().getData();
            } else {
                currentNode = getParent(currentNode);
                currentItr = currentNode.iterator();
                if (!hasNext()) {
                    return null; //TODO should throw exception?
                } else {
                    return currentItr.next().getData();
                }
            }
        }

        public boolean hasMoreChildren() {
            return (currentItr != null && currentItr.hasNext());
        }
*/

        // Does not implement remove
    }
}
