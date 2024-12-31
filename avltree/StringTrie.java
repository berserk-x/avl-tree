/* String-keyed tries.

   You can easily find articles explaining what tries are.  Most often
they will have strings as keys and the strings are considered character
by character.  The keys themselves determine the structure of the "trie".
The root of the trie is always associated with the empty string as its key.
Let's say you also need keys "ab", "abc", "abx", and "abcd".  This forms 
the trie:
             ""
             |
             a
             |
             ab
           /    \
         abx    abc
                 |
                abcd

The key associated with a parent node is always a *prefix* of the keys
associated with its descendants (in the subtree beneath it).  A node
can have an arbitrary number of children: up to 26 if we're limited to
lower case characters, more if other chars are allowed.  Each child
extends the parent key by one character.

Notice that even though "a" was not one of the keys we need, we still need
a node in the trie that correspond to it, for otherwise we won't be
able to form the nodes beneath it.

At each node, we can optionally store a value. Not all nodes will have
values: sometimes only the leave nodes of the tree will have values.
Unlike hashmaps and treemaps, however, we don't need to store the keys
inside the nodes: the path from the root to the node defines the key.

The time complexity of searching, inserting or removing a key is 
O(length_of_key).  Some sources says this is O(n) but that's misleading:
n is the not the number of nodes in the trie but the length of the longest
key, which correspond to the *depth* of the trie.

Tries were invented by AT&T, the only phone company back in the 1950's
to store information about phone numbers. It allows, for example, numbers
with the same area code to be grouped together in the same "subtrie".
Unlike hashmap/treemap, a trie allows us to consider an entire group
of values that's associated with a key *prefix*.  Each key prefix defines
a subtree of other keys and values. We can get all numbers at Hofstra
with the prefix "516-463".  Even though 516-463 is not an actual phone
number, it is a node in the trie: it contains no value but its 10-digit
descendants will have values.

This program will make heavy use of both the Optional and Stream monads.
*/

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.HashSet;
import java.util.TreeSet; // red-black tree, similar to AVL tree
import java.util.stream.Stream;
import java.util.function.*;

record SVPair<V>(String key, V val) {
    @Override
	public String toString() { return key+" : "+val; }
}

//record rankedword(String word, int rank){}


public class StringTrie<VT> {
    
    // inner class for a Trie Node (but public)
    public class Node {
       	Optional<VT> item = Optional.empty();
	HashMap<Character,Node> children = new HashMap<Character,Node>();
	// could also use an array ..., hashmap is more memory efficient
	Node() {}
	Node(VT x) {
	    item = Optional.ofNullable(x); // will be empty if x is null
	}

	void cleanup() {
	    HashSet<Character> to_remove = new HashSet<Character>();
	    for (var c : children.keySet()) {
		Node child = children.get(c);
		child.cleanup();
		if (child.item.isEmpty() && child.children.size()==0) {
		    //this.children.remove(c);
		    to_remove.add(c);
		    nodes--;
		}
	    }
	    for (var c:to_remove) children.remove(c);
	}//cleanup

	Stream<SVPair<VT>> stream(String prefix, int depth) {
	    if (depth<1) return Stream.empty();
	    return
	    Stream.concat(item.stream()
			  .map(i -> new SVPair<VT>(prefix,i)),
			  
   		          children.keySet().stream()
			  .flatMap(c -> children.get(c)
				        .stream(prefix+c,depth-1)));
	}
    }// inner class Node

    //////////////// Outer Class Variables and Methods  /////////////////

    int size=0; // number of values stored in Trie
    int nodes=1;  // number of Nodes in Trie
    Node root = new Node();
    
    public int size() { return size; }
    public double load_factor() { return size*1.0 / nodes; }

    public Optional<VT> and_modify(String key,
			      Function<Optional<VT>,? extends VT> modifier) {
	Optional<VT> answer = Optional.empty();
	if (key==null || modifier==null) return answer;
	Node current = root;
	int k = 0; // indexes chars in key
	while (k < key.length()) {
	    current =
		current.children.computeIfAbsent(key.charAt(k),
						 p -> {
						     nodes++;
						     return new Node();
						 });
	    k++;
	}//while
	// at this point, current points to node containing the value,
	// or the value doesn't exist inside the trie
	answer = current.item; // previous item
	current.item = Optional.ofNullable(modifier.apply(current.item));
	if (answer.isEmpty() && current.item.isPresent())  size++;
	return answer;	
    }
    
