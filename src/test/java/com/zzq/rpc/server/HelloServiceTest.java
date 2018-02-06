package com.zzq.rpc.server;

import com.zzq.rpc.proxy.RpcProxy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by yhsyzzq on 2018-02-06.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-client.xml")
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest() {
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("zengzhiqi");
        Assert.assertEquals("hello world! ", result);
    }
}
