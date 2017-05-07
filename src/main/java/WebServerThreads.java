import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Diego Urrutia Astorga <durrutia@ucn.cl>
 * @version 20170330131600
 */
public class WebServerThreads {

    /**
     * Logger de la clase
     */
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    /**
     * Puerto de escucha
     */
    private static final Integer PORT = 9000;

    /**
     * Inicio del programa.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {

        log.debug("Starting ...");

        final ExecutorService executor = Executors.newFixedThreadPool(8);

        // Servidor en el puerto PORT
        final ServerSocket serverSocket = new ServerSocket(PORT);

        // Ciclo para atender a los clientes
        while (true) {

            log.debug("Waiting for connection ..");

            // 1 socket por peticion
            final Socket socket = serverSocket.accept();

            // Al executor ..
            final Runnable runnable = new ProcessRequestRunnable(socket);
            executor.execute(runnable);

            log.debug("Connection from {} in port {}.", socket.getInetAddress(), socket.getPort());

        }
    }

//    private static void processRequest(OutputStream outputStream, String request) throws IOException {
//        final String curDir = System.getProperty("user.dir");
//        final String path = curDir + "/index.html";
//        final File file = new File(path);
//        final String html = Files.toString(file,UTF_8);
//
//        final String path2 = curDir + "/chatlog.html";
//        final File file2 = new File(path2);
//        final String chatlog = Files.toString(file2,UTF_8);
//
//        final String index = StringUtils.replace(html, "hereGoesChatLog", chatlog);
//
//        IOUtils.write(index + "\r\n", outputStream, Charset.defaultCharset());
//    }
}
