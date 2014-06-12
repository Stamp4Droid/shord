package stamp.missingmodels.util.cflsolver.graph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.UnaryProduction;

public interface EdgeData {
	public EdgeData produce(UnaryProduction unaryProduction);
	public EdgeData produce(BinaryProduction binaryProduction, EdgeData secondData);
	public EdgeData produce(AuxProduction auxProduction, EdgeData auxData);
	
	public static final class Field implements EdgeData {
		public final static Field DEFAULT_FIELD = new Field(-1);
		
		private final int field;
		
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
		private static final int MAX_CONTEXT_DEPTH = 1;

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
			return true;
		}	
	}	
}
