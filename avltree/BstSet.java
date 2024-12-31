package avltree;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Stack;
import java.util.Optional;
import java.util.function.*;

public class BstSet<T extends Comparable<? super T>> {

    Tree<T> Empty = new Nil();    // only instance ever needed

    Tree<T> root = Empty;
    int size = 0;
    Comparator<T> cmp = (x,y) -> x.compareTo(y);

    public BstSet() {}
    public BstSet(Comparator<T> cmp) {
	if (cmp!=null) this.cmp = cmp;
    }
    
    ///// wrapper class methods:

    public int size() {return size;}
    public int depth() {return root.depth();}
    public boolean contains(T x) { 
	if (x==null) return false;
	else return root.contains(x); 
    }
    public boolean insert(T x) { 
        if (x==null) return false;
	int previous_size = size;
        root = root.insert(x);
        return size > previous_size;
    } // returns true if something was inserted, false otherwise
    public Optional<T> min() { return root.min(); }
    public void map_inorder(Consumer<? super T> cf) {
	if (cf != null) root.map_inorder(cf);
    }
    public void ifPresent(Consumer<? super T> cf) {
        root.ifPresent(cf);
    }
    public <U> U match(Function<? super T,? extends U> fn, 
                       Supplier<? extends U> fe)  { 
	return root.match(fn,fe);
    }



  //////////////////////////////// inner classes //////////////////

  class Nil implements Tree<T>
  {
    public boolean is_empty() { return true; }
    public int depth() { return 0; }
    public boolean contains(T x) { return false; }
    public Tree<T> insert(T x) {
      size++;  // advantage of having a wrapper class
      return new Node(x,Empty,Empty);
    }//insert
    public Optional<T> min() { return Optional.empty(); }
    public void map_inorder(Consumer<? super T> cf) {}
    public void ifPresent(Consumer<? super T> cf) {}
    public <U> U match(Function<? super T,? extends U> fn, 
                       Supplier<? extends U> fe)  { 
      return fe.get(); 
    }
    public Optional<T> max() { return Optional.empty(); }
    public Tree<T> clone() { 
        T x = null;
        return new Node(x , Empty, Empty); 
    }
    public Tree<T> successor(T x, Tree<T> ancestor) { return Empty; }
    public Tree<T> predecessor(T x, Tree<T> ancestor) { return Empty; }



  }//Nil

  class Node implements Tree<T>
  {
    T item;
    Tree<T> left;
    Tree<T> right;
    public Node(T i, Tree<T> lf, Tree<T> rt) {
	item = i;  left=lf;  right = rt;
    }
    public boolean is_empty() { return false; }
    public int depth() { return 1 + Math.max(left.depth(),right.depth()); }
    public boolean contains(T x) {
      int c = cmp.compare(x,item);
      return (c==0) || (c<0 && left.contains(x)) || (c>0 && right.contains(x));
      /* for earthlings, the above line is equivalent to:
         if (c==0) return true;
         else if (c<0) return left.contains(x);
         else return right.contains(x);
      */
    }//contains

    public Tree<T> insert(T x) {
	int c = cmp.compare(x,item);
        if (c<0) left = left.insert(x);
        else if (c>0) right = right.insert(x);
        // else c==0 and x is a duplicate, ignore
        adjust(); // to be completed later ...
        return this;
    }//insert

    public Optional<T> min() { 
	if (left.is_empty()) return Optional.of(this.item);
	else return left.min();
    }

    public void map_inorder(Consumer<? super T> cf) {
	left.map_inorder(cf);
	cf.accept(this.item);
        right.map_inorder(cf);
    }

    /// these functions are not recursive: only works on item ...
    public void ifPresent(Consumer<? super T> cf) { 
         cf.accept(this.item);
    }
    public <U> U match(Function<? super T,? extends U> fn, 
                       Supplier<? extends U> fe)  { 
	return fn.apply(this.item);
    }

    public Optional<T> max() {
        if(right.is_empty()) return Optional.of(this.item);
        else return right.max();
    }

    @Override
    public Tree<T> clone() {
        return new Node(item, left, right);
    }

    public Tree<T> successor(T x, Tree<T> ancestor) {
        int comparison = cmp.compare(x, item);
            
            if (comparison == 0) {
                if (!right.is_empty()) {
                    Tree<T> current = right;
                    while (((Node)current).left.is_empty() == false) {
                        current = ((Node)current).left;
                    }
                    return current;
                } else {
                    return ancestor;
                }
            }
            
            if (comparison < 0) {
                return ((Node)left).successor(x, this);
            }
            
            return ((Node)right).successor(x, ancestor);
        }

    public Tree<T> predecessor(T x, Tree<T> ancestor) {
        int comparison = cmp.compare(x, item);

            if (comparison == 0) {
                if (!left.is_empty()) {
                    Tree<T> current = left;
                    while (!((Node)current).right.is_empty()) {
                        current = ((Node)current).right;
                    }
                    return current;
                } else {
                    return ancestor;
                }
            }

            if (comparison > 0) {
                return ((Node) right).predecessor(x, this);
            }

            return ((Node) left).predecessor(x, ancestor);
    }

    ///// for comparison, non-recursive version of insert
    public void add(T x) {
	Node current = this;
	boolean stop = false;
	while (!stop) {
	    int c = x.compareTo(current.item);
	    if (c<0) {
		if (current.left.is_empty()) {
		    current.left = new Node(x,Empty,Empty);
		    stop = true;
		}
		else current = (Node)current.left;
	    }
	    else if (c>0) {
		if (current.right.is_empty()) {
		    current.right = new Node(x,Empty,Empty);
		    stop = true;
		}
		else current = (Node)current.right;
	    }
	    else stop = true;  // duplicate found
	}//while
    }//add

    void adjust() {
        if (!left.is_empty() && left.depth() > right.depth() + 1) {
            LL();
        } else if (!right.is_empty() && right.depth() > left.depth() + 1) {
            RR();
        }
    } // for now

    void LL() {
        Node lnode = (Node)left;
        left = lnode.right;
        lnode.right = this;
        this.item = lnode.item;
        this.left = lnode.left;
        this.right = lnode.right;
    } 
    
    void RR() {
        Node rnode = (Node)right;
        right = rnode.left;
        rnode.left = this;
        this.item = rnode.item;
        this.left = rnode.left;
        this.right = rnode.right;

    }
  }//Node inner class

}//BstSet wrapper class
