//package gateway;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.gateway.*;
//import io.netty.util.CharsetUtil;
//
//import static io.netty.handler.codec.gateway.HttpResponseStatus.INTERNAL_SERVER_ERROR;
//import static io.netty.handler.codec.gateway.HttpVersion.HTTP_1_1;
//
//public class CallAnotherHandlerSample extends SimpleChannelInboundHandler<FullHttpRequest> { //HttpObject
//
//    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
//        FullHttpResponse response = new DefaultFullHttpResponse(
//                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
//        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
//
//        // Close the connection as soon as the error message is sent.
//        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
//        try {
//
//            msg.retain(); //since reference count is decremented
//            if (msg instanceof FullHttpRequest) {
//
//                QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
//                String uri = decoder.uri();  //path with params
//
//                SimpleChannelInboundHandler handler = null;
//
//                switch (uri) {
//                    case "/":
//                        String responseJson = "{\"status\": \"200 OK\" }";
//                        ByteBuf content = Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8);
//                        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, content);
//                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "json");
//                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseJson.length());
//
//                        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
//                        break;
//                    case "/order":
//                        handler = new OrderHandler();
//                        break;
//                }
//
//                //CALNG ANOTHE HANDLER
//                ctx.pipeline().addLast(handler); //see API for arguments
//                handler.channelRead(ctx, msg);
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        if (ctx.channel().isActive()) {
//            sendError(ctx, INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
//    }
//}