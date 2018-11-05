package gateway.query.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class QueryResponseReceiver implements QueryOnCompleteListener {

    private static QueryResponseReceiver queryResponseReceiver = null;

    private QueryResponseReceiver() {
    }

    public static QueryResponseReceiver getInstance() {
        if (queryResponseReceiver == null) {
            queryResponseReceiver = new QueryResponseReceiver();
        }
        return queryResponseReceiver;
    }

    @Override
    public void onComplete(ChannelHandlerContext ctx, String queryResponse) {

        ByteBuf content = Unpooled.copiedBuffer(queryResponse, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, queryResponse.length());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }
}
