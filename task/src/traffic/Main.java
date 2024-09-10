package traffic;
import java.io.IOException;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to the traffic management system!");
    System.out.println("Input the number of roads: ");
    int maxRoadsCount;
    while (true) {
      try {
        maxRoadsCount = scanner.nextInt();
        scanner.nextLine();
        if (maxRoadsCount < 1) {
          System.out.println("Error! Incorrect Input. Try again: ");
        } else {
          break;
        }
      } catch (Exception e) {
        System.out.println("Error! Incorrect Input. Try again: ");
        scanner.nextLine();
      }
    }

    System.out.println("Input the interval: ");
    int interval;
    while (true) {
      try {
        interval = scanner.nextInt();
        scanner.nextLine();
        if (interval < 1) {
          System.out.println("Error! Incorrect Input. Try again: ");
        } else {
          break;
        }
      } catch (Exception e) {
        System.out.println("Error! Incorrect Input. Try again: ");
        scanner.nextLine();
      }
    }
    // --- starting time

    RoadsThread roadsThread = new RoadsThread(interval);
    roadsThread.start();

    QueueThread queueThread = new QueueThread(interval, roadsThread, maxRoadsCount);
    queueThread.start();
    queueThread.setName("QueueThread");

    while (true) {
      printMenu();
      String action = scanner.nextLine();

      switch (action) {
        case "1" -> addRoad(scanner, queueThread, maxRoadsCount, roadsThread);
        case "2" -> deleteRoad(roadsThread);
        case "3" -> {
          queueThread.changeState("system");
          openSystem(queueThread, scanner);
        }
        case "0" -> {
          System.out.println("Bye!");
          queueThread.changeState("quit");
          roadsThread.setState("quit");
          return;
        }
        default -> System.out.print("Incorrect option\n");
      }

        scanner.nextLine();

      cleanDisplay();
    }
  }

    public static void cleanDisplay() {
      try {
        var clearCommand = System.getProperty("os.name").contains("Windows")
                ? new ProcessBuilder("cmd", "/c", "cls")
                : new ProcessBuilder("clear");
        clearCommand.inheritIO().start().waitFor();
      } catch (IOException | InterruptedException e) {
      }
    }
  public static void addRoad(Scanner scanner, QueueThread queueThread, int maxRoadsCount, RoadsThread roadsThread) {
    System.out.print("Input road name: ");
    String roadName = scanner.nextLine();

    if (roadsThread.roadsCount + 1 > maxRoadsCount){
      System.out.println("queue is full");
    } else{
      queueThread.addRoad(roadName);
      System.out.println(roadsThread.roads.getLast() + " added!");
    }
  }

  public static void deleteRoad(RoadsThread roadsThread) {
    if (roadsThread.roadsCount == 0){
      System.out.println("queue is empty");
    } else{
      System.out.println(roadsThread.roads.getFirst() + " deleted!");
      roadsThread.roads.remove(roadsThread.roads.getFirst());
      roadsThread.roadsCount--;
      roadsThread.isOpen.remove(roadsThread.isOpen.getFirst());
      roadsThread.roadsTime.remove(roadsThread.roadsTime.getFirst());
    }
  }

  public static void openSystem(QueueThread queueThread, Scanner scanner) {

    if (scanner.hasNextLine()){
      queueThread.changeState("idle");
    }
  }

  public static void printMenu() {
    System.out.printf("""
            Menu:
            1. Add
            2. Delete
            3. System
            0. Quit
            """);
  }
}