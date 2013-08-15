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
        root.setParent(root);
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

    public boolean isRoot(Node<T> node) {
        return node != null && root.equals(node);
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

    public Node<T> getSuccessor(Node<T> node, int[] depthDelta) {

        //if (isInTree(child)) { //TODO: implement this part and throw
        //			 //exception on fail
        int depthChange = 0;

        System.out.println("***GetSuccessor***");
        System.out.println("Node " + node.getData());

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
                depthDelta[0] = depthChange;
                System.out.println("BROTHERHOOD");
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
            return !(isRoot(currentNode) && (currentItr == null || !currentItr.hasNext()));
        }
        
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
