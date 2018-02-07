package com.zzq.rpc.client;

import com.zzq.rpc.encrypt.RpcDecoder;
import com.zzq.rpc.encrypt.RpcEncoder;
import com.zzq.rpc.exchange.RpcRequest;
import com.zzq.rpc.exchange.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yhsyzzq on 2018-02-06.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);

    private String host;

    private int port;

    private RpcResponse response;

    private final Object obj = new Object();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        this.response = response;
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) {
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
                        .addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
                        .addLast(RpcClient.this); // 使用 RpcClient 发送 RPC 请求
            }
        }).option(ChannelOption.SO_KEEPALIVE, true);

        try {
            //发起异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();
            log.debug("client send request:id="+request.getRequestId());
            future.channel().writeAndFlush(request).sync();
            synchronized (obj) {
                obj.wait(); //未收到响应，使线程等待
            }

            if (response != null) {
                //等待客户端链路关闭
                future.channel().closeFuture().sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
        return response;
    }
}
