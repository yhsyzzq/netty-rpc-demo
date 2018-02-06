package com.zzq.rpc.server;

import com.zzq.rpc.annotation.RpcService;

/**
 * Created by yhsyzzq on 2018-02-05.
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
