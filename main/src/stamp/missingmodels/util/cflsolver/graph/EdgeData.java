package stamp.missingmodels.util.cflsolver.graph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.UnaryProduction;

public interface EdgeData {
	public EdgeData produce(UnaryProduction unaryProduction);
	public EdgeData produce(BinaryProduction binaryProduction, EdgeData secondData);
	public EdgeData produce(AuxProduction auxProduction, EdgeData auxData);
	
	public static final class Field implements EdgeData {
		public final static Field DEFAULT_FIELD = new Field(-1);
		
		public final int field;
		
		public Field(int field) {
			this.field = field;
		}

		@Override
		public Field produce(UnaryProduction unaryProduction) {
			return unaryProduction.ignoreFields ? DEFAULT_FIELD : this;
		}

		@Override
		public Field produce(BinaryProduction binaryProduction, EdgeData secondData) {
			if(secondData instanceof Field) {
				Field otherField = (Field)secondData;
				if(binaryProduction.ignoreFields) {
					return DEFAULT_FIELD;
				} else if(this.field == otherField.field) {
					return DEFAULT_FIELD;
				} else if(this.field == DEFAULT_FIELD.field) {
					return otherField;
				} else if(otherField.field == DEFAULT_FIELD.field) {
					return this;
				} else {
					return null;
				}			
			} else {
				throw new RuntimeException("Mismatched edge data!");
			}
		}
		
		@Override
		public String toString() {
			return Integer.toString(this.field);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + field;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Field other = (Field) obj;
			if (field != other.field)
				return false;
			return true;
		}

		@Override
		public Field produce(AuxProduction auxProduction, EdgeData auxData) {
			return auxProduction.ignoreFields ? DEFAULT_FIELD : this;
		}
	}
	
	public static final class Context implements EdgeData {
		public static final Context DEFAULT_CONTEXT = new Context(true);
		private static final int MAX_CONTEXT_DEPTH = 0;

		private final LinkedList<Integer> contexts = new LinkedList<Integer>();
		private final boolean isForward;
		
		public Context(boolean isForward) {
			this.isForward = isForward;
		}
		
		public Context(int context, boolean direction) {
			this.contexts.add(context);
			this.isForward = direction;
			this.trim();
		}
		
		public Context(List<Integer> contexts, boolean direction) {
			this.contexts.addAll(contexts);
			this.isForward = direction;
			this.trim();
		}
		
		private void add(int context) {
			this.contexts.add(context);
			this.trim();
		}
		
		private void addAll(List<Integer> contexts) {
			this.contexts.addAll(contexts);
			this.trim();
		}
		
		private void trim() {
			while(this.contexts.size() > MAX_CONTEXT_DEPTH) {
				this.contexts.removeFirst();
			}
		}
		
		public List<Integer> getContexts() {
			return Collections.unmodifiableList(this.contexts);
		}

		@Override
		public Context produce(UnaryProduction unaryProduction) {
			if(unaryProduction.ignoreContexts) {
				return DEFAULT_CONTEXT;
			} else if(unaryProduction.isInputBackwards) {
				return new Context(this.contexts, !this.isForward);
			} else {
				return this;
			}
		}

