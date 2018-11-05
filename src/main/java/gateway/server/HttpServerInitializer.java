package gateway.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServerInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1048576));
        ch.pipeline().addLast(new HttpServerHandler());

//          Cannot ADD another SimpleInboundChannelHandler as it releases the resurces
//        therefore, manually call payload handler after msg.retain()
        //        ch.pipeline().addLast(new PayloadHandler());

        ch.pipeline().addLast(new CustomExceptionHandler());
    }
}