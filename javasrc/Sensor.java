import java.util.concurrent.atomic.AtomicMarkableReference;

class Queue
{
    Node start, end;
    Queue()
    {
        this.start = new Node(-500);
    }

    /// Done. Checks to find the spot in which we can continue getting the values
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

				if (curr.val > x)
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

    // TODO
    public boolean addNode(int temp)
    {
        // check if the node is less than the queue
        Node[] xy = this.getLoc(temp);
        Node curr = xy[0], prev = xy[1];

        

        return true;
    }
}


public class Sensor extends Thread
{
    static Queue one;
    static int numSensors = 8;
    static int INTERVAL = 100;

    public void run()
    {   
        for (int i = 0; i < 60; i++)
        {
            // temp ranges from -100 -> 70F
            int temp = (int)(Math.random() * 170) + -100;

            //TODO add node
            one.addNode(temp);

            try { Thread.sleep(INTERVAL);}
            catch (Exception e) { System.out.println("Error:" + e); };
        }

    }

    public static void main(String[] args) {
        one = new Queue();
        Sensor[] pool = new Sensor[numSensors];
        for (int i = 0; i < numSensors; i++)
        {
            pool[i] = new Sensor();
            pool[i].start();
        }

        for (int i = 0; i < numSensors; i++)
            try {pool[i].join();} catch (Exception e) {;}

        System.out.println("Everything has been sensed");

    }
}