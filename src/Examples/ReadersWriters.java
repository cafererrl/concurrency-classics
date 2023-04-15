package Examples;

import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ReadersWriters {
    static int numReaders; // number of Readers
    static int numWriters; // number of Writers
    static int maxNumReaders; // max number of Readers at a time

    static int activeReaderCount = 0;
    static int writeOpCount = 0;
    static int readOpCount = 0;

    static Semaphore readersLock;
    static Semaphore writersLock;
    static Semaphore mutex;
    static Random rand;
    // Constructor
    private ReadersWriters(int numReaders, int numWriters, int maxNumReaders) {
        ReadersWriters.numReaders = numReaders;
        ReadersWriters.numWriters = numWriters;
        ReadersWriters.maxNumReaders = maxNumReaders;

        readersLock = new Semaphore(maxNumReaders, true);
        writersLock = new Semaphore(0, true); // Default writer access to FALSE.
        mutex = new Semaphore(1, true);
        rand = new Random();
        System.out.println("\n>>> Begin operation.");
        // Spin up the Readers and Writers
        for (int i=0; i < numReaders; i++) {
            RW reader = new RW(i, "Reader");
            reader.start();
        }
        for (int i=0; i < numWriters; i++) {
            RW writer = new RW(i, "Writer");
            writer.start();
        }
    }

    static class RW extends Thread {
        int id;
        String type;
        // Constructor
        RW(int id, String type) {
            this.id = id;
            this.type = type;
        }

        void read() {
            System.out.println("Reader "+this.id+" began reading from file.");
        }

        void write() {
            System.out.println("Writer "+this.id+" began writing to file.");
        }

        void yieldTime(int amtCycles) {
            int timeWastedCounter = 0;
            for (int i=0; i < amtCycles; i++) {
                timeWastedCounter++;
            }
        }

        @Override
        public void run() {
            while (writeOpCount < numWriters) {
                if (Objects.equals(type, "Reader")) {
                    // READER LOGIC
                    if (activeReaderCount < maxNumReaders & readOpCount < maxNumReaders) {
                        readersLock.acquireUninterruptibly(); // Permit one Reader to enter file.
                        mutex.acquireUninterruptibly();
                        activeReaderCount++; // Increment active reader count.
                        mutex.release();

                        read();
                        yieldTime(rand.nextInt(3,7));

                        mutex.acquireUninterruptibly();
                        System.out.println("Reader "+this.id+" finished reading the file and left.");
                        readOpCount++; // Increment number of performed read operations.
                        activeReaderCount--; // Decrement active reader count.
                        if (readOpCount == maxNumReaders) {
                            System.out.println("("+readOpCount+") Readers have read. Now allowing writer.\n");
                        }
                        // End the Time of Readers, once N readers have read.
                        if (readOpCount == maxNumReaders) {
                            writersLock.release(); // Grant Writers control.
                            activeReaderCount = 0;
                        }
                        mutex.release();

                    }
                } else if (Objects.equals(type, "Writer")){
                    // WRITER LOGIC
                    // If there are no Readers present, and we have not completed all write operations, then write to file.
                    if (activeReaderCount == 0) {
                        writersLock.acquireUninterruptibly();

                        write();
                        yieldTime(rand.nextInt(3,7));
                        writeOpCount++; // Only 1 Writer at a time, so no mutex needed.
                        System.out.println("Writer "+this.id+" finished writing to the file and left.");
                        System.out.println("Total Writer(s) written to file thus far: ("+writeOpCount+")\n");

                        if (writeOpCount == numWriters) {
                            System.out.println(">>> Finished operation.");
                            System.exit(0);
                        }
                        // Once done writing, return Semaphore permits to Reader and reset # read operations performed.
                        readersLock.release(readOpCount);
                        readOpCount = 0;
                    }
                }
            }
        }
    }

    public static void main() {
        // Init to nonsense value.
        int numReaders = -1; // number of Readers
        int numWriters = -1; // number of Writers
        int maxNumReaders = -1; // max number of Readers at a time

        do {
            System.out.println("How many Readers (integer from 2-10000000)?:");
            Scanner userIn = new Scanner(System.in);
            try {
                numReaders = userIn.nextInt();  // Read user input
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer. Restart the program.");
                System.exit(0);
            }
        } while (numReaders > 10000000 || numReaders <= 0);

        do {
            System.out.println("How many Writers (integer from 1-10000000)?:");
            Scanner userIn = new Scanner(System.in);
            try {
                numWriters = userIn.nextInt();  // Read user input
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer. Restart the program.");
                System.exit(0);
            }
        } while (numWriters > 10000000 || numWriters <= 0);

        do {
            System.out.println("How many Readers can read at a time (integer, L.T.E to # of Readers, up to 10000000)?:");
            Scanner userIn = new Scanner(System.in);
            try {
                maxNumReaders = userIn.nextInt();  // Read user input
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer. Restart the program.");
                System.exit(0);
            }
        } while (maxNumReaders > 10000000 || maxNumReaders > numReaders);

        // Constructor runs simulation.
        new ReadersWriters(numReaders, numWriters, maxNumReaders);
    }
}