		@Override
		public Context produce(BinaryProduction binaryProduction, EdgeData secondData) {
			if(binaryProduction.ignoreContexts) {
				return DEFAULT_CONTEXT;
			}
			if(secondData instanceof Context) {
				Context secondContext = (Context)secondData;
				
				// -> -> => -> or <- <- => <-
				// -> <- => match + ->
				// <- -> => error
				if((this.isForward ^ binaryProduction.isFirstInputBackwards) == (secondContext.isForward ^ binaryProduction.isSecondInputBackwards)) {
					boolean newDirection = this.isForward ^ binaryProduction.isFirstInputBackwards;
					LinkedList<Integer> firstContextList = newDirection ? this.contexts : secondContext.contexts;
					LinkedList<Integer> secondContextList = newDirection ? secondContext.contexts : this.contexts;
					Context newContext = new Context(firstContextList, newDirection);
					newContext.addAll(secondContextList);
					//System.out.println(binaryProduction);
					//System.out.println(newContext + " :- " + this + ", " + secondData + ".");
					return newContext;
				} else if(this.isForward ^ binaryProduction.isFirstInputBackwards) {
					int thisSize = this.contexts.size();
					int secondSize = secondContext.contexts.size();
					int minSize = Math.min(thisSize, secondSize);
					for(int i=0; i<minSize; i++) {
						if(!this.contexts.get(thisSize-i-1).equals(secondContext.contexts.get(secondSize-i-1))) {
							//System.out.println(binaryProduction);
							//System.out.println("null :- " + this + ", " + secondData + ".");
							return null;
						}
					}
					Context newContext = new Context(true);
					for(int i=0; i<thisSize-minSize; i++) {
						newContext.add(this.contexts.get(i));
					}
					//System.out.println(binaryProduction);
					//System.out.println(newContext + " :- " + this + ", " + secondData + ".");
					return newContext;
				} else {
					throw new RuntimeException("Unhandled case");
				}
			} else {
				throw new RuntimeException("Mismatched context data!");
			}
		}

		@Override
		public Context produce(AuxProduction auxProduction, EdgeData auxData) {
			return auxProduction.ignoreContexts ? DEFAULT_CONTEXT : this;
		}	
		
