package gateway.query.handler;

import gateway.cache.RequestCache;
import gateway.kafka.KafkaProducerService;
import gateway.server.HttpServerRunner;
import gateway.util.JsonSerializer;
import gateway.util.PayloadObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class QueryHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


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
            QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());

            if (!decoder.parameters().isEmpty()) {

                String entityID = (decoder.parameters().get("id") == null) ? "" : decoder.parameters().get("id").get(0);

                if (!entityID.equals("")) {
                    PayloadObject payloadObject = new PayloadObject(msg.method());
//                    payloadObject.setEntity_id(entityID);

                    Optional<String> params = JsonSerializer.toJsonString(decoder.parameters());

                    payloadObject.setPayload(params.get()).setHeaders(msg.headers().entries());

                    Optional<String[]> matchingUrlDef;

                    matchingUrlDef = HttpServerRunner.urls.stream().filter(url ->
                            (url[0].equals(msg.method().name()) && url[1].equals(decoder.path()))).findFirst();
                    matchingUrlDef.ifPresent(matchingUrl -> {
                                payloadObject.setTag(matchingUrl[2].split("=")[1]);
                                payloadObject.setEntity(matchingUrlDef.get()[3].split("=")[1]);
                            }
                    );

//                    Optional<String> requestMessage = JsonSerializer.toJsonString(payloadObject);

                    //ToDo complete msg
                    String requestMessage = getRequestMessage(payloadObject);
                    System.out.println(requestMessage);
                    KafkaProducerService.getInstance().publish(
                            /*TOPIC*/String.valueOf(payloadObject.getMeta().get("entity")),
                            /*ENTITY_ID*/String.valueOf(decoder.parameters().get("orderId")),
                            /*REQUEST_MSG*/requestMessage);
//                    System.out.println("MSG sent to KAFKA producer");

                    //Cache the QUERY along with the Ref_ID
                    RequestCache.getInstance().saveQueryRequest(payloadObject.getMeta().get("flowId").toString(), ctx);

                } else {
                    sendInvalidReqResponse(ctx);
                    System.out.println("INVALID SENT");
                }

            } else {
                sendInvalidReqResponse(ctx);
                System.out.println("INVALID SENT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRequestMessage(PayloadObject obj) {

        StringBuilder str = new StringBuilder();

        str.append(obj.getMeta().get("entity"));
        str.append("_");
        str.append(obj.getTag());

        str.append("::");
        Optional<String> meta = JsonSerializer.toJsonString(obj.getMeta());
        str.append(meta.get());

        str.append("::");
        str.append(obj.getPayload());

        return str.toString();

    }

    private void sendInvalidReqResponse(ChannelHandlerContext ctx) {

        String queryResponseString = "Entitiy ID is not present";

        System.out.println(queryResponseString);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                HttpResponseStatus.EXPECTATION_FAILED, Unpooled.copiedBuffer(queryResponseString, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
                .set(HttpHeaderNames.CONTENT_LENGTH, queryResponseString.length());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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