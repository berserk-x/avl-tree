///////// Skeleton for Lab 6 part II: AVL Balanced Trees /////////
/*
  Complete the implementation of AVL Binary Search Trees by modifying this
program.  Test and demonstrate the implementation with the test program at
https://cs.hofstra.edu/~cscccl/csc17/avltest.java.

You must implement the LL() and RR() procedures inside the AVLNode
class and call them when appropriate from the adjust() method.  These
methods also must call the setheight() method so that each node is
guaranteed to have the correct height.  Here again, is the summary of
the AVL balancing algorithm.

1. call setheight, which sets the height and returns the balance factor
   (right.depth() - left.depth()).
2. if balance factor is less than -1 (left side too deep) then:
       a. if ((AVLNode)left).left.depth() is >= ((AVLNode)left).right.depth(), 
          then apply an LL() single rotation.
       b. else apply an LR() double rotation as defined in class.
3. if balance factor is greater than 1 (right side too deep) then apply
   an RR() or RL() rotation symmetrically.

The professor will devise torture tests for your program.
*/

package avltree;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Stack;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public class AVLSet<T extends Comparable<? super T>> extends BstSet<T>
implements Iterable<T>
{
    //  inherited:
    // /*final*/ Tree<T> Empty = new Nil(); 
    /*
       I made a mistake.  I shouldn't have made the Empty variable in
       the BstSet superclass "final".  Then I could've just changed
       Empty from a Nil to an AVLNil in the constructor.  Without
       changing the superclass, we would have to define a new Empty.
       But then all inherited code would still be using the original
       Empty.  Unlike methods, Java does not allow dynamic dispatch on
       instance variables.  Unfortunately this means I had to go back
       to the superclass and make one small change (comment out `final`).

       The following code assumes that Empty in the superclass isn't final:
    */
    public AVLSet() {   // new constructor
      super();
      Empty = new AVLNil();  // change from new Nil()
      root = Empty;
    }
    public AVLSet(Comparator<T> cmp) { 
      super(cmp); 
      Empty = new AVLNil();  
      root = Empty;
    }  

    ///// wrapper class methods:  all inherited except the new ones

    public boolean remove(T x) { // returns true if removed
    if (x==null) return false;
    int prev_size = size;
    root = root.remove(x);
    return size < prev_size;
    }

    public void visit_preorder(BiConsumer<Tree<T>,T> bc) {
    if (bc==null) return;
    var nv = new NodeVisitor<T>(Empty,bc);
    root.visit_preorder(nv);
    }

    public Stream<T> stream() { return root.stream(); }
    public Iterator<T> iterator() { return root.stream().iterator(); }

    // non-recursive binary search, slightly more efficient
    public boolean search(T x) {
        if (x==null) return false;
    Tree<T> current = root;
    while (!current.is_empty()) {
        var current_node = (AVLNode)current;
        int c = cmp.compare(x, current_node.item);
        if (c==0) return true;
        else if (c<0) current = current_node.left;
        else current = current_node.right;
    }
    return false;
    }//non-recursive search

  //////////////////////////// new inner classes //////////////////////

  class AVLNil extends Nil
  {
    @Override    
    public Tree<T> insert(T x) {
      size++;
      return new AVLNode(x,Empty,Empty);  // change from new Node(..)
    }//insert
    @Override
    public String toString() { return ""; }
  }//AVLNil

  class AVLNode extends Node
  {
    // item, left, right inherited
    int height; // height of this subtree

    @Override
    public int depth() { return height; }  // now O(1)
    
    int set_height() {  // sets height in O(1) time
    int ldepth = left.depth();
        int rdepth = right.depth();
        height = 1 + Math.max(ldepth,rdepth);
        return rdepth - ldepth;
    }// returns height balance factor

    public AVLNode(T i, Tree<T> lf, Tree<T> rt) { 
       super(i,lf,rt); 
       set_height();
    }

    @Override
    public String toString() { return item+""; }

    // need to uncomment this, once you've implemented clone in BstSet
    //@Override 
    //public Tree<T> clone() {
    //   return new AVLNode(item,left.clone(), right.clone());
    //}

    // preorder visitor with parent
    public void visit_preorder(NodeVisitor<T> nv) {
       nv.accept(item);
       left.visit_preorder(nv.with_parent(this));
       right.visit_preorder(nv.with_parent(this));
    }

    public Stream<T> stream() {
       return
       Stream.generate(() -> left.stream()).limit(1)
       .map(ls -> Stream.concat(ls, Stream.of(this.item)))
       .flatMap(ms -> Stream.concat(ms, right.stream()));
    }//stream

      // removal
    public Tree<T> remove(T x) {
    int c = x.compareTo(item);
    if (c<0) left = left.remove(x);
    else if (c>0) right = right.remove(x);
    else { // found it
        size--;
        if (left.is_empty()) return right;
        else {  // delete largest value on left subtree
        left = ((AVLNode)left).delete_max(this);
        }
    }
    adjust();
    return this;
    }//remove

    Tree<T> delete_max(Node to_modify) {
        /*
    switch (right) {    // requires recent jdk
    case AVLNode rn :
        right = rn.delete_max(to_modify);
        adjust();
        return this;
    case AVLNil _ :
        to_modify.item = this.item;
        return left;
        default : throw new Error("This should never happen!");
    }
    */
    if (right.is_empty()) {
        to_modify.item = this.item;
        return left;
    }
    else {
        right = ((AVLNode)right).delete_max(to_modify);
        adjust();
        return this;
    }
    }//delete_max

    /////////////////// THIS STILL HAS TO BE IMPLEMENTED FULLY /////////
    @Override
    void adjust() {
    int bf = set_height();
    if (bf < -1) {
        if (((AVLNode)left).left.depth() >= ((AVLNode)left).right.depth()) {
            LL();
        } else {
            LR();
            }
    }
    if (bf > 1) {
        if (((AVLNode)right).right.depth() >= ((AVLNode)right).left.depth()) {
            RR();
        } else {
            RL();
        }
    }
    } // for now
    ////////////////////////////////////////////////////


    // These should basically be the same as the ones you already defined,
    // HOWEVER, be sure to type-cast to AVLNode instead of Node, as in
    // AVLNode lnode = (AVLNode)left; ...

    void LL() {
        var lnode = (AVLNode)this.left;
        this.left = lnode.left;
        lnode.left = lnode.right;
        lnode.right = this.right;
        this.right = lnode;
        lnode.set_height();
        this.set_height();
    }
    void RR() {
        var rnode = (AVLNode)this.right;
        this.right = rnode.right;
        rnode.right = rnode.left;
        rnode.left = this.left;
        this.left = rnode;
        rnode.set_height();
        this.set_height();
        
    }

    void LR() {
        if (this.left != null) {
            ((AVLNode)this.left).LL();
        }

        RR();
    }

    void RL() {
        if (this.right != null) {
            ((AVLNode)this.right).RR();
        }
        LL();
    }
  }//AVLNode inner class

}//AVLSet wrapper class


// for visit-preorder, visit only within package
    class NodeVisitor<T> implements Consumer<T> {
        Tree<T> parent;
        final BiConsumer<Tree<T>, T> visitor;
        public NodeVisitor(Tree<T> p, BiConsumer<Tree<T>, T> visitor) {
            parent = p;
            this.visitor = visitor;
        }
        public NodeVisitor<T> with_parent(Tree<T> p) {
            parent = p;
            return this;
        }
        public void accept(T item) {  // for consumer interface
            visitor.accept(parent,item);
        }
    }
