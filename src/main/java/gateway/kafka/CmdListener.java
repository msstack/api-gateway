package gateway.kafka;

import gateway.cache.RequestCache;
import gateway.query.handler.QueryOnCompleteListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.Optional;
import java.util.Scanner;

public class CmdListener extends Thread {

    private QueryOnCompleteListener queryOnCompleteListener;

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        long key;

        while (true) {
            System.out.println("Please Enter LONG key");
            key = sc.nextLong();
            System.out.println("Please Enter String response");
            String value = sc.next();
            /**
             * Implemented only for QUERIES
             */
            Optional<ChannelHandlerContext> context;
            context = RequestCache.getInstance().getQueryRequestor(String.valueOf(key));
            //Record is a RESPONSE for a QUERY
            context.ifPresent(channelHandlerContext ->
                    queryOnCompleteListener.onComplete(channelHandlerContext, value));
        }
    }

    public CmdListener setQueryOnCompleteListener(QueryOnCompleteListener queryOnCompleteListener) {
        this.queryOnCompleteListener = queryOnCompleteListener;
        return this;
    }

}
