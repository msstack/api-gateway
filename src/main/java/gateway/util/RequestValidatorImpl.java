package gateway.util;

public class RequestValidatorImpl extends RequestValidator {

    @Override
    public Boolean validateRequest(String authenticationHeader) {

        return authenticationHeader.equals("Bearer 123");

    }

}
