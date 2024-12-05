/*
 *Author: Luis E. Ortiz
 *Purpose: Code for simple stock market trading simulator using priority queues based on heaps and
 a hash map to keep track of trade transaction records
# Update: 10/30/2023 - Refactored
*/

import java.io.*;
import java.util.*;
import java.text.*;

/*
 *Purpose: Class definition of a simple implementation of an ordered map ADT,
 mapping integers keys to integer values, using a binary search tree (BST)
 *NOTE: only basic methods of the ordered map ADT (i.e., put, erase, find, size, empty), and some auxiliary functions (e.g., successor and predecessor) implemented
*/



// Key structure for stock market trading
class Key {
    public double price;
    //public Date timeStamp;
    public int timeStamp;
    public Key() {
        price = 0.0;
        //timeStamp = new Date();
        timeStamp = 0;
    }
    //public Key(double p, Date t) {
    public Key(double p, int t) {
        price = p;
        timeStamp = t;
    }

    // overloading output stream operator for keys
    // INPUT: output stream out and a key (both passed by ref)
    // OUTPUT: the output stream (passed by ref)
    // PRECONDITION: properly initialized input
    // POSTCONDITION: string-formatted key sent to out
    public String toString () {
        NumberFormat formatter = new DecimalFormat("#0.00");
        return("(" + formatter.format(price) + "," + timeStamp + ")");
    }

    // less-than key-comparison
    // INPUT: key y
    // OUPUT: true iff the price of this < price of y, or prices are equal but time stamp of this < time stamp of y
    // PRECONDITION: properly initialized keys
    public boolean isLessThan (Key y) {
        return ((this.price < y.price) || ((this.price == y.price) && (this.timeStamp < y.timeStamp)));
    }
    public static boolean isLessThan(Key z, Key y) {
        return ((z.price < y.price) || ((z.price == y.price) && (z.timeStamp < y.timeStamp)));
    }
}



// Value structure for stock-market trading
class Value {
    int numShares;
    int traderID;
    public Value() {
        numShares = 0;
        traderID = 0;
    }
    public Value(int num, int id) {
        numShares = num;
        traderID = id;
    }

    // INPUT:
    // OUTPUT: string representation of this Value
    // PRECONDITION:
    // POSTCONDITION:
    public String toString() {
        return("(" + numShares + "," + traderID + ")");
    }
}

// Element structure used to represent elements of a node in a binary tree
class Elem {
    Key key;
    Value value;
    public Elem() {
        key = null;
        value = null;
    }
    public Elem(Key k, Value v) {
        key = k;
        value = v;
    }
    public Elem(Elem w) {
        key = w.key;
        value = w.value;
    }

    // INPUT:
    // OUTPUT: string representation of this Elem
    // PRECONDITION:
    // POSTCONDITION:
    public String toString() {
        return (key + ":" + value);
    }

    // INPUT: element y
    // PRECONDITION: input is non-NULL
    // POSTCONDITON: the data members of this and y are swapped
    public void swapElem(Elem y) {
        Key key = y.key;
        Value value = y.value;
        y.key = this.key;
        y.value = this.value;
        this.key = key;
        this.value = value;
    }
    // less-than comparison operator for elements
    // INPUT: element y
    // OUTPUT: true iff the key of this < key of y
    // PRECONDITION: input properly initialized (each element's key is non-NULL)
    public boolean isLessThan (Elem y) {
        return (this.key.isLessThan(y.key));
    }
    public boolean isLessThan (Elem z, Elem y) {
        return (Key.isLessThan(z.key, y.key));
    }
    // greater-than comparison operator for elements
    // INPUT: element y
    // OUTPUT: true iff the key of  this > key of y
    // PRECONDITION: input properly initialized
    public boolean isGreaterThan (Elem y) {
        return (y.key.isLessThan(this.key));
    }
}

// Binary tree (BT) ADT
class BT {
    // simple data structure used to create nodes for (linked-list) implementation of general BTs
    public class Node {
        public Elem elem;
        public Node left;
        public Node right;
        public Node parent;
        public Node() {
            elem = null;
            left = null;
            right = null;
            parent = null;
        }
        public Node(Elem e, Node l, Node r, Node p) {
            elem = e;
            left = l;
            right = r;
            parent = p;
        }
    }

