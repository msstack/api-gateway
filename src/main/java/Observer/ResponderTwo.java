package Observer;

public class ResponderTwo implements OnCompleteListener {

    @Override
    public void completedEventTriggered(long uniqueID) {
        System.out.println("2-->"+uniqueID);

    }
}
