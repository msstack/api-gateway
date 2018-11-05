package Observer;

public class Responder implements OnCompleteListener {

    @Override
    public void completedEventTriggered(long uniqueID) {
        System.out.println("1-->"+uniqueID);

    }
}