    // utility/aux function to print out a parenthetic string representation of a BT
    // INPUT: a node w in the BT whose subtree is to be printed out; or NULL
    // PRECONDITION: w has been properly initialized (non-NULL element)
    protected void printAux(Node w) {
        if (w != null) {
            System.out.print("[" + w.elem + "]");
            System.out.print("(");
            printAux(w.left);
            System.out.print("),(");
            printAux(w.right);
            System.out.print(")");
        }
    }

    public Node root;
    protected int n;

    public BT() {
        root = null;
        n = 0;
    }

    // INPUT: a pair of nodes w and z in the BT
    // PRECONDITION: w and z non-NULL
    // POSTCONDITION: w and z swap their elements
    public void swapElem(Node w, Node z) {
        // just swaps values not places...
        Elem tmp = w.elem;
        w.elem = z.elem;
        z.elem = tmp;
    }

    // INPUT: p, a node in the BT or NULL; c, a node in the BT or NULL; and a Boolean flag, isLeft, signaling whether c should become the left child of p
    // PRECONDITION:
    // POSTCONDITON: the corresponding child of p is set to c, depending on the input; if c is not NULL, then p becomes its parent
    public void makeChild(Node p, Node c, boolean isLeft) {
        if (p != null)
            if (isLeft) p.left = c;
            else p.right = c;
        if (c != null) c.parent = p;
    }

    /*
      # INPUT: a node w in the BT or NULL;
      # ancestor type flag: True if searching for youngest left ancestor
      # OUTPUT: the node corresponding to the youngest right/left ancestor
      # of w: this is the node x in the tree
      # that is the first ancestor of w whose immediate descendant is a
      # left/right child, depending on input type flag (said differently, the node x is the first found in the path from w to
      # the root such that for a node z that is also an ancestor of w,
      # thus also in that path to the root from w, and where z could be w
      # itself, we have x.right = z or x.left = z, depending on input type flag); or NULL if w is NULL or the root
      # node of the tree
    */
    public Node youngestAncestorType(Node w, boolean check_left) {
        if (w == null) return null;
        Node z = w;
        Node x = z.parent;
        // while x is not null && if check left is true then check if x.left eqauls Z else check if right equals Z
        while ((x != null) && ((check_left ? x.right : x.left) == z)) {
            z = x;
            x = z.parent;
        }
        return x;
    }

    /*
      # INPUT: a node w in the BT or NULL;
      # descendant type flag: True iff searching for youngest left descendant
      # OUTPUT: a node corresponding to the youngest left/right descendant
      # of w, inclusive: this is the node x in the tree
      # that is the last left/right descendant of w, depending on input
      # type flag; or NULL if w is NULL
    */
    public Node youngestDescendantType(Node w, boolean check_left) {
        if (w == null) return null;
        while ((check_left ? w.left : w.right) != null)
            w = check_left ? w.left : w.right;
        return w;
    }

    // INPUT: a node w in a BT
    // OUTPUT: z = the first ancestor node (i.e., node in a path from w to the root)
    // such that w is in the left subtree of the BT rooted at z
    // PRECONDITION: w is not NULL
    public Node firstLeftAncestor(Node w) {
        return youngestAncestorType(w,true);
    }

    // INPUT: a node w in a BT
    // OUTPUT: "leftmost" node of the BT rooted at w
    // PRECONDITION:
    // POSTCONDITION:
    public Node lastLeftDescendant(Node w) {
        // NAME: <Alexander Boccaccio>
        // Your code here
        Node j = w; // test pointer
        Node p = j; // actual pointer
        while(j != null){
            p = j;      // protect the main pointer
            j = j.left; // the child is sacrificed
        }
        return p;       // return the pure pointer
    }

