package gateway.server;

import io.netty.channel.*;
import java.net.SocketAddress;

public class CustomExceptionHandler extends ChannelDuplexHandler {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println(cause.getLocalizedMessage());
        System.out.println("THROWABLE CAUSE");
        cause.printStackTrace();

//        ctx.close upon completion
//        ctx.fireExceptionCaught(cause);
        // Uncaught exceptions from inbound handlers will propagate up to this handler
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    System.out.println("FUTURE iS NOT SUCCESS");
                    // Handle connect exception here...
                }
            }
        }));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(msg, promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    // Handle write exception here...
                }
            }
        }));
    }

    // ... override more outbound methods to handle their exceptions as well
}