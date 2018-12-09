package wtc.rt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Router implements Runnable {
    public final String HOSTNAME = "127.0.0.1";

    private ServerSocketChannel brokerChannel;
    private ServerSocketChannel marketChannel;
    private Selector brokerSelector;
    private Selector marketSelector;

    private Map<SocketChannel, byte[]> dataTracking = new HashMap<>();
    private Map<String, SocketChannel> markets = new HashMap<>();
    private Map<String, SocketChannel> brokers = new HashMap<>();

    private int staticid = 100000;

    public Router() {
        init();
    }

    public void init() {
        System.out.println("starting server.......");
        if (brokerChannel != null)
            return;
        if (brokerSelector != null)
            return;
        try {
            brokerChannel = ServerSocketChannel.open();
            brokerChannel.configureBlocking(false);
            brokerChannel.socket().bind(new InetSocketAddress(HOSTNAME, 5000));
            brokerSelector = Selector.open();
            brokerChannel.register(brokerSelector, SelectionKey.OP_ACCEPT);

            marketChannel = ServerSocketChannel.open();
            marketChannel.configureBlocking(false);
            marketChannel.socket().bind(new InetSocketAddress(HOSTNAME, 5001));
            marketSelector = Selector.open();
            marketChannel.register(marketSelector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Listening for connections");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                brokerSelector.select(1000);
                Iterator<SelectionKey> keys = brokerSelector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid())
                        continue;
                    if (key.isAcceptable()) {
                        System.out.println("Accepting broker");
                        accept(key);
                    }
                    if (key.isWritable()) {
                        write(key);
                    }
                    if (key.isReadable()) {
                        System.out.println("Reading connection");
                        read(key);
                    }
                }

                marketSelector.select(1000);
                Iterator<SelectionKey> keys2 = marketSelector.selectedKeys().iterator();
                while (keys2.hasNext()) {
                    SelectionKey key = keys2.next();
                    keys2.remove();
                    if (!key.isValid())
                        continue;
                    if (key.isAcceptable()) {
                        System.out.println("Accepting market");
                        accept(key);
                    }
                    if (key.isWritable()) {
                        write(key);
                    }
                    if (key.isReadable()) {
                        System.out.println("Reading connection");
                        read(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
//        int id = new Random().nextInt((999999 - 100000) + 1) + 100000;
        int id = staticid++;

        //System.out.println("Server socket channel :: " + socketChannel.getLocalAddress());

        if (key.selector().equals(marketSelector)) {
            markets.put(Integer.toString(id), socketChannel);
            System.out.println("Saved: " + id);
        } else if (key.selector().equals(brokerSelector)) {
            brokers.put(Integer.toString(id), socketChannel);
            System.out.println("Saved: " + id);
        }



        socketChannel.register(brokerSelector, SelectionKey.OP_WRITE);
        byte[] hello = ("ID["+ id +"]").getBytes();
        dataTracking.put(socketChannel, hello);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        byte[] data = dataTracking.get(channel);
        dataTracking.remove(channel);
        channel.write(ByteBuffer.wrap(data));
        key.interestOps(SelectionKey.OP_READ);
    }

    private void writeChannel(SocketChannel channel, byte[] data) throws IOException {
        channel.write(ByteBuffer.wrap(data));
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        //SocketChannel market =  marketChannel.accept();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();

        int read;
        try {
            read = channel.read(readBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            channel.close();
            return;
        }

        if (read == -1) {
            System.out.println("Nothing was there to be read, closing connection");
            channel.close();
            key.cancel();
            return;
        }
        readBuffer.flip();
        byte[] data = new byte[readBuffer.remaining()];
        readBuffer.get(data);
        readBuffer.rewind();
        System.out.println(StandardCharsets.ISO_8859_1.decode(readBuffer));
        readBuffer.rewind();

        String[] fix = StandardCharsets.ISO_8859_1.decode(readBuffer).toString().split("\\|");

        if (fix.length > 8) {
            if (fix[8].equals("success")) {
                writeChannel(brokers.get(fix[4].split("=")[1]), data);
            } else if (fix[8].equals("failure")) {
                writeChannel(brokers.get(fix[4].split("=")[1]), data);
            } else if (markets.containsKey(fix[3].split("=")[1])) {
                writeChannel(markets.get(fix[3].split("=")[1]), data);
            } else
                writeChannel(channel, data);
        } else
            writeChannel(channel, data);

        key.interestOps(SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Router());
    }
}
