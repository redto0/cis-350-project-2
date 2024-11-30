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
      # type flag); or NULL if w is NULL
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
        while (w.parent.left != w){// check to see if w is the right child of this parent
            if(w.parent == this.root) return w.parent;
            w = w.parent;

        }
        // return youngestAncestorType(w,false);
        return w; // if w is the right child of its parent then return w;

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
    public Object CalulateDept(int n){
        int dept = 0;
        if( n == 0 ) return null;
        int bound = 0;
        while (bound < n ){
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
        // NAME: <Aleaxander Boccaccio>
        // Your code here
        // right child is greater than root

        // Node(Elem e, Node l, Node r, Node p) {
        Node w = new Node(e, null, null, p);
        if(p.left != null){
            p.right = w;
        } else {
            p.left = w;
        }
        this.lastNode = w;
        this.n++;
        return w;
    }

    // OUTPUT: the element of the last node of the complete BT (to be removed); NULL if the tree is empty
    // PRECONDITION:
    // POSTCONDITION:
    public Elem remove() {
        // NAME: <your name here>
        // Your code here
        Node out = this.root;
        Elem outElem = this.root.elem;
        // idc to find if this exist as a function lol
        // assgin the last element to null
        if(this.lastNode.parent.right == null) {
            this.lastNode.left = null;
        } else {
            this.lastNode.parent.right = null;
        }
        // assgin the root's children to the last node
        if(this.root.left != null) {
            // make sure that right isn't null
            this.lastNode.left = this.root.left;
        }
        if (this.root.right != null ){
            // make sure that left isn't null
            this.lastNode.right = this.root.right;
        }
        // assgin the root to the last node
        this.root = this.lastNode;
        // Root is gone remove adjust count
        this.n--;
        // assign the next last node
        this.lastNode = this.getNewLastNode();
        return outElem;
    }

    // OUTPUT: the node in the complete BT where any new node inserted would be placed
    // PRECONDITION:
    // POSTCONDITION:
    private Node getParentOfNewLastNode() {
        // NAME: <Alexander Boccaccio>
        // Your code here
        // return where the next new node should be placed
        // so we place in the last spot

        //assuing this.last node is not the root it seems...
        if(this.lastNode == null) this.lastNode = this.root;

        Node w = this.lastNode;
        if(this.lastNode.parent.left == this.lastNode){
            // then the next spot is the right node of the last node
            return this.lastNode.parent;
        }
        // this is if the last Node happens to be the last Node in which
        if ( this.n == this.CalulateTotal( (int) this.CalulateDept(n) ) ){
            // this is the last node of its branch
            Node p =this.lastLeftDescendant( this.root );
            return p;
        } else {
            // if (this.lastNode)
            Node p = this.firstLeftAncestor(w);
            p = p.right;
            while (p.left != null){
                p = p.left;
            }
            return p;
        }

        /*
        int dept = (int) this.CalulateDept(this.n);
        Node Start = this.root;
        dept--;
        int n = this.n - CalulateTotal(dept - 1);
        double deptAdment;
        while (dept > 1){
            dept--;
            deptAdment = (double) CalulateTotalAtLevel(dept) / 2;
            if( n > deptAdment) {

            }
        }
        // we do math lol
        // didnt like this
        //this.n
        return new Node();
        */
    }

    // because sometimes math can be hard for people
    public int CalulateTotal(int numb){
        return (2 ^ (numb + 1)) - 1;
    }
    public int CalulateTotalAtLevel(int numb){
        return (2 ^ (numb));
    }

    // OUTPUT: the node in the BT that would become the last node of the complete BT should the last node be removed
    // PRECONDITION:    has the node been removed yet? probably so last node has a null element but a valid pointer in the structure
    // POSTCONDITION:   we return last node (booooo) but it's ok if we need to still delete the other node
    //                  it can have a global pointer before this mess
    private Node getNewLastNode() {
        // NAME: <Alexander Boccaccio>
        // Your code here

        // this is the one to remove, and remember, no pointers!


        int Dept = (int) CalulateDept(this.n);
        if (this.root == null){
            return null;
        } if (this.lastNode == null){
            return this.root;
        } else if ( this.n == this.CalulateTotal( (int) this.CalulateDept(n)) + 1 ) {
            // then can we assume that this is the last node of its level, and we need to step back to the last one
            Node p = this.root;
            while (p.right != null){
                p = p.right;
            }
            return p;
        }else if ( this.lastNode == this.lastNode.parent.right){
            // we find that p is the right child so we can pass the trouch to the left child
            return this.lastNode.parent.left;
        }else{
            // we manually find it (normal case) and p is a left child
            // we find the first node that is left of p
            Node p = this.firstLeftAncestor(this.lastNode);
            // then we step left to go down the other path;
            p = p.left;
            // then we go as far right as possible 
            while(p.right != null){

                // I know there is a function already, but I don't like it, and it isn't required to do so
                p = p.right;
            }

        }
            // this left is annoying
        }
        //return new Node();
    }

}

