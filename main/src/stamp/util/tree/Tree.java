/*
 * Copyright 2013 Bryce Cronkite-Ratcliff.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
