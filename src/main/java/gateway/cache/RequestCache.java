package gateway.cache;

import io.netty.channel.ChannelHandlerContext;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

//ToDo Write to a File and Retrieve from the file
public class RequestCache {

    private static RequestCache requestCache = null;

    private ConcurrentHashMap<String, ChannelHandlerContext> queryRequests;
    private ConcurrentHashMap<String, ChannelHandlerContext> commandRequests;


    private RequestCache() {
        queryRequests = new ConcurrentHashMap<>();
        commandRequests = new ConcurrentHashMap<>();
    }

    public static RequestCache getInstance() {
        if (requestCache == null) {
            requestCache = new RequestCache();
        }
        return requestCache;
    }

    public void saveQueryRequest(String requestID, ChannelHandlerContext context){
        queryRequests.put(requestID,context);
        System.out.println(requestID);
        System.out.println(context.toString());
    }

    public void saveCommandRequest(String requestID, ChannelHandlerContext context){
        commandRequests.put(requestID,context);
        System.out.println(requestID);
        System.out.println(context.toString());
    }

    public Optional<ChannelHandlerContext> getQueryRequestor(String requestID){
        ChannelHandlerContext ctx = queryRequests.get(requestID);
        return Optional.ofNullable(ctx);
    }

    public Optional<ChannelHandlerContext> getCommandRequestor(String requestID){
        ChannelHandlerContext ctx = commandRequests.get(requestID);
        return Optional.ofNullable(ctx);
    }

    public ConcurrentHashMap getAllQueryRequests(){
        return queryRequests;
    }

    public ConcurrentHashMap getAllCommandRequests(){
        return commandRequests;
    }


}