// Heap data-structure implementation of a priority queue ADT
class Heap extends CompleteBT {
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
        // NAME: <your name here>
        // NAME: <Alexander Boccaccio>
        // Your code here
        return this.root.elem;
    }

    // PRECONDITION:
    // POSTCONDITION:
    public void removeMin() {
        // NAME: <your name here>
        // Your code here

    }

    // PRECONDITION:
    // POSTCONDITION:
    private void upHeapBubbling() {
        // NAME: <your name here>
        // Your code here

    }

    // PRECONDITION:
    // POSTCONDITION:
    private void downHeapBubbling() {
        // NAME: <your name here>
        // Your code here

    }
}

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
        public Record()  {
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

    private HashMap<Integer,Record> book;

    public Ledger() {
        book = new HashMap<Integer,Record>();
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
            book.put(id, new Record(id,0.0,0));
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
        trans(e,true);
    }

    // INPUT: an element e
    // POSTCONDITION: a sell transaction is inserted into the ledger/book for the corresponding trader, and the trader record is updated to reduce its holdings by the number of shares sold and increase the trader's balance by the amount of money the trader obtained for the shares; if this is the first transaction for the trader, a new record is created, and properly initialized
    public void sell(Elem e) {
        trans(e,false);
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
            numRemain = numBuy-numSell;
            // add leftover buys as a new order
            if (numRemain > 0) buyAux(numRemain, priceBuy, idBuy, timeBuy);
        }
        else {
            numTrade = numBuy;
            buyTrade = buyLimitOrder;
            k = new Key(priceSell,timeSell);
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
    public static void main(String[] args)
    {
        String inputFilename = "input.txt";
        String line;

        StockMarket M = new StockMarket();
        // open input file
        try
        {
            File inputFile = new File(inputFilename);
            BufferedReader ifs = new BufferedReader(new FileReader(inputFile));
            while ((line = ifs.readLine())!=null)
            {
                // echo input
                System.out.println(line.trim());
                // parse input
                StringTokenizer st = new StringTokenizer(line);
                String token;
                String command = "";
                // store tokens in an array
                ArrayList<String> tokens = new ArrayList<String>();
                while (st.hasMoreTokens())
                {
                    // trim whitespace
                    token = st.nextToken().trim();
                    tokens.add(token);
                }
                if (tokens.size() > 0)
                {
                    command = tokens.get(0); // first token is the command
                }
                if (tokens.size() == 1)
                {
                    if (command.equals("print"))
                    {
                        M.print();
                    }
                }
                if (tokens.size() > 1)
                {
                    if (command.equals("buy")) // buy # shares @ specific price, id
                    {
                        M.buy(Float.parseFloat(tokens.get(2)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(3)));
                    }
                    if (command.equals("sell")) // buy # shares @ specific price, id
                    {
                        M.sell(Float.parseFloat(tokens.get(2)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(3)));
                    }
                    if (command.equals("print"))
                    {
                        if (tokens.get(1).equals("buy"))
                        {
                            M.printBuy();
                        }
                        if (tokens.get(1).equals("sell"))
                        {
                            M.printSell();
                        }
                        if (tokens.get(1).equals("ledger"))
                        {
                            M.printLedger();
                        }
                        if (tokens.get(1).equals("bank"))
                        {
                            M.printBank();
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            return;
        }
        catch (IOException e)
        {
            return;
        }

    }
}
