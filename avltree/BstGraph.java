/* Graphical representation of binary trees (updated Fall 2024)

   Compatible Tree<D> interface in Tree.java

   this file and "Tree.java", "BinSearchTree.java" must be in same directory:
   best to compile together.
  
   Everytime drawtree is called, the background is cleared, so you
   don't have to create a new BstGraph object to draw a new tree.

   See "main" at end of file for sample usage
*/

package avltree;
import java.awt.*;import java.awt.event.WindowEvent;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class BstGraph extends JFrame
{
    public int XDIM, YDIM;
    public Graphics display;
    public Tree<?> currenttree;

    public void paint(Graphics g) {drawtree(currenttree);} // override method

    // constructor sets window dimensions
    public BstGraph(int x, int y)
    {
	XDIM = x;  YDIM = y;
	this.setBounds(0,0,XDIM+4,YDIM);
	this.setVisible(true); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	display = this.getGraphics();
	// draw static background as a black rectangle
	display.setColor(Color.black);
	display.fillRect(0,0,x,y);
        display.setColor(Color.red);
	try{Thread.sleep(700);} catch(Exception e) {} // Synch with system
    }  // drawingwindow


    // internal vars used by drawtree routines:
    public int bheight = 50; // default branch height
    public int yoff = 40;  // static y-offset
    public int xoff = 8;

    // l is level, lb,rb are the bounds (position of left and right child)

    public void drawtree(Tree<?> T)
    {
        if (T==null) return;
	currenttree = T;
	int d = T.depth();
	display.setColor(Color.white);
	display.fillRect(0,0,XDIM,YDIM);  // clear background
	if (d<1) return;
	bheight = ((YDIM-yoff)/d);
	draw(T,0,0,XDIM-xoff);
    }

    public void draw(BstSet<?> t)
    {
	drawtree(t.root);
    }

    public void draw(Tree<?> Nd, int l, int lb, int rb)
    {
	if (Nd.is_empty()) return;
	var N = ((BstSet<?>.Node)Nd);
	//	try{Thread.sleep(10);} catch(Exception e) {} // slow down
        display.setColor(Color.green);
	display.fillOval(((lb+rb)/2)-10,yoff+(l*bheight),20,20);
	display.setColor(Color.red);
	display.drawString(N.item+"",((lb+rb)/2)-5,yoff+15+(l*bheight));
	display.setColor(Color.blue); // draw branches
        if (!N.left.is_empty())
	    {
   	       display.drawLine((lb+rb)/2,yoff+10+(l*bheight),
			((3*lb+rb)/4),yoff+(l*bheight+bheight));
	       draw(N.left,l+1,lb,(lb+rb)/2);
	    }
        if (!N.right.is_empty())
	    {
               display.drawLine((lb+rb)/2,yoff+10+(l*bheight),
			((3*rb+lb)/4),yoff+(l*bheight+bheight));
	       draw(N.right,l+1,(lb+rb)/2,rb);
	    }
    } // draw

    public void delay(int ms) {  // delay by milliseconds
	try {Thread.sleep(ms);} catch (Exception e) {}
    }
    // display string at specified coordinates
    public void display_string(String s, int x, int y) {
	display.drawString(s,x,y);
    }
    // close window, stops program
    public void close() {
	dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /* sample use:  (put this in another file) ************** 
    public static void main(String[] args)
    {
      BstGraph W = new BstGraph(1024,768); // for graphical rendering
      BstSet<Integer> tree = new BstSet<Integer>();
      for(int i = 0;i<64;i++) tree.insert((int)(Math.random()*100));
      W.draw(tree);
      try{Thread.sleep(5000);} catch(Exception e) {} // 5 sec delay
      //for(int x=1;x<100;x+=2) tree.remove(x); // delete all odd numbers
      W.draw(tree);
      // System.out.println(tree); // should be sorted
    }  // main
    ********************/

} // BstGraph
