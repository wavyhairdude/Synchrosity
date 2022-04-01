# Synchrosity

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

- 3 actions
1. create new node, 
    connect new node after predecessor
        connect new node to next
2. remove node , {linearizable @ predecessor = next} 
    then connect predecessor to next
3. check whether the node exists in linkedlist

What we have to create is a wait-free lock-free linked list


Problem 2: ||
------------
Shared memory space. Queue?
