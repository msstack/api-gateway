package gateway.util;

import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayloadObject {

    /*
     *
     * type :[REQUEST,RESPONSE,EVENT],
     * reference_id: UUID,
     * meta: map<str,str>
     * payload : "string"
     *
     * */
    private String type;
    private String reference_id;
    private String payload;
    private Map<String, Object> meta;

    public PayloadObject(HttpMethod httpMethod, String path) {

        this.reference_id = IDgenerator.getInstance().getUniqueID();
        this.type = MessageType.REQUEST.toString();
        this.meta = new HashMap<>();
        this.meta.put("httpMethod", httpMethod.toString());
        this.meta.put("path", path);
    }

    public String getType() {
        return type;
    }

    public String getReference_id() {
        return reference_id;
    }

    public String getPayload() {
        return payload;
    }

    public PayloadObject setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public PayloadObject setMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }

    public PayloadObject setUri(String uri) {
        setMeta("uri", uri);
        return this;
    }

    public PayloadObject setHeaders(List<Map.Entry<String, String>> headers) {
        //        setMeta("headers", headers);
        headers.stream().forEach(header->setMeta(header.getKey(),header.getValue()));
        return this;
    }

    public PayloadObject setParams(Map<String, List<String>> parameters) {
        setMeta("params", parameters);
        return this;
    }

    public PayloadObject setMicroservice(String microservice) {
        setMeta("microservice", microservice);
        return this;
    }

    public PayloadObject setHandler(String handler) {
        setMeta("handler", handler);
        return this;
    }

    public PayloadObject setEntity(String entity) {
        setMeta("entity", entity);
        return this;
    }

    public PayloadObject setValidation(String isValidationRequired) {
        setMeta("validate", isValidationRequired);
        return this;
    }

    @Override
    public String toString() {
        return "PayloadObject{" +
                "type='" + type + '\'' +
                ", reference_id='" + reference_id + '\'' +
                ", payload='" + payload + '\'' +
                ", meta=" + meta +
                '}';
    }

}
