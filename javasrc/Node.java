import java.util.concurrent.atomic.AtomicMarkableReference;

class Node
{
	AtomicMarkableReference<Node> next;
	int val;
	
	Node(int v)
	{
		this.val = v;
		this.next = new AtomicMarkableReference<Node>(null, false);
	}

	/// set new node reference
	public synchronized void setNext(Node next, Node newNext)
	{
		this.next = new AtomicMarkableReference<Node>(newNext, false);
	}
}