    // INPUT: a node w in a BT
    // OUTPUT: z = the first ancestor node (i.e., node in a path from w to the root)
    // such that w is the right subtree of the BT rooted at z
    // PRECONDITION:
    // POSTCONDITION:
    public Node firstRightAncestor(Node w) {
        // NAME: <Alexander Boccaccio>
        // Your code here

        if(w == this.root){
            return w;
        }
        while (w.parent.left != w){ // check to see if w is the right child of this parent
            if(w.parent == this.root) return w.parent;
            w = w.parent;

        }
        return youngestAncestorType(w,false);
        // return w; // if w is the right child of its parent then return w;

    }

    // INPUT: a node w in a BT
    // OUTPUT: "rightmost" node of the BT rooted at w
    // PRECONDITION: w is not NULL
    public Node lastRightDescendant(Node w) {
        return youngestDescendantType(w,false);
    }

    // INPUT: w, a node in the BT
    // OUTPUT: the child with the lowest key (if any)
    // PRECONDITION: if w has both children, then their elements are non-NULL
    public Node minChild(Node w) {
        if (w == null) return null;
        Node wL = w.left;
        Node wR = w.right;
        if (wR == null) return wL;
        if (wL == null) return wR;
        Elem eL = wL.elem;
        Elem eR = wR.elem;
        return ((eL.isLessThan(eR)) ? wL : wR);
    }

    // INPUT: w, a node in the BT
    // OUTPUT: e, the element of w
    // PRECONDITION: the tree is not empty; and the left or right subtree, or both, of w are empty
    // POSTCONDITION: the size of the BT is reduced by 1 after w is properly removed from the BT
    public Elem removeNode(Node w) {
        Elem e = w.elem;
        Node z = w.parent;
        // identify child if it exists
        Node x = (w.left != null) ? w.left : w.right;
        makeChild(z, x, (z==null) || (z.left == w));
        if (z == null)
            root = x;
        n--;
        return e;
    }

    public int size() { return n; }
    public boolean empty() { return (n == 0); }

    // prints out a String representation of the whole BST using a reverse inorder traversal
    public void printTree(Node s, int space) {
        int addSpace = 8;
        // base case
        if (s == null)
        {
            return;
        }
        // add more whitespace
        space = space + addSpace;
        // print right
        this.printTree(s.right, space);

        System.out.print("\n");
        for (int i = addSpace; i < space; i++)
            System.out.print(" ");
        System.out.print(s.elem + "\n");

        // print left
        this.printTree(s.left, space);
    }

    // print out a parenthetic string representation of the whole BT
    public void print() {
        printAux(root);
        System.out.println();
    }
}

// Complete Binary Tree (Complete BT) ADT
class CompleteBT extends BT {

    public Node lastNode;

    public int CalulateDept(int n) {
        int dept = 0;
        // if (n == 0) return null;
        int bound = 0;
        while (bound < n) {
            dept++;
            bound += 2 ^ dept;
        }
        return dept;
    }

    public CompleteBT() {
        lastNode = null;
    }

    // INPUT: an element e
    // OUTPUT: the new node x in the complete BT containing e
    // PRECONDITION:
    // POSTCONDITION:
    public Node add(Elem e) {
        // NAME: <Alexander Boccaccio>
        // Your code here

        // Node(Elem e, Node l, Node r, Node p) {
        Node x = new Node(e, null, null, null);
        // Node z = this.lastNode;
        if(this.root == null) {
            this.root = x;
        } else {
            Node y = getParentOfNewLastNode();
            makeChild(y, x, y.left == null);
        }
        this.lastNode = x;
        n++;
        return x;
    }

    // OUTPUT: the element of the last node of the complete BT (to be removed); NULL if the tree is empty
    // PRECONDITION:
    // POSTCONDITION:
    public Elem remove() {
        // NAME: <alexander boccaccio>
        // Your code here
        Node out = this.lastNode;
        if (this.empty()) return null;   // empty tree
        if (lastNode == root) {         // the tree has one element
            // removeNode does not handle LastNode
            this.lastNode = null;
            return removeNode(out);
        }
        // removeNode(lastNode);
        Elem outElem = out.elem;
        removeNode(out);
        this.lastNode = getNewLastNode();
        //n--;
        // out.parent.
        return outElem;
    }


