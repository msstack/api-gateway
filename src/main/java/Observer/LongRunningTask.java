package Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LongRunningTask implements Runnable {

    private List<OnCompleteListener> listeners = new ArrayList<OnCompleteListener>();

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.listeners.add(onCompleteListener);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5*1000); // sleep for 5 seconds and pretend to be working
            for (int i=0;i<listeners.size();i++){
                listeners.get(i).completedEventTriggered(Math.abs(UUID.randomUUID().getLeastSignificantBits()));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }
}