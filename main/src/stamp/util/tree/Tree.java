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

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here
	}
}
