package com.zzq.rpc.proxy;

import com.zzq.rpc.client.RpcClient;
import com.zzq.rpc.exchange.RpcRequest;
import com.zzq.rpc.exchange.RpcResponse;
import com.zzq.rpc.registry.ServiceDiscovery;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 服务代理类
 * Created by yhsyzzq on 2018-02-06.
 */
public class RpcProxy {
    private String serverAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                        //创建并初始化rpc请求
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        if (serviceDiscovery != null) {
                            //发现服务
                            serverAddress = serviceDiscovery.discover();
                        }

                        String[] array = serverAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);

                        RpcClient client = new RpcClient(host, port); //初始化RPC客户端
                        RpcResponse response = client.send(request); //通过RPC客户端发送RPC请求并获取RPC响应

                        if (response.isError()) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }
}
