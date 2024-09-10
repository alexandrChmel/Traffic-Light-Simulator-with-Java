package traffic;

import java.util.LinkedList;

public class RoadsThread extends Thread{

    LinkedList<Integer> roadsTime = new LinkedList<Integer>();
    LinkedList<String> roads = new LinkedList<String>();
    LinkedList<Boolean> isOpen = new LinkedList<Boolean>();
    int interval;
    int roadsCount = 0;
    String state = "running";

    public void setState(String state) {
        this.state = state;
    }

    public RoadsThread(int interval) {
        this.interval = interval;
    }
    public void addRoad(String name){
        roads.add(name);
        roadsCount++;
        if (roadsTime.isEmpty()){
            isOpen.add(true);
            roadsTime.add(interval);
        } else{
            if (roads.size() == 2){
                roadsTime.add(roadsTime.getLast());
            } else{
                for (int i = 0; i < isOpen.size(); i++){
                    if (!isOpen.get(i)){
                        roadsTime.set(i, roadsTime.get(i) + interval);
                    } else {
                        if (isOpen.getLast()){
                            roadsTime.add(roadsTime.getLast());
                            break;
                        } else{
                            roadsTime.add(roadsTime.getLast() + interval);
                            break;
                        }
                    }
                }

            }
            isOpen.add(false);
        }

    }
    @Override
    public void run() {
        while(state.equals("running")){
            for(int i = 0; i < roadsTime.size(); i++){
                roadsTime.set(i, roadsTime.get(i) - 1);
                if (roadsTime.get(i) <= 0){
                    int combineT = (roadsCount * interval - interval);
                    if (roadsCount != 1){
                        isOpen.set(i, !isOpen.get(i));
                    } else{
                        isOpen.set(i, true);
                    }
                    if (isOpen.get(i)){
                        roadsTime.set(i, interval);
                    } else{
                        roadsTime.set(i, combineT);
                    }
                }
            }
            try {
                RoadsThread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
