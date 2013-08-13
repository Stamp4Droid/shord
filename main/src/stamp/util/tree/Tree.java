package tree;

/**
 *
 * @author brycecr
 */
public class Tree<T> {

    protected Node<T> root;

    public Tree(T treename) {
        root = new Node<T>(treename);
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
        //} else {
        //	return null;
        //}
        return null;
    }

    public Iterator<T> iterator() {
        return new TreeIterator<T>();
    }

    class TreeIterator<T> implements Iterator {
    
        Node<T> currentNode = null;
        NodeIterator<Node<T>> currentItr = null;

        public TreeIterator<T>() {
            currentNode = getRoot();
            while (currentNode.hasChildren()) {
                currentNode = currentNode.getChildren().get(0);
            }
            currentNode = getParent(currentNode);
            currentItr = currentNode.iterator();
        }
    
        @implement
        public boolean hasNext() {
            return !(currentItr == null || ((!currentItr.hasNext()) && isRoot(currentNode)));
        }
        
        @implement
        public T next() {
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

        // Does not implement remove
    }
}
