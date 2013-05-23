class LinkedList<E>{
  public boolean add(E object){
   this.f = object;
   return true;  
  }
  // Auxiliary Code for method add
  private E f;

  public java.lang.Object[] toArray()  
  {      
    Object[] a = new Object[1];
    a[0] = f;
    return a;
  }
}


