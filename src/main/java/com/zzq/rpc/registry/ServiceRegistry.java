package com.zzq.rpc.registry;

import com.zzq.rpc.context.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 注册中心注册服务
 * Created by yhsyzzq on 2018-02-06.
 */
public class ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * 注册服务
     *
     * @param data
     */
    public void register(String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                createNode(zk, data);
            }
        }
    }

    /**
     * 连接服务
     *
     * @return
     * @throws IOException
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, (int) Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            log.error("zookeeper connet error", e);
        }
        return zk;
    }

    private void createNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();
            String path = zk.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.debug("create zookeeper node({}=>{})", path, data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            log.error("", e);
        }
    }
}
