import Tasks.*;

public class Main {
    public static void main(String[] args) {
        // Check number of args.
        if (args.length <= 0) {
            System.out.println("Cannot have 0 args.");
            System.exit(0);
        } else if (args.length <= 1) {
            System.out.println("More arguments required.");
            System.exit(0);
        } else if (args.length > 2) {
            System.out.println("Too many arguments. Expected: (2) args.");
            System.exit(0);
        } else { // Number of args is correct.
            if (args[0].equals("-A")) { // arg1 is correct.
                switch (args[1]) {
                    case "1" -> DiningPhilosophers.main();
                    case "2" -> PostOffice.main();
                    case "3" -> ReadersWriters.main();
                    default -> {
                        // Invalid input was entered.
                        System.out.format("\"%s\" is not a valid option.\n--Options are 1, 2, or 3.\n", args[1]);
                        System.exit(0);
                    }
                }

            } else { // Incorrect arg1.
                System.out.format("\"%s\" is not a recognized argument, please try again.\n--Recognized args: \"-S\"\n",
                        args[1]);
                System.exit(0);
            }
        }
    }
}