    private int getDepth(Node z) {
        if (z == null) return 0;
        int leftDepth = getDepth(z.left);
        int rightDepth = getDepth(z.right);
        return Math.min(leftDepth, rightDepth) + 1;
    }
    // OUTPUT: the node in the complete BT where any new node inserted would be placed
    // PRECONDITION:
    // POSTCONDITION:
    private Node getParentOfNewLastNode() {
        // NAME: <Alexander Boccaccio>
        // Your code here
        // return where the next new node should be placed
        // so we place in the last spot
        Node start = this.root;
        // if(start == null) return this.root;
        if(start == null){
            return null;
        }
        while(true){
            if(start.left == null || start.right == null){
                // base case where one of the children do not exist
                return start;
                // TODO check below
            }
            int l = getDepth(start.left);
            int r = getDepth(start.right);
            if (r == l){
                start = start.left;
            }
            if ( l > r){
                start = start.right;
            }

        }
        // return start;
        /*
        Node z = this.lastNode;
        // last node pointer
        if (z == null || z.parent == null) return z;
        // we check if the last is null or the root,
        // if null then there is no new parent (since it is the root so we return z!)
        // if z.parent is null then it is the root so we can return z
        if ( z == z.parent.left) return z.parent;
        // this is the easy case where z is the left child so return its parent so we can fill the right

        z = firstLeftAncestor(z);
        if(z == null){
            z = this.root;
        } else {
            z = z.right;
            z = lastLeftDescendant(z);
        }
        return z;
        */
    }

    // because sometimes math can be hard for people
    public int CalulateTotal(int numb) {
        return (2 ^ (numb + 1)) - 1;
    }

    public int CalulateTotalAtLevel(int numb) {
        return (2 ^ (numb));
    }

    // OUTPUT: the lastNode in the BT
    // PRECONDITION:    has the node been removed yet? probably so last node has a null element but a valid pointer in the structure
    // POSTCONDITION:   we return last node (booooo) but it's ok if we need to still delete the other node
    //                  it can have a global pointer before this mess
    private Node getNewLastNode() {
        // NAME: <Alexander Boccaccio>
        // Your code here
        Node newer = getParentOfNewLastNode();
        if (newer.left == null) return newer;
        return (newer.right != null) ? newer.right : newer.left;

        /*
        Node z = this.lastNode;
        // last node pointer
        if (z.parent == null) return null;
        // this means the last node is the root which means we will have no root or last node
        if (z.parent.right == z) {
            return z.parent.left;
        }

        z = firstRightAncestor(z);
        if (z == null){
            z = this.root;
        } else {
            z = z.left;
            z = lastRightDescendant(z);
        }
        return z; */
    }
}

// Heap data-structure implementation of a priority queue ADT
class Heap extends CompleteBT {

    public Heap() {
    }




    // INPUT: an element e to be inserted in the heap
    // PRECONDITION:
    // POSTCONDITION:
    public void insert(Elem e) {
        // NAME: <Alexander Boccaccio>
        // Your code here
        this.add(e);
        this.upHeapBubbling();
    }

    // OUTPUT: the minimum (highest priority) element of the heap
    // PRECONDITION:
    // POSTCONDITION:
    public Elem min() {
        // NAME: <Alexander Boccaccio>
        // Your code here
        return this.root.elem;
    }

    // PRECONDITION:
    // POSTCONDITION:
    public void removeMin() {
        // NAME: <Alexander Boccaccio>
        // Your code here
        if (this.empty() ) return;
        

        if(this.n == 1) {
            removeNode(this.root);
        } else {
            swapElem(this.lastNode, this.root);
            this.remove();
        }this.downHeapBubbling();
    }

    // PRECONDITION:
    // POSTCONDITION:
    private void upHeapBubbling() {
        // NAME: <Alexander Boccaccio>
        // Your code here
        // from last node up basically
        if ( n == 1) return;
        Node w = this.lastNode;
        Node z = w.parent;
        while (z != null && z.elem.isGreaterThan(w.elem)){
            swapElem(w, z);
            w = z;
            z = w.parent;
        }

    }

