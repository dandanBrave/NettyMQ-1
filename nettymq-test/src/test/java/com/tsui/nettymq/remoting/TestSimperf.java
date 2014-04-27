package com.tsui.nettymq.remoting;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import simperf.Simperf;
import simperf.junit.SimperfTestCase;
import simperf.thread.SimperfThread;
import simperf.thread.SimperfThreadFactory;

public class TestSimperf extends SimperfTestCase {
    public static Logger logger = Logger.getLogger(TestSimperf.class);

    @Before
    public void before() throws Exception {
        System.out.println("before");
    }

    @Test
    public void test() throws Exception {
        Simperf perf = new Simperf(100, 1, 10, new SimperfThreadFactory() {
            @Override
            public SimperfThread newThread() {
                return new SimperfThread() {
                    @Override
                    public boolean runTask() {
                        System.out.println("test1");
                        try {
                            new NIOEchoClient();
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                        return true;
                    }
                };
            }
        });

        // setting output file path£¬simperf-result.log by default.
        perf.getMonitorThread().setLogFile("simperf.log");
        // begin performance testing
        perf.start();
        Thread.sleep(10000);
    }
}