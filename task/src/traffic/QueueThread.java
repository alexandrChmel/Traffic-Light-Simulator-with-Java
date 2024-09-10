package traffic;

import com.sun.source.doctree.SystemPropertyTree;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class QueueThread extends RoadsThread{
    String state = "idle";
    public void changeState(String newState) {
        this.state = newState;
    }
    int runTime = 0;
    long startTime;
    RoadsThread roadsThread;
    int maxRoadsCount;

    public void addRoad(String name){
        roadsThread.addRoad(name);
    }

    public QueueThread(int interval, RoadsThread roadsThread, int maxRoadsCount) {
        super(interval);
        this.startTime = System.currentTimeMillis();
        this.roadsThread = roadsThread;
        this.maxRoadsCount = maxRoadsCount;
    }

    public void run(){
        while (!state.equals("quit")){
            while(state.equals("idle")){
                runTime++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            int systemIteration = 0;
            while (state.equals("system")){
                cleanDisplay();
                System.out.printf("""
                ! %ds. have passed since system startup !
                ! Number of roads: %d !
                ! Interval: %d !
                
                """, (System.currentTimeMillis() - startTime) / 1000,  maxRoadsCount, interval);

                int i = 0;
                for (String road : roadsThread.roads) {
                    int openTime = roadsThread.roadsTime.get(i);
                    if (roadsThread.isOpen.get(i)){
                        System.out.println(road + "\u001B[32m" +  " will be open for " + openTime + "s." + "\u001B[0m");
                    } else{
                        System.out.println(road + "\u001B[31m" +  " will be closed for " + openTime + "s." + "\u001B[0m");
                    }
                    i++;
                }
                System.out.println("\n! Press \"Enter\" to open menu !");
                try {
                    QueueThread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                systemIteration++;
            }
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
}
