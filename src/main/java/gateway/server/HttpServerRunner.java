package gateway.server;

import gateway.kafka.KafkaConsumerService;
import gateway.query.handler.QueryResponseReceiver;
import gateway.util.ReadAPIdoc;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.ArrayList;
import java.util.concurrent.Executors;

//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.SelfSignedCertificate;
//
//import javax.net.ssl.SSLException;
//import java.security.cert.CertificateException;

public class HttpServerRunner {

    public static ArrayList<String[]> urls;
    private int port;

    public HttpServerRunner(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {

        urls = ReadAPIdoc.getInstance().getEndPoints("/home/gavindya/Desktop/FYP/apigateway/src/main/resources/input.txt");
        int port = 8080;

        KafkaConsumerService kafkaConsumerService = new KafkaConsumerService();
        kafkaConsumerService.setQueryOnCompleteListener(QueryResponseReceiver.getInstance());
//        kafkaConsumerService.start();

        Executors.newSingleThreadExecutor().submit(kafkaConsumerService::start);

//        CmdListener cmdListener = new CmdListener();
//        cmdListener.setQueryOnCompleteListener(QueryResponseReceiver.getInstance());
//        Executors.newSingleThreadExecutor().submit(cmdListener::start);

        System.out.println("Kafka Consumer started");

//        KafkaConsumer.getInstance().setQueryOnCompleteListener(QueryResponseReceiver.getInstance());

//        printRequests();
        new HttpServerRunner(port).run();
    }

//    private static void printRequests() {
//        System.out.println("printing thread started\n");
//        Thread printRequestsThread = new Thread(new RequestsPrinter());
//        printRequestsThread.start();
//    }

    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(300);

        try {
            //ToDo Add SSL
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            final SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

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