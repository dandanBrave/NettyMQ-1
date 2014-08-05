package com.tsui.nettymq.remoting;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import simperf.Simperf;
import simperf.junit.SimperfTestCase;
import simperf.thread.SimperfThread;
import simperf.thread.SimperfThreadFactory;

public class TestSimperf extends SimperfTestCase {
    public static Logger         logger     = Logger.getLogger(TestSimperf.class);
    public static int            count      = 0;
    public static int            thread_num = 100;
    public static CountDownLatch cl         = new CountDownLatch(thread_num);

    @Before
    public void before() throws Exception {
        System.out.println("before");
    }

    @Test
    public void test() throws Exception {
        Simperf perf = new Simperf(thread_num, 1, 10, new SimperfThreadFactory() {
            @Override
            public SimperfThread newThread() {
                return new SimperfThread() {
                    @Override
                    public boolean runTask() {
                        System.out.println("test" + count++);
                        try {
                            //                            new NIOEchoClient();
                            //                            new TCPClient();
                            new NIOClientConnect();
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                        cl.countDown();
                        return true;
                    }
                };
            }
        });

        // setting output file path simperf-result.log by default.
        perf.getMonitorThread().setLogFile("simperf.log");
        // begin performance testing
        long start1 = System.currentTimeMillis();
        perf.start();
        cl.await();
        long end1 = System.currentTimeMillis();
        long time1 = end1 - start1;
        System.out.println("thread total time: " + time1);
        //        Thread.sleep(10000);

    }
}