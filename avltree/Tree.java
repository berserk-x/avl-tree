package avltree;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public interface Tree<T> 
{
    boolean is_empty();
    int depth();
    boolean contains(T x);
    Tree<T> insert(T x);
    default Tree<T> remove(T s) { return this; } // for later
    void map_inorder(Consumer<? super T> cf);
    void ifPresent(Consumer<? super T> cf); // calls function on item
    <U> U match(Function<? super T,? extends U> fn, Supplier<? extends U> fe);
    Optional<T> min(); // return min value of subtree

default void visit_preorder(NodeVisitor<T> nv) {}
default Stream<T> stream() { return Stream.empty(); }

    ////////////// implement fully for Lab 6 part I (see lab description)

    Optional<T> max(); // uncomment when done
    Tree<T> clone();
    Tree<T> successor(T x, Tree<T> ancestor);
    Tree<T> predecessor(T x, Tree<T> ancestor);
    //boolean is_bst();
}

