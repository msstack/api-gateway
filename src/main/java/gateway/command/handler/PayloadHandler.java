package gateway.command.handler;

import com.fasterxml.jackson.databind.JsonNode;
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

                JsonNode msgJson = JsonSerializer.toJsonObject(msgContent);

                HttpMethod httpMethod = msg.method();
                QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
                String uri = decoder.uri();  //path with params
                String path = decoder.path(); //path without params
                Map<String, List<String>> params = decoder.parameters();
                List<Map.Entry<String, String>> headers = msg.headers().entries();

                PayloadObject payloadObject = new PayloadObject(httpMethod);
                payloadObject.setEntity_id(msgJson.get("id").asText());

//                RequestCache.getInstance().saveCommandRequest(payloadObject.getFlow_id(),ctx);

                payloadObject.setPayload(msgContent)
                        .setHeaders(headers);

                Optional<String[]> matchingUrlDef;

                matchingUrlDef = HttpServerRunner.urls.stream().filter(url ->
                        (url[0].equals(httpMethod.name()) && url[1].equals(path))).findFirst();
                matchingUrlDef.ifPresent(matchingUrl -> {
                            payloadObject.setTag(matchingUrl[2].split("=")[1]);
                            payloadObject.setEntity(matchingUrlDef.get()[3].split("=")[1]);
                        }
                );

                String requestMessage = getRequestMessage(payloadObject);
                System.out.println(requestMessage);
                KafkaProducerService.getInstance().publish(
                        /*TOPIC*/String.valueOf(payloadObject.getMeta().get("entity")),
                        /*ENTITY_ID*/String.valueOf(payloadObject.getMeta().get("entity_id")),
                        /*REQUEST_MSG*/requestMessage);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRequestMessage(PayloadObject obj) {

        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append(obj.getMeta().get("entity"));
        strBuilder.append("_");
        strBuilder.append(obj.getTag());

        strBuilder.append("::");
        Optional<String> meta = JsonSerializer.toJsonString(obj.getMeta());
        strBuilder.append(meta.get());

        strBuilder.append("::");
        strBuilder.append(obj.getPayload());

        return strBuilder.toString();
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