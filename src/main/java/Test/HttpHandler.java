package Test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.concurrent.Promise;

import java.util.HashMap;
import java.util.Map;

public class HttpHandler extends
        SimpleChannelInboundHandler<FullHttpResponse> {

    private Map<Integer, Promise<FullHttpResponse>> streamId2Promise =  new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
        if (streamId == null) {
            System.err.println("HttpResponseHandler unexpected message received: "
                    + msg);
            return;
        }
        if (streamId.intValue() == 1) {
            // this is the upgrade response message, just ignore it.
            return;
        }
        Promise<FullHttpResponse> promise;
        synchronized (this) {
            promise = streamId2Promise.get(streamId);
        }
        if (promise == null) {
            System.err.println("Message received for unknown stream id " + streamId);
        } else {
            // Do stuff with the message (for now just print it)
            promise.setSuccess(msg.retain());

        }
    }

    public void put(Integer streamId, Promise<FullHttpResponse> promise) {
        streamId2Promise.put(streamId, promise);
    }
}