    // PRECONDITION:
    // POSTCONDITION:
    private void downHeapBubbling() {
        // NAME: <Alexander Boccaccio>
        // Your code here
        Node p = this.root;
        if(p == null) return;
        if(p.left == null) return;
        // minChild();
        // it is proabably redunant to have this twice, but it works
        while (true){
            boolean isLeft = false;
            // check it the children exist
            if (p.left == null) break;
            // this would be great to use the min child function lol
            if(p.right == null || p.left.elem.isLessThan(p.right.elem)){
                // basically we check that right does not exist and is higher than the left one.
                isLeft = true;
                // then left is the min child
            }
            if (isLeft){
                // left is the min child
                if(p.left == null) break;
                    // we check if left exist (again);
                if ((p.elem.isGreaterThan(p.left.elem))){
                    // if the parent is great than the child we need to swap
                    swapElem(p, p.left);
                    p = p.left;
                    // assgin the next case
                    if ( p == null) break;
                    // we check to make p still exist
                } else break;
            } else {
                if (p.elem.isGreaterThan(p.right.elem)){
                    swapElem(p, p.right);
                } else break;
            }
        }
    }

    public void printTree() {
        if (this.root == null) {
            System.out.println("Tree is empty");
            return;
        }

        Queue<Node> queue = new LinkedList<>();
        queue.offer(root);
        int level = 0;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            System.out.print("Level " + level + ": ");

            for (int i = 0; i < levelSize; i++) {
                Node node = queue.poll();
                if (node != null) {
                    System.out.print(node.elem + " ");
                    queue.offer(node.left);
                    queue.offer(node.right);
                } else {
                    System.out.print("null ");
                }
            }

            System.out.println();
            level++;
        }
    }
}// end heap class


