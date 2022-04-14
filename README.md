# Synchrosity

Running Instrucitions:

    To Compile: 'javac *.java' or javac Combinator.java

    To Run: java Combinator


Problem 1: || 
-------------
For the first problem, the mistake in which the error could have occurred is with the removal and addition operations.
We know that the members are all operating in tandem, but they are not communicating with each other the step that they
are in. So if say we were adding a current node after position 4, and we are concurrently removing a node at position 5.
Since the two 'workers' here aren't communicating, the order of operations could change the outcome for scenarios that 
require two operations to be done. For example:
    If we take the Adding present operation and observe that it has two seperate operations that must occur. It has to 
    set the predecessor pointing to the new node, and set the new node pointing to the next node. We have two ways to 
    order this. 
        First we have the instance where we first add to the predecessor. In this scenario, when adding two nodes, the
    first one could link itself to the predecessor, and in the time prior to linking to the next node another adding 
    operation could be done by another. If the other person were to add in, the second add node would set the first persons
    node to be the second threads new node. But upon the first person updating the node to next, it will unintentionally
    cut out the second persons node, leaving it to where the node is no longer in the chain. Illustrated below:

    |1| -> |4| -> |8| -> |12|
    ______________________________________________________________________

    |1| -> |4|    |8| -> |12|       ----        |1| -> |4|    |8| -> |12|
            \-> |5|                 ----                \-> |5| -> |7|
    ______________________________________________________________________
    |1| -> |4| -> |5| -> |8| -> |12|
                    \-x->|7|

What we have to create is a lock-free linked list

Since we want to take advantage of the lock free solutionn, what we must do is use the fact that there exists atomic operations
that we can perform to ensure that updating things is not turned into two steps. Rather than doing two seperate operations
giving any other thread time to update a value. And we also want to use validation, which would work for the servants as well
since they are smart sentient creatures. So in our linked list we use CAS, which is Compare and Set. So we can verify that the
node ahead of us is still the same as when we first reached that node, and if so. Update the values.
    In addition, for deletions we can manifest the use of logical and physical deletions to indicate that the the nodes will be
removed shortly, that way during the timeframe, no other node will attempt to modify to that one node. But in this special case, for
the servants since we know that the presents will all be added , and EVERY present that is added will be removed, we can just pull from
the head. This still provides contention, as many nodes may attempt to remove. But we can still use validation, checking if the node that
is following the sentinel is still maintained. If not, then another thread changed the start, thus we must just try again until the nodes
are the same. 
    For addition, we must just use atomic operations and validation as well. Knowing that two nodes may try to add onto each other at the
same time, we can see that we must use validation. So we have to check that the node that follows our predecessor hasn't changed, only
after then can we proceed to add in our node. This must be atomic, so that other threads couldn't have changed the values during our checks.
But setting the node's next pointer must also be atomic. So we also use the Compare And Set operation here to ensure that in one computer
step the node's are checked and updated.
    Since we are choosing random values from a bag, there was another complication of simulating randomness in a sequential manner. So what
was done to simulate this is the use of an atomic getAndIncrement() to have a counter on which number present to be added. This ensured that
we got unique values that persisted in the thread and that they aren't being accessed by another. We get these values from an atomic integer
array that we created in a sequential manner using random numbers, and a shrinking number array that swaps values from middle to end, to not
have to resize.


Problem 2: ||
------------
Here what we want to do is to use the concept of a Queue. But what is a Queue?

Normally a Queue is represented via a heap as a tree structure. But this will not work. Heaps have added operations such as perculating that
occur when the order is disturbed. In a concurrent solution, that added time is detrimental. But since we know that there will only ever be 
a certain number of readings, instead we can just use a simple list to maintain that.

Perfect! This is just as what we used for the present collection. But the caveat is that now we have to be able to modify the add Function to
only add values that are greater than the minimum. This is actually easy to implement. By using the fact that the minimum values will always
succeed the start sentinel, we can check if our additions location getter function returned a value that contained the sentinel as the preceding
 value; if so, we just return false. 
    This will ensure we are only adding necessary values. Now we have to ensure that we are only maintaining 5 total nodes, since that is the shared
memory space. So what we do is we have to remove the minimum node for every node that we add. This is easy, since we have an ordered list, we just 
remove whatever follows the sentinel node, but we must use validation as always to make sure that other threads haven't already done this.
    This solves our problem for maintaining the maximum nodes. For Minimum nodes, we store the values the same, but with negative values.

    For generating reports, we just have to keep the numbers static long enough for the println function to run, so we will have to depend on
the .join() method to wait for the 'sensor' threads to finish recording their first 10 min prior to doing anything else. Then we can get the 
greatest value for the two, this will help us find the difference. Values are only added if they beat the previous at being farther as bigger 
values, so anytime the farthestmost value is changed, we will update the difference. What we could do here is use a tail pointer, since a tail
pointer will only ever go in the farthest direction, then we can just return the value at the pointer. For updating the tail, we would just have
to ensure that we keep traversing the node until the next is null otherwise we could have another node succinctly add a value. 
