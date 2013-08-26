package stamp.util.tree;

import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simple minimal tree implementation. This is designed
 * to support the callgraph creation in SrcSinkFlowViz class
 * and so is supports functionality that is important there
 * and little more. 
 *
 * @author brycecr
 */
public class Tree<T> {

    protected Node<T> root;
    protected final String label;

    public Tree(String l) {
        root = new Node<T>();
        root.setParent(root);
        label = l;
    }

    public Tree(T treename) {
        root = new Node<T>(treename);
        root.setParent(root);
        label = "AnonTree: root";
    }

    public String getLabel() {
        return label;
    }

    public Node<T> getRoot() {
        return root;
    }

    public boolean isRoot(Node<T> node) {
        return node != null && root.equals(node);
    }

    public boolean isRoot(T dat) {
        return getRoot().getData() != null && getRoot().getData().equals(dat);
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
        return parent;
    }

    public Node<T> getSuccessor(Node<T> node) {
        return getSuccessor(node, new int[1]);
    }

    public Node<T> getSuccessor(Node<T> node, int[] depthDelta) {

        //if (isInTree(child)) { //TODO: implement this part and throw
        //			 //exception on fail
        int depthChange = 0;

        while (!isRoot(node)) {
            Node<T> parent = this.getParent(node);
            ArrayList<Node<T>> siblings = parent.getChildren();
            int ind = siblings.indexOf(node);
            if (ind == -1) {
                System.err.println("Node not found in getSuccessor");
                return null;
            } else if (ind == siblings.size() - 1) {
                depthChange -= 1;
                node = parent;
                continue;
            } else {
                depthDelta[0] = depthChange;
                return siblings.get(ind + 1);
            }
        }

        depthDelta[0] = depthChange;
        return getRoot();
    }

    public TreeIterator iterator() {
        return new TreeIterator();
    }

    public String toString() {
        TreeIterator itr = iterator();
        StringBuilder builder = new StringBuilder();

        while (itr.hasNext()) {
            T entry = itr.next();
            int depth = itr.getDepth();

            for (int i = 0; i < depth; ++i) {
                builder.append("----");
            }

            String str;
            if (isRoot(entry)) {
                str = "Root";
            } else {
                str = entry.toString();
            }

            builder.append(str);
            builder.append('\n');
        }

        return builder.toString();
    }

    /**
     * Iterator for trees. Notice that next() returns a T, not
     * Node<T>. Tree is traversed depth-first (first) and in order of
     * insertion (FIFO) (second). The final node is the Root.
     *
     * Iterating while maintianing knowledge of depth requires calling getDepth()
     * after every call to next.
     *
     */
    public class TreeIterator implements Iterator<T> {
    
        protected Node<T> currentNode = null;
        protected Node<T>.NodeIterator currentItr = null;
        protected int depth = 0; //track depth so clients know when level is changed

        public TreeIterator() {
            currentNode = getRoot();
            currentItr = currentNode.iterator();
        }
    
        public boolean hasNext() {
            return !(isRoot(getSuccessor(currentNode)) && isRoot(currentNode) 
                && (currentItr == null || !currentItr.hasNext()));
        }
        
        /**
         * Returns the data object of type T of the next node to be traversed.
         * Traversal order is depth-first and FIFO second.
         */
        public T next() {
            if (!hasNext()) {
                return null; //TODO should throw exception?
            }

            Node<T> nextNode;

            if (currentNode.hasChildren()) {
                currentItr = currentNode.iterator();
                assert currentItr.hasNext();
                nextNode = currentItr.next();
                currentNode = nextNode;
                depth += 1;
            } else if (currentItr != null && currentItr.hasNext()) {
                nextNode = currentItr.next();
            } else {
                // we need to know by how many levels we popped
                int[] depthDelta = new int[1];
                depthDelta[0] = 0; 

                nextNode = getSuccessor(currentNode, depthDelta);
                currentNode = nextNode;
                currentItr = null;
                depth += depthDelta[0]; //we always lose at least 1 level
            }

            return nextNode.getData();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public int getDepth() {
            return depth;
        }
    }
}
