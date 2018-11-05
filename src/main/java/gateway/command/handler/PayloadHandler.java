package gateway.command.handler;

import gateway.cache.RequestCache;
import gateway.kafka.KafkaProducer;
import gateway.server.HttpServerRunner;
import gateway.util.JsonSerializer;
import gateway.util.PayloadObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class PayloadHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        try {
            msg.retain(); //since reference count is decremented
            if (msg != null) {
                String msgContent = msg.content().toString(CharsetUtil.UTF_8);

                HttpMethod httpMethod = msg.method();
                QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
                String uri = decoder.uri();  //path with params
                String path = decoder.path(); //path without params
                Map<String, List<String>> params = decoder.parameters();
                List<Map.Entry<String, String>> headers = msg.headers().entries();


                PayloadObject payloadObject = new PayloadObject(httpMethod, path);

                RequestCache.getInstance().saveCommandRequest(payloadObject.getReference_id(),ctx);

                payloadObject.setParams(params)
                        .setUri(uri)
                        .setPayload(msgContent)
                        .setHeaders(headers);

                Optional<String[]> matchingUrlDef;

                matchingUrlDef = HttpServerRunner.urls.stream().filter(url ->
                        (url[0].equals(httpMethod.name()) && url[1].equals(path))).findFirst();
                matchingUrlDef.ifPresent(matchingUrl -> {
                            String[] serviceMethod = matchingUrlDef.get()[2].split("\\.");
                            payloadObject.setMicroservice(serviceMethod[0]);
                            payloadObject.setHandler(serviceMethod[1]);
                            payloadObject.setEntity(matchingUrlDef.get()[3].split("=")[1]);
                            payloadObject.setValidation(matchingUrlDef.get()[4].split("=")[1]);
                        }
                );

                Optional<String> requestMessage = JsonSerializer.toJsonString(payloadObject);
                if (requestMessage.isPresent()) {
                    System.out.println(requestMessage.get());

                    //ToDo send to the Kafka Queue
                    KafkaProducer.getInstance().queue(requestMessage.get());
                }
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

}