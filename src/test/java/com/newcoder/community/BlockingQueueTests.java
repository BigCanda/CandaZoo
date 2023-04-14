package com.newcoder.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {

    public static void main(String args[]) {
        // 传入一个容量为10的阻塞队列
        BlockingQueue queue = new ArrayBlockingQueue(10);
        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}

class Producer implements Runnable {

    private BlockingQueue<Integer> queue;

    protected Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }
    @Override
    public void run() {
        try {
            for (int i =0; i < 100; i++) {
                // 模拟处理业务
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产:" + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable {
    private BlockingQueue<Integer> queue;

    protected Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }
    @Override
    public void run() {
        try {
            while(true) {
                // 模拟用户使用业务
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费:" + queue.size());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
