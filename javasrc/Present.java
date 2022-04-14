import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicMarkableReference;



class List
{
	Node start;
	
	List()
	{
		this.start = new Node(-1);
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
		
		// keep going until add is succesful
		while (true)
		{
			prev = this.start;
			curr = prev.next.getReference();
			while (true)
			{
				// courrect spot found, return
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
					// if failed to remove, retry
					if (!snip)	return getLoc(x);

					curr = following;
					following = curr.next.get(marked);
				}

				// found correct value, return
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

	/// Done. Will add node based on the value that is has. ordered into the linkedlist
	public  void addNode(int x)
	{
		Node[] xy = getLoc(x);
		Node n = new Node(x);
		Node curr = xy[0], prev = xy[1];
		n.next = new AtomicMarkableReference<Node>(curr, false);
		// end of list, just place
		if (curr == null)
		{
			prev.next = new AtomicMarkableReference<Node>(n, false);
			return;
		}
		
		
		// was curr, change to new N
		if (prev.next.compareAndSet(curr, n, false, false) )
			return;
		else
			addNode(x);
		
		//prev.next = n;
		//n.getReference().next = t;
		
	}

	/// Remove the firstish elements in atomic fashion
	public boolean remove()
	{	
		Node prev = this.start, curr = prev.next.getReference();

		if (curr == null)
			return false;

		// only update if the values stayed the same otherwise, failed remove
		return prev.next.compareAndSet(curr, curr.next.getReference(), false, false);
	}

	/// check if we have a value without locking. and use marking to see if node is contained
	/// go in ordered manner
	public boolean hasValue(int x)
	{
		Node t = this.start;

		// simply traverse
		while(t != null)
		{
			// if found, return true if not marked
			if (t.val == x)
				return !t.next.isMarked();

			t = t.next.getReference();
		}

		return false;
	}

	/// simply print the list
	public void showList()
	{
		Node t = this.start;
		// ignore the firsst sentinel, traverse list and print
		while (t != null)
		{
			System.out.println(t.val);
			if (t.next != null)
				t = t.next.getReference();
			else
				t = null;
		}
	}
}

public class Present extends Thread
{
	static AtomicIntegerArray bag;
	static AtomicInteger p;
	static int NUM_PRESENTS = 500_000;
	static int NUM_THREADS = 4;
	static List chain;
	
	/// create the bag which holds the presents to get
	/// will simulate the randomization in this form
	public static AtomicIntegerArray createBag()
	{
		AtomicIntegerArray res = new AtomicIntegerArray(NUM_PRESENTS);
		int[] nums = new int[NUM_PRESENTS];
		int pos, temp, used = 0;

		for (int i = 0; i < NUM_PRESENTS;i++)
			nums[i] = i;

		// pull a random position every time
		for (int i = 0; i < NUM_PRESENTS; i++)
		{
			// get random position within our array
			pos = (int)(Math.random() * NUM_PRESENTS) % (NUM_PRESENTS - used);
			used++;

			// swap with last element
			temp = nums[NUM_PRESENTS - used];
			res.set(i, nums[pos]);
			nums[pos] = temp;
		}

		return res;
	}

	// method for each thread
	public void run()
	{
		int z;
		boolean alternate = true;

		while (true)
		{
			// chance of minatour searching for a present
			if (Math.random() > .9)
			{
				Present.chain.hasValue( (int)(Math.random() *Present.NUM_PRESENTS) );
				continue;
			}

			alternate = !alternate;

			// alternate between adding and removing with boolean
			if ( alternate )
			{
				// atomically get the random value
				z = Present.p.getAndIncrement();
				// if all are added, continue removing until chain is empty
				if (z >= NUM_PRESENTS)
					if (Present.chain.start.next.getReference() == null)
						return;
					else
						continue;
				
				Present.chain.addNode(z);
			}
			else
			{
				//remove
				Present.chain.remove();
			}


			//System.out.println( z + " " + Present.bag.get(z));
		}
	}

	public static void main(String[] args)
	{
		Present[] pool = new Present[NUM_THREADS];
		bag = Present.createBag();
		p = new AtomicInteger(0);
		chain = new List();

		System.out.println("Beginning to thank and present!");
		long init = System.currentTimeMillis();
		// create threads
		for (int i = 0; i < NUM_THREADS; i++)
		{
			pool[i] = new Present();
			pool[i].start();
		}

		for (int i = 0; i < NUM_THREADS; i++)
			try
			{ pool[i].join(); }
			catch (Exception e)
			{ System.out.println(e); }

		
		long end = System.currentTimeMillis();
		System.out.println("Finished giving thanks in "+ (end - init) +" ms");
		//chain.addNode(100);
		//Present.chain.showList();
		//System.out.println(chain.hasValue(100));
	}
}