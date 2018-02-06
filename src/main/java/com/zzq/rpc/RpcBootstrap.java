package com.zzq.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by yhsyzzq on 2018-02-05.
 */
public class RpcBootstrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring-server.xml");
    }
}
