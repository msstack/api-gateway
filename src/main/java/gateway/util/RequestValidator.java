package gateway.util;

public abstract class RequestValidator {

    public abstract Boolean validateRequest(String authenticationHeader);

}