// DO NOT CHANGE ANYTHING BELOW THIS LINE

    // Ledger ADT for financial books/records
    class Ledger {
        // a financial record data-structure
        public class Record {
            int id;
            double balance;
            int holdings;
            LinkedList<Elem> buyTrans;
            LinkedList<Elem> sellTrans;

            public Record() {
                id = 0;
                balance = 0.0;
                holdings = 0;
                buyTrans = new LinkedList<Elem>();
                sellTrans = new LinkedList<Elem>();
            }

            public Record(int i, double bal, int h) {
                id = i;
                balance = bal;
                holdings = h;
                buyTrans = new LinkedList<Elem>();
                sellTrans = new LinkedList<Elem>();
            }
        }

        private HashMap<Integer, Record> book;

        public Ledger() {
            book = new HashMap<Integer, Record>();
        }

        public void printTransList(List L) {
            System.out.print("(");
            Iterator it = L.iterator();
            if (it.hasNext()) {
                System.out.print(it.next());
                for (; it.hasNext(); )
                    System.out.print("," + it.next());
            }
            System.out.print(")");
        }

        public void printRecord(Record r) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            System.out.print(r.id + ":" + formatter.format(r.balance) + ":" + r.holdings + ":");
            printTransList(r.buyTrans);
            System.out.print(":");
            printTransList(r.sellTrans);
        }

        // INPUT: an element e and a Boolean flag signaling whether e corresponds to a buy transaction
        // PRECONDITION: e is non-NULL, as are its key and value
        // POSTCONDITION: the transaction is inserted into the ledger/book for the corresponding trader, and the trader record is updated (i.e., the trader's holdings/num of shares and balance/amount of money made or lost in all of the trader's transactions); the record for a new trader is created, and properly initialized, if this is the first transaction for the trader
        public void trans(Elem e, boolean isBuyTrans) {
            double price = e.key.price;
            int num = e.value.numShares;
            Integer id = e.value.traderID;
            Record record = book.get(id);
            if (record == null) {
                book.put(id, new Record(id, 0.0, 0));
                record = book.get(id);
            }
            record.holdings += num;
            record.balance += num * price;
            if (isBuyTrans) record.buyTrans.addLast(e);
            else record.sellTrans.addLast(e);
        }

        // INPUT: an element e
        // POSTCONDITION: a buy transaction is inserted into the ledger/book for the corresponding trader, and the trader record is updated to increase the trader's holdings by the number of shares bought and reduce the trader's balance by the amount of money the trader paid for the shares; if this is the first transaction for the trader, a new record is created, and properly initialized
        public void buy(Elem e) {
            trans(e, true);
        }

        // INPUT: an element e
        // POSTCONDITION: a sell transaction is inserted into the ledger/book for the corresponding trader, and the trader record is updated to reduce its holdings by the number of shares sold and increase the trader's balance by the amount of money the trader obtained for the shares; if this is the first transaction for the trader, a new record is created, and properly initialized
        public void sell(Elem e) {
            trans(e, false);
        }

        public void print() {
            for (Record it : book.values()) {
                printRecord(it);
                System.out.println();
            }
        }
    }

    // Stock Market ADT
    class StockMarket {
        private Heap buyOrders;
        private Heap sellOrders;
        private Ledger books;
        private double bank;
        private int counter = 0;

        public StockMarket() {
            buyOrders = new Heap();
            sellOrders = new Heap();
            books = new Ledger();
            bank = 0.0;
        }

        // PRECONDITION: there are matching orders in the limit-order books for the stock market
        // POSTCONDITION: any possible trade is executed, and properly documented/recorded; the limit-order books for the stock market are properly updated and maintained; the stock market's bank balance is increased if there is a margin over the markets' spread (i.e., the buy limit-order price is higher than the sell limit-order price)
        private void processTrade() {
            Elem buyLimitOrder = new Elem(buyOrders.min());
            Elem sellLimitOrder = new Elem(sellOrders.min());

            double priceBuy = -buyLimitOrder.key.price;
            double priceSell = sellLimitOrder.key.price;
            int timeBuy = buyLimitOrder.key.timeStamp;
            int timeSell = sellLimitOrder.key.timeStamp;
            int numBuy = buyLimitOrder.value.numShares;
            int numSell = sellLimitOrder.value.numShares;
            int idBuy = buyLimitOrder.value.traderID;
            int idSell = sellLimitOrder.value.traderID;

            double priceDiff = priceBuy - priceSell;

            int numTrade;
            int numRemain;

            Elem buyTrade;
            Elem sellTrade;
            Key k;
            Value v;

            sellOrders.removeMin();
            buyOrders.removeMin();

            if (numBuy > numSell) {
                numTrade = numSell;
                sellTrade = sellLimitOrder;
                k = new Key(-priceBuy, timeBuy);
                v = new Value(numTrade, idBuy);
                buyTrade = new Elem(k, v);
                numRemain = numBuy - numSell;
                // add leftover buys as a new order
                if (numRemain > 0) buyAux(numRemain, priceBuy, idBuy, timeBuy);
            } else {
                numTrade = numBuy;
                buyTrade = buyLimitOrder;
                k = new Key(priceSell, timeSell);
                v = new Value(numTrade, idSell);
                sellTrade = new Elem(k, v);
                numRemain = numSell - numBuy;
                // add leftover sells as a new order
                if (numRemain > 0) sellAux(numRemain, priceSell, idSell, timeSell);
            }
            books.buy(buyTrade);
            books.sell(sellTrade);
            bank += priceDiff * numTrade;
        }

        // POSTCONDITION: all possible trades are processed/executed and recorded/documented, the market's limit-order books are properly updated/maintained, and the market profit from the respective trades (if any) is updated/increased
        private void trade() {
            if (buyOrders.empty() || sellOrders.empty()) return;
            boolean tradeAvail = true;
            while ((tradeAvail) && !(buyOrders.empty() || sellOrders.empty())) {
                Elem buyLimitOrder = buyOrders.min();
                Elem sellLimitOrder = sellOrders.min();

                double buyPrice = -buyLimitOrder.key.price;
                double sellPrice = sellLimitOrder.key.price;

                double marketSpread = sellPrice - buyPrice;

                // process trades if lowest sell <= highest buy
                tradeAvail = (marketSpread <= 0.0);
                if (tradeAvail) processTrade();
            }
        }

        // INPUT: the number of shares and price involved in the trade, the trader's ID, and the time order was placed
        // POSTCONDITION: a new element for the order is added to the respective limit-order book for the stock market depending on the trade type
        //private void transAux(int num, double price, int id, Date t, boolean buyTrans) {
        private void transAux(int num, double price, int id, int t, boolean buyTrans) {
            Key k = new Key(price * ((buyTrans) ? -1.0 : 1.0), t);
            Value v = new Value(num, id);
            Elem e = new Elem(k, v);
            if (buyTrans) buyOrders.insert(e);
            else sellOrders.insert(e);
        }

        // INPUT: the number of shares and price for the buy order placed by the trader with the given input id, and the time the buy order was placed
        // POSTCONDITION: a new element for the buy order is added to the respective buy limit-order book for the stock market
        //private void buyAux(int num, double price, int id, Date t) {
        private void buyAux(int num, double price, int id, int t) {
            transAux(num, price, id, t, true);
        }

        // INPUT: the number of shares and price for the sell order placed by the trader with the given input id, and the time the sell order was placed
        // POSTCONDITION: a new element for the sell order is added to the respective sell limit-order book for the stock market
        private void sellAux(int num, double price, int id, int t) {
            transAux(num, price, id, t, false);
        }

        // INPUT: the price and number of shares for the buy order placed by the trader with the given input id
        // POSTCONDITION: a new element for the buy order is added to the respective buy limit-order book for the stock market and a trade is executed if there is a matching sell order already in the limit-order books for the stock market
        public void buy(double price, int num, int id) {
            buyAux(num, price, id, counter++);
            trade();
        }

        // INPUT: the price and number of shares for the sell order placed by the trader with the given input id
        // POSTCONDITION: a new element for the sell order is added to the respective sell limit-order book for the stock market and a trade is executed if there is a matching buy order already in the limit-order books for the stock market
        public void sell(double price, int num, int id) {
            sellAux(num, price, id, counter++);
            trade();
        }

        public void print() {
            printBuy();
            printSell();
            printBank();
        }

        public void printBuy() {
            System.out.println("*** Buy Limit Orders ***");
            buyOrders.printTree(buyOrders.root, 0);
        }

        public void printSell() {
            System.out.println("*** Sell Limit Orders ***");
            sellOrders.printTree(sellOrders.root, 0);
        }

        public void printLedger() {
            System.out.println("*** Transaction Record ***");
            books.print();
        }

        public void printBank() {
            NumberFormat formatter = new DecimalFormat("#0.00");
            System.out.println("*** Bank Profit ***");
            System.out.println("$ " + formatter.format(bank));
        }
    }


    public class Main {
        public static void main(String[] args) {
            String inputFilename = "input.txt";
            String line;

            StockMarket M = new StockMarket();
            // open input file
            try {
                File inputFile = new File(inputFilename);
                BufferedReader ifs = new BufferedReader(new FileReader(inputFile));
                while ((line = ifs.readLine()) != null) {
                    // echo input
                    System.out.println(line.trim());
                    // parse input
                    StringTokenizer st = new StringTokenizer(line);
                    String token;
                    String command = "";
                    // store tokens in an array
                    ArrayList<String> tokens = new ArrayList<String>();
                    while (st.hasMoreTokens()) {
                        // trim whitespace
                        token = st.nextToken().trim();
                        tokens.add(token);
                    }
                    if (tokens.size() > 0) {
                        command = tokens.get(0); // first token is the command
                    }
                    if (tokens.size() == 1) {
                        if (command.equals("print")) {
                            M.print();
                        }
                    }
                    if (tokens.size() > 1) {
                        if (command.equals("buy")) // buy # shares @ specific price, id
                        {
                            M.buy(Float.parseFloat(tokens.get(2)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(3)));
                        }
                        if (command.equals("sell")) // buy # shares @ specific price, id
                        {
                            M.sell(Float.parseFloat(tokens.get(2)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(3)));
                        }
                        if (command.equals("print")) {
                            if (tokens.get(1).equals("buy")) {
                                M.printBuy();
                            }
                            if (tokens.get(1).equals("sell")) {
                                M.printSell();
                            }
                            if (tokens.get(1).equals("ledger")) {
                                M.printLedger();
                            }
                            if (tokens.get(1).equals("bank")) {
                                M.printBank();
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                return;
            }

        }
    }