    // insert or change key-value pair, return previous value
    public Optional<VT> set(String key, VT val) {
	return and_modify(key, x -> val);
    }// set

    // lookup value given key, with option to delete, don't insert new nodes
    Optional<VT> search(String key, boolean delete) {
        Optional<VT> answer = Optional.empty();
	if (key==null) return answer;
	Node current = root;
	int k = 0; // indexes chars in key
	while (k < key.length()) {
	    current = current.children.get(key.charAt(k));
	    if (current==null) return answer;
	    k++;
	}//while
	answer = current.item;
	if (delete && answer.isPresent()) {
	    current.item = Optional.empty();
	    size--;
	}
	return answer;
    }//search

    public Optional<VT> get(String key) { return search(key,false); }
    public Optional<VT> remove(String key) {return search(key,true); }
    
    public int cleanup() {
	int snodes = nodes;
	root.cleanup();
	return snodes - nodes;
    }

    // continuation (so don't have to restart from root each time)
    private Optional<Node> continue_node = Optional.of(root);
    private String continue_key = "";
    public void reset_continuation() {
        continue_node = Optional.of(root);
        continue_key = "";
    }
    public void begin_continuation(String start) {
        if (start==null) return;
        continue_key = start;
        Node current = root;
        int k=0;
        while(k<start.length()) {
            current = current.children.get(start.charAt(k));
            if (current==null) {
                continue_node = Optional.empty();
                return;
            }
            k++;
        }
        continue_node = Optional.of(current);
    }    
    public boolean can_continue() { return continue_node.isPresent(); }
    public String current_key() { return continue_key; }
    public Optional<VT> current_val() {
        return continue_node.flatMap(n -> n.item);
    }
    public boolean continue_search(char nextchar) {
        continue_key = continue_key + nextchar;
        continue_node =
            continue_node
            .flatMap(cn->Optional.ofNullable(cn.children.get(nextchar)));
        return continue_node.isPresent();
    }//continue_search
    
    public Stream<SVPair<VT>> current_stream(int depth) {
        return
            continue_node.stream()
            .flatMap(n -> n.stream(continue_key,depth));
    }
    public Stream<SVPair<VT>> current_stream() {
        return current_stream(0x7fffffff);
    }
    
    public Stream<SVPair<VT>> stream() {
	return root.stream("",0);
    }

    public Stream<SVPair<VT>> stream(String prefix, int depth) {
	if (prefix==null) return Stream.empty();
	if (depth<1) depth = 0x7fffffff;
	Node current = root;
	int k = 0;
	while (k<prefix.length()) {
	    current = current.children.get(prefix.charAt(k));
	    if (current==null) return Stream.empty();
	    k++;
	}
	return current.stream(prefix,depth);
    }    
    public Stream<SVPair<VT>> stream(String prefix) {
	return stream(prefix,0);
    }
    
    //////////// for testing
    public static void main1(String[] args) {
	var GPA = new StringTrie<Double>();
	String[] Roster={"Alex","Tyrone","Alexi","Alexander","Alexandra","Al","Tyler"};
	for(String n:Roster)
	    GPA.set(n,((int)(Math.random()*401))/100.0);

	GPA.remove("Alexi");
	GPA.set("Alexander",3.5);
	System.out.println("size: "+GPA.size());		
	for(String n:Roster)
	    System.out.println(n+" has a GPA of "+GPA.get(n));
	GPA.set("Alexi",1.0);
	
        GPA.stream("Alex",2).forEach(System.out::println);

	GPA.remove("Alexander");
	GPA.remove("Alexandra");
	System.out.println("size: "+GPA.size());	
	System.out.println("load factor: "+GPA.load_factor());
	int cleaned = GPA.cleanup();
	System.out.println("cleaned: "+cleaned);
	System.out.println("load factor: "+GPA.load_factor());	
	
    }//main
    
}// StringTrie