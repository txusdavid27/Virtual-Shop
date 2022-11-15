package com.puj.machines;


import org.zeromq.SocketType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZLoop;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class Balanceador {    
    private static byte[]    WORKER_READY = { '\001' };


    //Our load-balancer structure, passed to reactor handlers
    private static class LBBroker
    {
        Socket        frontend; //  Listen to clients
        Socket        backend;  //  Listen to workers
        Queue<ZFrame> workers;  //  List of ready workers
    };

    /**
     * In the reactor design, each time a message arrives on a socket, the
     * reactor passes it to a handler function. We have two handlers; one
     * for the frontend, one for the backend:
     */
    private static class FrontendHandler implements ZLoop.IZLoopHandler
    {

        @Override
        public int handle(ZLoop loop, PollItem item, Object arg_)
        {
            LBBroker arg = (LBBroker) arg_;
            ZMsg msg = ZMsg.recvMsg(arg.frontend);
            System.out.println("hola");
            if (msg != null) {
                msg.wrap(arg.workers.poll());
                msg.send(arg.backend);

                //  Cancel reader on frontend if we went from 1 to 0 workers
                if (arg.workers.size() == 0) {
                    loop.removePoller(new PollItem(arg.frontend, 0));
                }
            }
            return 0;
        }

    }

    private static class BackendHandler implements ZLoop.IZLoopHandler
    {

        @Override
        public int handle(ZLoop loop, PollItem item, Object arg_)
        {
            LBBroker arg = (LBBroker) arg_;
            ZMsg msg = ZMsg.recvMsg(arg.backend);
            if (msg != null) {
                ZFrame address = msg.unwrap();
                //  Queue worker address for load-balancing
                arg.workers.add(address);

                //  Enable reader on frontend if we went from 0 to 1 workers
                if (arg.workers.size() == 1) {
                    PollItem newItem = new PollItem(arg.frontend, ZMQ.Poller.POLLIN);
                    loop.addPoller(newItem, frontendHandler, arg);
                }

                //  Forward message to client if it's not a READY
                ZFrame frame = msg.getFirst();
                if (Arrays.equals(frame.getData(), WORKER_READY))
                    msg.destroy();
                else msg.send(arg.frontend);
            }
            return 0;
        }
    }

    private final static FrontendHandler frontendHandler = new FrontendHandler();
    private final static BackendHandler  backendHandler  = new BackendHandler();


    public static void main( String[] args ) throws Exception
    {
        //  Prepare our context and sockets
        try (ZContext context = new ZContext()) {
            LBBroker arg = new LBBroker();
            arg.frontend = context.createSocket(SocketType.ROUTER);
            arg.backend = context.createSocket(SocketType.ROUTER);
            arg.frontend.bind("tcp://*:5557");
            arg.backend.bind("tcp://*:5558");

            //  Queue of available workers
            arg.workers = new LinkedList<ZFrame>();

            //  Prepare reactor and fire it up
            ZLoop reactor = new ZLoop(context);
            PollItem item = new PollItem(arg.backend, ZMQ.Poller.POLLIN);
            reactor.addPoller(item, backendHandler, arg);
            reactor.start();
        }
    }
}

