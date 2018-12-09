package wtc.mk;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class Market implements Runnable {
    private final static String HOSTNAME = "127.0.0.1";
    private final static int PORT = 5001;

    private String message;
    private Selector selector;
    private String line = "";
    private Scanner scanner;
    private boolean init = true;
    private Database stocks;

    public Market(String message)
    {
        this.message = message;
    }

    @Override
    public void run() {
        stocks = new Database();
        stocks.printAllStock();

        SocketChannel channel;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress(HOSTNAME, PORT));

            scanner = new Scanner(System.in);


            while (!Thread.interrupted()) {
                selector.select(1000);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid())
                        continue;

                    if (key.isConnectable()) {
//                        System.out.println("Connected to the server.");
                        connect(key);
                    }
                    if (key.isWritable()) {
                        System.out.println("Writing to the server");
                        write(key);
                    }
                    if (key.isReadable()) {
                        System.out.println("Reading from server");
                        read(key);
                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            close();
        }
    }

    private void close() {
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        try {
            length = channel.read(readBuffer);
        } catch (IOException e) {
            System.out.println("Reading problem, closing connection.");
            key.cancel();
            channel.close();
            return;
        }
        if (length == -1) {
            System.out.println("Nothing was read from server");
            channel.close();
            key.cancel();
            return;
        }
        readBuffer.flip();
        byte[] buff = new byte[readBuffer.remaining()];
        readBuffer.get(buff, 0, length);
        readBuffer.rewind();
        String[] fix = StandardCharsets.ISO_8859_1.decode(readBuffer).toString().split("\\|");
        readBuffer.rewind();

        String newfix = String.join("|", Arrays.copyOf(fix, 8));
        if (fix.length < 2)
            System.out.println("Router: " + StandardCharsets.ISO_8859_1.decode(readBuffer));
        else {
            System.out.println("Router: " + StandardCharsets.ISO_8859_1.decode(readBuffer));
            Trade trade = new Trade(fix);
            if (trade.process()) {
                channel.write(ByteBuffer.wrap(newfix.concat("|success").getBytes(StandardCharsets.ISO_8859_1)));
                System.out.println("Reply: " + newfix.concat("|success"));
            }
            else {
                channel.write(ByteBuffer.wrap(newfix.concat("|failure").getBytes(StandardCharsets.ISO_8859_1)));
                System.out.println("Reply: " + newfix.concat("|failure"));
            }
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, OP_READ);
    }

    private void write(SelectionKey key) throws IOException {
//        if(!init)
//            line = scanner.nextLine();
//        else {
            line = message;
//            init = false;
//        }
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap(line.getBytes(StandardCharsets.ISO_8859_1)));

        key.interestOps(OP_READ);
    }

    public static void main(String[] args) {
        String string1 = "Reply from Market";

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Market(string1), 1, 1, TimeUnit.SECONDS);
//        executorService.execute(new com.wtc.fix.test2.Broker(string1));
    }
}
