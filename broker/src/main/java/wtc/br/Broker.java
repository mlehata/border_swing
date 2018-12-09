package wtc.br;

import org.apache.commons.codec.digest.DigestUtils;
import wtc.br.Controler.Fix;

import java.io.IOException;
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

public class Broker implements Runnable {
    private final static String HOSTNAME = "127.0.0.1";
    private final static int PORT = 5000;

    private String ID;
    private String message;
    private Selector selector;
    private String line = "";
    private Scanner scanner;
    private boolean init = true;

    public Broker(String message)
    {
        this.message = message;
    }

    @Override
    public void run()
    {
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
                        System.out.println("Connected to the server");
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

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, OP_READ);
    }

    private void close() {
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        try {
            length = channel.read(readBuffer);
        } catch (IOException e) {
            System.out.println("Reading problem, closing connection");
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
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        readBuffer.rewind();
        String[] fix = StandardCharsets.ISO_8859_1.decode(readBuffer).toString().split("\\|");
        readBuffer.rewind();

        if (fix.length < 2) {
            String line = StandardCharsets.ISO_8859_1.decode(readBuffer).toString();
            String[] id = line.split("(\\[)|(])");
            if (id[0].equals("ID"))
                ID = id[1];
            System.out.println("Router: " + line);
        }
        else {
            System.out.println("Router: " + StandardCharsets.ISO_8859_1.decode(readBuffer));
            Stock stock = new Stock(fix);
            stock.process();
        }

        key.interestOps(OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException {
        if(!init) {
            while (true) {
                line = new Fix(ID).order();
                System.out.println(line);

                Stock stock = new Stock(line.split("\\|"));
                if (stock.check())
                    break;
                System.out.println("Input error");
            }

            String checksum = DigestUtils.md5Hex(line);
            line = line.concat(checksum);
        }
        else {
            line = message;
            init = false;
        }
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap(line.getBytes(StandardCharsets.ISO_8859_1)));

        key.interestOps(OP_READ);
    }

    public static void main(String[] args) {
        String string1 = "Broker Connected";

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Broker(string1), 1, 1, TimeUnit.SECONDS);
//        executorService.execute(new com.wtc.fix.test2.Broker(string1));
    }
}
