use std::thread;
// four threads
static NUM_THREADS: u8 = 4;
static NUM_PRESENTS: u32 = 500000;

pub fn give() {
    let mut t_list: Vec<thread::JoinHandle<()>> = Vec::new();

    for i in 0..NUM_THREADS {
        t_list.push(std::thread::spawn(move || {
            println!("test {}", i);
        }));
    }

    // finalize
    for z in t_list {
        let _ = z.join();
    }
}

/*
// generate random numbers to add to list
.. linked list must be in ascending order
.. four threads equivalent of 4 servers
..


*/
