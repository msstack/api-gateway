package Observer;

public class Initiator {

    public static void main(String args[]) {

        LongRunningTask longRunningTask = new LongRunningTask();

        System.out.println("reg responder_1");
        longRunningTask.setOnCompleteListener(new Responder());

        System.out.println("reg responder_2");
        longRunningTask.setOnCompleteListener(new ResponderTwo());

        System.out.println("Starting the long running task.");
        longRunningTask.run();
    }
}