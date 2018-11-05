package Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.ArrayList;

public class HttpServerRunner {

    public static ArrayList<String[]> urls;
    private int port;

    public HttpServerRunner(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new HttpServerRunner(port).run();
    }

    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(300);

        try{
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer())
                    .channel(NioServerSocketChannel.class);

            Channel ch = bootstrap.bind(port).sync().channel();
            ch.closeFuture().sync();

        } catch (InterruptedException /* | CertificateException | SSLException */e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}