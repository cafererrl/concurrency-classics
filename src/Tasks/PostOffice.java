import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class PostOffice {
    static Random rand = new Random();
    static int numMailers; // N - number of mailers
    static int numInboxCapacity; // S - number of messages a mailer's inbox can hold
    static int numTotalMessages; // M - number of total messages to be sent
    static AtomicInteger mailSerial = new AtomicInteger(0);
    static AtomicInteger numMessagesSent = new AtomicInteger(0); // TODO make regular int and add mutex around this value

    static MailPerson[] directory;
    static String[] chatter = new String[] {
            "I think therefore I am.",
            "Deus ex machina.",
            "Dogs chase cats.",
            "I can see the truth.",
            "Protect your child.",
            "Prove your ex wrong.",
            "Leave tonal space for melody.",
            "We can see a movie.",
            "We can see the moon.",
            "Once upon a city far, far away."
    };

    // Constructor
    private PostOffice(int numMailers, int numMaxMessages, int numTotalMessages) {
        PostOffice.numMailers = numMailers;
        PostOffice.numInboxCapacity = numMaxMessages;
        PostOffice.numTotalMessages = numTotalMessages;
        directory = new MailPerson[numMailers];
        System.out.println();
        for (int i=0; i < directory.length; i++) {
            MailPerson mailer = directory[i] = new MailPerson(numMaxMessages, numTotalMessages);
            mailer.start();
        }
    }

    static class MailPerson extends Thread {
        final int id;
        Stack<AbstractMap.SimpleEntry<Integer, String>> inbox = new Stack<>();
        AbstractMap.SimpleEntry<Integer, String> outgoingMsg;
        Semaphore inboxCtrlSem = new Semaphore(PostOffice.numInboxCapacity, true);
        // Constructor
        private MailPerson(int numInboxCapacity, int numTotalMessages) {
            this.id = mailSerial.getAndIncrement();
            PostOffice.numInboxCapacity = numInboxCapacity; // TODO Delete ME
            PostOffice.numTotalMessages = numTotalMessages; // TODO Delete ME
        }

        void sendMessage() {
            MailPerson recipient = directory[outgoingMsg.getKey()];
            String message = outgoingMsg.getValue();
            // If there is a free space in the Mailer's slots present, then acquire Sem and send msg.
            System.out.println("\t[Mailer "+this.id+"] is sending to [Mailer "+recipient.id+"]...");
            recipient.inboxCtrlSem.acquireUninterruptibly();
            // Place my sender id and message in recipient's inbox.
            AbstractMap.SimpleEntry<Integer, String> inboxData = new AbstractMap.SimpleEntry<>(this.id, message);
            recipient.inbox.push(inboxData);
            numMessagesSent.getAndIncrement();
            System.out.println("\t[Mailer "+this.id+"] SENT message '"+message+"' to [Mailer "+recipient.id+"].");
        }

        public int getRandomRecipient() {
            int assignedId = rand.nextInt(PostOffice.numMailers); // mailer ids
            if (assignedId < this.id) {
                return assignedId;
            } else if (assignedId == PostOffice.numMailers-1) { // if last mailer, assign smaller id value
                return --assignedId;
            } else {
                return ++assignedId;
            }
        }

        void composeMessage() {
            int recipientId = getRandomRecipient();
            int randMessageId = rand.nextInt(PostOffice.chatter.length);
            outgoingMsg = new AbstractMap.SimpleEntry<Integer, String>(recipientId, chatter[randMessageId]);
            System.out.println("\t[Mailer "+this.id+"] composed a DRAFT for [Mailer "+recipientId+"].");
        }

        void yieldMailer(int amtCycles) {
            int timeWaster=0;
            for (int i=0; i < amtCycles; i++) {
                timeWaster++;
            }
        }

        void checkInbox() {
            if (inbox.size() < 1) {
                System.out.println("\tMailer ["+this.id+"]'s inbox is empty. No messages.");
            } else {
                while (inbox.size() > 0) {
                    AbstractMap.SimpleEntry<Integer, String> msgData = inbox.pop();
                    System.out.println("\t[Mailer "+this.id+"] has mail from [Mailer "+msgData.getKey()+"]: '"+msgData.getValue()+"'");
                    inboxCtrlSem.release();
                    yieldMailer(1);
                }
            }
        }

        @Override
        public void run() {
            // Perform mail-checking algorithm from specification.
            while (numMessagesSent.get() < numTotalMessages) {
                System.out.println(">>> [Mailer "+this.id+"] has ENTERED the mailroom.");
                checkInbox();
                composeMessage();
                sendMessage();
                System.out.println("← [Mailer "+this.id+"] has LEFT the mailroom.");
                yieldMailer(rand.nextInt(3,7));
            }
            System.out.println("numMessagesSent: "+numMessagesSent);
            System.exit(0);
        }
    }


    // Driver
    public static void main(String[] args) {
        // Init to nonsense value.
        int numMailers = -1; // N
        int numMaxMessages = -1; // S
        int numTotalMessages = -1; // M

        do {
            System.out.println("How many mailers will come through the post office (integer from 2-10000000)?:");
            Scanner userIn = new Scanner(System.in);
            try {
                numMailers = userIn.nextInt();  // Read user input
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer. Restart the program.");
                System.exit(0);
            }
        } while (numMailers > 10000000 || numMailers <= 1);

        do {
            System.out.println("How many messages will each mailbox hold (integer from 1-10000000)?:");
            Scanner userIn = new Scanner(System.in);
            try {
                numMaxMessages = userIn.nextInt();  // Read user input
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer. Restart the program.");
                System.exit(0);
            }
        } while (numMaxMessages > 10000000 || numMaxMessages <= 0);

        do {
            System.out.println("How many messages total will the mailers send through the post office (integer from 1-10000000)?:");
            Scanner userIn = new Scanner(System.in);
            try {
                numTotalMessages = userIn.nextInt();  // Read user input
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer. Restart the program.");
                System.exit(0);
            }
        } while (numTotalMessages > 10000000 || numTotalMessages <= 0);

        // Constructor runs simulation.
        new PostOffice(numMailers, numMaxMessages, numTotalMessages);

    }
}
