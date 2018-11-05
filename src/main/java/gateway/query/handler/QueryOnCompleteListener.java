package gateway.query.handler;

import io.netty.channel.ChannelHandlerContext;

public interface QueryOnCompleteListener {
    void onComplete(ChannelHandlerContext ctx, String response);
}