		@Override
		public String toString() {
			if(this.contexts.size() == 0) {
				return "default";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(this.isForward ? "" : "_");
			for(int i=0; i<this.contexts.size()-1; i++) {
				sb.append(this.contexts.get(i)).append(",");
			}
			sb.append(this.contexts.getLast());
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((contexts == null) ? 0 : contexts.hashCode());
			result = prime * result + (isForward ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Context other = (Context) obj;
			if (contexts == null) {
				if (other.contexts != null)
					return false;
			} else if (!contexts.equals(other.contexts))
				return false;
			if (isForward != other.isForward)
				return false;
			return true;
		}
	}
	
	public static final class NewContext implements EdgeData {
		public static final NewContext DEFAULT_CONTEXT = new NewContext();
		private static final int MAX_CONTEXT_DEPTH = 1;
		
		/*
		 * A context is )) ... ((
		 * Forward contexts are (( and backward contexts are )) (this is to denote the direction)
		 * The closeContexts are )) and openContexts are the (( (this is to denote start/end of stack)
		 * The context depth is the length of the )) and (( (in this example, 2)
		 * )a)b ... (c(d + )e)f ... (g(h = d==e && c==f ? )a)b ... (g(h
		 */
		
		private final LinkedList<Integer> openContexts = new LinkedList<Integer>();
		private final LinkedList<Integer> closeContexts = new LinkedList<Integer>();
		
		private boolean openOverflow = false;
		private boolean closeOverflow = false;
		
		public NewContext() {}
		
		public NewContext(int context, boolean isForward) {
			if(isForward) {
				this.openContexts.add(context);
			} else {
				this.closeContexts.add(context);
			}
			this.trim();
		}
		
		private boolean pushClose(int closeContext) {
			if(this.openContexts.isEmpty()) {
				if(!this.openOverflow && !this.closeOverflow) { 
					this.closeContexts.addLast(closeContext);
				}
				return true;
			} else {
				int openContext = this.openContexts.removeLast();
				return closeContext == openContext;
			}
		}
		
		private void pushCloseAny() {
			this.openContexts.clear();
		}
		
		private void pushOpenAny() {
			this.openContexts.clear();
			this.openOverflow = true;
		}
		
		private void pushOpen(int openContext) {
			this.openContexts.addLast(openContext);
		}
		
		private void trim() {
			while(this.openContexts.size() > MAX_CONTEXT_DEPTH) {
				this.openContexts.removeFirst();
				this.openOverflow = true;
			}
			while(this.closeContexts.size() > MAX_CONTEXT_DEPTH) {
				this.closeContexts.removeLast();
				this.closeOverflow = true;
			}
		}
		
		private NewContext getCopy() {
			NewContext context = new NewContext();
			context.openContexts.addAll(this.openContexts);
			context.closeContexts.addAll(this.closeContexts);
			context.openOverflow = this.openOverflow;
			context.closeOverflow = this.closeOverflow;
			return context;
		}
		
		private NewContext getReverse() {
			NewContext context = new NewContext();
			context.openContexts.addAll(this.closeContexts);
			context.closeContexts.addAll(this.openContexts);
			context.openOverflow = this.closeOverflow;
			context.closeOverflow = this.openOverflow;
			return context;
		}

		@Override
		public NewContext produce(UnaryProduction unaryProduction) {
			if(unaryProduction.ignoreContexts) {
				return DEFAULT_CONTEXT;
			} else if(unaryProduction.isInputBackwards) {
				return this.getReverse();
			} else {
				return this;
			}
		}

		@Override
		public NewContext produce(BinaryProduction binaryProduction, EdgeData secondData) {
			if(binaryProduction.ignoreContexts) {
				return DEFAULT_CONTEXT;
			}
			if(secondData instanceof NewContext) {
				// STEP 1: Get the first and second contexts in the right direction
				NewContext firstContext = binaryProduction.isFirstInputBackwards ? this.getReverse() : this;
				NewContext secondContext = binaryProduction.isSecondInputBackwards ? ((NewContext)secondData).getReverse() : (NewContext)secondData;
				
				// STEP 2: Initialize the new context as a copy of the first context
				NewContext newContext = firstContext.getCopy();
				
				// STEP 3: Push the close contexts, and clear the new open contexts if they are overflowed
				for(int closeContext : secondContext.closeContexts) {
					if(!newContext.pushClose(closeContext)) {
						return null;
					}
				}
				if(secondContext.closeOverflow){
					newContext.pushCloseAny();
				}
				
				// STEP 4: Clear the new open contexts if they are overflowed, and push the open contexts
				if(secondContext.openOverflow) {
					newContext.pushOpenAny();
				}
				for(int openContext : secondContext.openContexts) {
					newContext.pushOpen(openContext);
				}
				
				// STEP 5: Trim the context
				newContext.trim();
				
				return newContext;
			} else {
				throw new RuntimeException("Mismatched context data!");
			}
		}

		@Override
		public NewContext produce(AuxProduction auxProduction, EdgeData auxData) {
			return auxProduction.ignoreContexts ? DEFAULT_CONTEXT : this;
		}
		
		@Override
		public String toString() {
			/*
			if(this.contexts.size() == 0) {
				return "default";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(this.isForward ? "" : "_");
			for(int i=0; i<this.contexts.size()-1; i++) {
				sb.append(this.contexts.get(i)).append(",");
			}
			sb.append(this.contexts.getLast());
			return sb.toString();
			*/
			return "";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((closeContexts == null) ? 0 : closeContexts.hashCode());
			result = prime * result + (closeOverflow ? 1231 : 1237);
			result = prime * result
					+ ((openContexts == null) ? 0 : openContexts.hashCode());
			result = prime * result + (openOverflow ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NewContext other = (NewContext) obj;
			if (closeContexts == null) {
				if (other.closeContexts != null)
					return false;
			} else if (!closeContexts.equals(other.closeContexts))
				return false;
			if (closeOverflow != other.closeOverflow)
				return false;
			if (openContexts == null) {
				if (other.openContexts != null)
					return false;
			} else if (!openContexts.equals(other.openContexts))
				return false;
			if (openOverflow != other.openOverflow)
				return false;
			return true;
		}
	}
}
