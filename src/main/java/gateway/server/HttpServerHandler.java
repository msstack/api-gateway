package gateway.server;

import com.fasterxml.jackson.databind.JsonNode;
import gateway.command.handler.PayloadHandler;
import gateway.query.handler.QueryHandler;
import gateway.util.JsonSerializer;
import gateway.util.RequestValidator;
import gateway.util.RequestValidatorImpl;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    //ToDo plug the request validator here
    RequestValidator requestValidator = new RequestValidatorImpl();

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            msg.retain(); //since reference count is decremented

            Optional<String[]> matchingUrlDef = HttpServerRunner.urls.stream().filter(url ->
                    (url[0].equals(msg.method().name()) && url[1].equals(new QueryStringDecoder(msg.uri()).path()))).findFirst();

            if (matchingUrlDef.isPresent()) {
                //validate=true is the 5th item
                if (matchingUrlDef.get()[4].split("=")[1].equals("true")) {
                    Optional<Map.Entry<String, String>> authenticationHeader = msg.headers().entries().stream()
                            .filter(h -> h.getKey().equals("authentication")).findFirst();

                    StringBuilder authenticationHeaderValueBuilder = new StringBuilder();
                    authenticationHeader.ifPresent(h -> authenticationHeaderValueBuilder.append(h.getValue()));

                    if (authenticationHeaderValueBuilder.toString().equals("")) {
                        System.out.println("Authentication REQUIRED but header NOT present");
                        //ToDo is this really a BAD_REQ??
                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plailn");
                        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    } else {

                        Boolean isValidated = requestValidator.validateRequest(authenticationHeaderValueBuilder.toString());
//                            System.out.println("Authentication header present");
//                            System.out.println("Authentication header : " + authenticationHeaderValueBuilder.toString());
                        if (isValidated) {
                            processRequest(ctx, msg);
                        } else {
                            //ToDo not Authenticated response
                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
                            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                        }
                    }
                } else {
                    processRequest(ctx, msg);
                }
            } else {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void processRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
        if (msg.method().name().equalsIgnoreCase("GET")) {
            addQueryHandler(ctx, msg);
        } else {
            addCommandHandler(ctx, msg);
        }
    }

    private void addQueryHandler(ChannelHandlerContext ctx, FullHttpRequest msg) {
        SimpleChannelInboundHandler handler = new QueryHandler();
        ctx.pipeline().addLast(handler);
        try {
            handler.channelRead(ctx, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCommandHandler(ChannelHandlerContext ctx, FullHttpRequest msg) {

        JsonNode msgJson = JsonSerializer.toJsonObject(msg.content().toString(CharsetUtil.UTF_8));

        if (msgJson.get("id") != null) {
            //Send 200 OK ack to the client
            sendOK(ctx);

            SimpleChannelInboundHandler handler = new PayloadHandler();
            ctx.pipeline().addLast(handler);
            try {
                handler.channelRead(ctx, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sendExpectationFailed(ctx);
        }
    }

    private void sendOK(ChannelHandlerContext ctx) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendExpectationFailed(ChannelHandlerContext ctx) {
        String queryResponseString = "Entitiy ID is not present";
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                HttpResponseStatus.EXPECTATION_FAILED,
                Unpooled.copiedBuffer(queryResponseString, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
                .set(HttpHeaderNames.CONTENT_LENGTH, queryResponseString.length());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}