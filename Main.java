import java.util.*;

public class main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String menu = """
                +-----------------------+
                |        MENU           |
                |-----------------------|
                | 1. Add Patient        |
                | 2. View Records       |
                | 3. Exit               |
                +-----------------------+
                """;
        System.out.println(menu);
        System.out.print("Select an option: ");
        int choice = input.nextInt();   
        switch (choice) {
            case 1:
                System.out.println("Adding a new patient...");
                // Code to add patient
                break;
            case 2:
                System.out.println("Viewing records...");
                // Code to view records
                break;
            case 3:
                System.out.println("Exiting...");
                // Code to exit
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }


    }

}
