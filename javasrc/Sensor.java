import java.util.concurrent.atomic.AtomicMarkableReference;


class Queue
{
    Node start;
    Queue()
    {
        // initialize and set to minimum value
        this.start = new Node(-500);
        Node n = start;
        // create placeholder nodes with min values
        // these will always get replaced
		for (int i = 0; i < 5; i++)
		{
			n.next = new AtomicMarkableReference<Node>(new Node(-500), false);
			n = n.next.getReference();
		}

		//this.printQ();
    }

    /// Checks to find the spot in which we can continue getting the values
	/// for adding and removing, physically deletes nodes
	private Node[] getLoc(int x)
	{
		Node[] t = new Node[2];
		Node curr, prev, following;
		prev = this.start;
		curr = prev.next.getReference();
		boolean snip;
		boolean[] marked = new boolean[1];
		
		while (true)
		{
			prev = this.start;
			curr = prev.next.getReference();
			while (true)
			{
				if (curr == null)
				{
					t[0] = curr;
					t[1] = prev;
					return t;
				}
				following = curr.next.get(marked);
				// check to see if the node is marked for deletion
				while (marked[0])
				{
					snip = prev.next.compareAndSet(curr, following, false, false);
					// if failed to cut, retry
					if (!snip)	return getLoc(x);

					curr = following;
					following = curr.next.get(marked);
				}

				if (curr.val >= x)
				{
					t[0] = curr;
					t[1] = prev;
					return t;
				}

				prev = curr;
				curr = following;
			}
		}
	}

    /// add node at correct position, if posssible, otherwise ignore
    /// if adding a node, remove the next lowest
    public synchronized boolean addNode(int temp)
    {
        Node n = new Node(temp);


        while (true)
        {
            Node[] xy = getLoc(temp);
            Node curr = xy[0], prev = xy[1];

            if (curr != null && (curr.val == temp ))
                return false;

            n.next = new AtomicMarkableReference<Node>(curr, false);
            if (prev.next.compareAndSet(curr, n, false, false))
            {
                this.start = this.start.next.getReference();
                return true;
            }
        }
    }

    /// simply print the queue in the order it is placed
    public void printQ(boolean reverse)
    {

        Node c = this.start.next.getReference();
        while (c != null)
        {
            if (reverse)
                System.out.print(c.val * -1 + " ");
            else
                System.out.print(c.val + " ");
            c = c.next.getReference();
        }

        System.out.println();
    }

    // order N operation to get the last value
    public int getTail(boolean reversed)
    {
        int c = 500;
        
        Node[] xy = getLoc(c);
        int z = xy[1].val;
        return (reversed? -1: 1) * z;
    }

    

    /// remove the first element in the list
    public synchronized void removeFirst()
    {
        //Node prev = this.start, curr = prev.next.getReference();

		// only update if the values stayed the same otherwise, failed remove
		//prev.next.compareAndSet(curr, curr.next.getReference(), false, false);
        this.start = this.start.next.getReference();
    }

}


public class Sensor extends Thread
{
    static Queue inc, dec;
    static int numSensors = 8;
    static int INTERVAL = 10;
    static boolean DEBUG = false;
    static boolean PRINT_REPORTS = false;
    // interval in ms

    public void run()
    {   
        if (DEBUG)
            System.out.println("thread started + " + this.getId());

        // once per minute, record time, for total of 10 minutes
        for (int i = 0; i < 10; i++)
        {
            // temp ranges from -100 -> 70F
            int temp = (int)(Math.random() * 170) + -100;

            // add node (removing will be built in), negative for the decreasing order
            inc.addNode(temp);
            dec.addNode(-1 * temp);

            // wait one user designated minute, default is 100ms
            try { Thread.sleep(INTERVAL);}
            catch (Exception e) { System.out.println("Error:" + e); };
        }

        if (DEBUG)
                System.out.println("Thread finished " + this.getId() );

    }

    public static void main(String[] args) {
        inc = new Queue();
        dec = new Queue();
        
        Sensor[] pool = new Sensor[numSensors];
        int maxDiff = 0, maxInterval = 0;

        System.out.println("Beginning the simulation!");
        for (int j = 0; j <= 6; j++)
        {
            // start the threads monitoring for 10 minutes each
            for (int i = 0; i < numSensors; i++)
            {
                pool[i] = new Sensor();
                pool[i].start();
            }

            // get the largest value, dont modify.
            for (int i = 0; i < numSensors; i++)
                try {pool[i].join();} catch (Exception e) {;}

            // check if this is the instance where the diff is the greatest
            //int diff = 0;
            int diff = Math.abs(inc.getTail(false) - dec.getTail(true));
            if (diff > maxDiff)
            {
                maxDiff = diff;
                maxInterval = j;
            }
            
            if (PRINT_REPORTS)
            { 
                System.out.println("Report for "+(10 * j)+" mins:");
                System.out.print("Maximums: ");
                inc.printQ(false);
                System.out.print("Minimums: ");
                dec.printQ(true);
                System.out.println();
            }
        }


        System.out.println("10 min interval with greatest diff = " + (maxInterval * 10) + "mins in");

        System.out.println("Final Report:");
        System.out.print("Maximums: ");
        inc.printQ(false);
        System.out.print("Minimums: ");
        dec.printQ(true);
        System.out.println();
        
        System.out.println("Namaste, Everything has been sensed");


    }
}