import com.google.common.base.Stopwatch;
import com.google.*;
import com.google.common.io.Files;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Diego Urrutia Astorga <durrutia@ucn.cl>
 * @version 20170330130700
 */
public class ProcessRequestRunnable implements Runnable {

    /**
     * Logger de la clase
     */
    private static final Logger log = LoggerFactory.getLogger(ProcessRequestRunnable.class);

    /**
     * Socket asociado al cliente.
     */
    private Socket socket;


    /**
     * Constructor
     *
     * @param socket
     */
    public ProcessRequestRunnable(final Socket socket) {
        this.socket = socket;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        // Cronometro ..
//        final Stopwatch stopWatch = Stopwatch.createStarted();

        log.debug("Connection from {} in port {}.", socket.getInetAddress(), socket.getPort());
        // A dormir por un segundo.
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        try {
            processRequest(this.socket);
        } catch (Exception ex) {
            log.error("Error", ex);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Nothing here
            }
        }

//        log.debug("Request procesed in {}.", stopWatch);

    }


    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }


    /**
     * Procesar peticion
     *
     * @param socket
     */

    private static void processRequest(final Socket socket) throws IOException {

        // Iterador de la peticion
        final LineIterator lineIterator = IOUtils.lineIterator(socket.getInputStream(), Charset.defaultCharset());
        // Peticion
//        final String request = getRequest(lineIterator);
        final String request = getRequest2(socket);
        log.debug("Request detected: {}", request);

        if (StringUtils.contains(request, "POST")) {

            log.debug("se hizo un post");
        }
        if (StringUtils.contains(request, "shutdown")) {
            log.debug("shutdown");
            // .. Cierro el servicio
            socket.close();
//            serverSocket.close();
//            break;

        }
//        if(request.equals("/post.php")){
//
//        if (StringUtils.contains("/addToChatLog/",100)) {
//            log.debug(String.valueOf(socket.getInputStream()));
//        }

        // Output
        final OutputStream outputStream = IOUtils.buffer(socket.getOutputStream());

        log.debug("Writing data for: {}", request);

        // HTTP header
        writeHeader(outputStream);

        // HTTP Body
//        writeBody(outputStream, request);

        // index.html
        writeIndex(outputStream);

        // Cierro el stream
        IOUtils.closeQuietly(outputStream);

    }

    /**
     * Obtengo la linea de peticion de request.
     *
     * @return the request.
     */

//    private static String getRequest(final LineIterator lineIterator) {
//
//        String request = null;
//        int n = 0;
//
//        while (lineIterator.hasNext()) {
//
//            // Leo linea a linea
//            final String line = lineIterator.nextLine();
//            log.debug("Linea {}: {}", ++n, line);
//
//            // Guardo la peticion de la primera linea
//            if (n == 1) {
//                request = line;
//            }
//
//            // Termine la peticion si llegue al final de la peticion
//            if (StringUtils.isEmpty(line)) {
//                break;
//            }
//
//        }
//
//        return request;
//    }


    private static String getRequest2(Socket insocket) throws IOException {
        final InputStream is = insocket.getInputStream();
        final BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        line = in.readLine();
        String request_method = line;
        line = "";
        // busca post data
        int postDataI = -1;
        while ((line = in.readLine()) != null && (line.length() != 0)) {
            log.debug("HTTP-HEADER: " + line);
            if (line.indexOf("Content-Length:") > -1) {
                postDataI = new Integer(
                        line.substring(
                                line.indexOf("Content-Length:") + 16,
                                line.length())).intValue();
            }
        }
        String postData = "";

        // lee el post data
        if (postDataI > 0) {
            char[] charArray = new char[postDataI];
            in.read(charArray, 0, postDataI);
            postData = new String(charArray);
            log.debug(postData);
        }
        if (StringUtils.contains(postData, "msg=")) {
            // Se obtiene el mensaje, separandolo de "msg=" y se decodifica
            final String[] array = StringUtils.split(postData,"=");
            final String linea = array[2];
            final String lineaDecodificada = URLDecoder.decode(linea);

            // Se obtiene el usuario que envio el mensaje y se decodifica
            final String[] array2 = StringUtils.split(array[1],"&");
            final String usuario = array2[0];
            final String usuarioDecodificado = URLDecoder.decode(usuario);

            // Se obtiene la hora actual
            final Date date = new Date();
            final DateFormat format = new SimpleDateFormat("HH:mm");
            final String horaActual = format.format(date);

            writeChatLog(usuarioDecodificado, horaActual, lineaDecodificada);
        }
        return request_method;
    }


    /**
     * Escribe el encabezado del protocolo HTTP.
     *
     * @param outputStream
     * @throws IOException
     */
    private static void writeHeader(OutputStream outputStream) throws IOException {

        // Header
        IOUtils.write("HTTP/1.0 200 OK\r\n", outputStream, Charset.defaultCharset());
        IOUtils.write("Content-type: text/html\r\n", outputStream, Charset.defaultCharset());

        // end-header
        IOUtils.write("\r\n", outputStream, Charset.defaultCharset());

    }

    /**
     * Escribe el body del encabezado.
     *
     * @param outputStream
     * @param request
     */
    private static void writeBody(OutputStream outputStream, String request) throws IOException {

        // Body
        final String body = "<html><head><title>WebServer v1.0</title></head><body><h3>Result:</h3><pre>CONTENT</pre></body></html>";

        final String random = RandomStringUtils.randomAlphabetic(100);

        final String result = StringUtils.replace(body, "CONTENT", random);

        IOUtils.write(result + "\r\n", outputStream, Charset.defaultCharset());

    }

    private static void writeIndex(OutputStream outputStream) throws IOException {

        // Chat
        final String curDir = System.getProperty("user.dir");
        final String path = curDir + File.separator + "index.html";
        final File file = new File(path);
        final String html = Files.toString(file,UTF_8);

        final String path2 = curDir + File.separator + "chatlog.txt";
        final File file2 = new File(path2);
        final String[] array = Files.toString(file2,UTF_8).split("///");

        String chatlog = "";
//        if(array.length>1){
            for (int i = 0; i < array.length; i++) {
                chatlog += "<div class='bubble-container'><span class='bubble'><img class='bubble-avatar' src='' /><div class='bubble-text'><p>" + array[i] + "</p></div><span class='bubble-quote' /></span></div>";
            }
//        }
//        chatlog = Files.toString(file2,UTF_8);

        final String index = StringUtils.replace(html, "hereGoesChatLog", chatlog);

//        try {
//            String asd = getHTML("http://localhost:9000");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        IOUtils.write(index + "\r\n", outputStream, Charset.defaultCharset());
    }

    private static void writeChatLog(String usuario, String horaEnvio, String newMsg){
        final String curDir = System.getProperty("user.dir");
        final String path = curDir + File.separator +"chatlog.txt";
        final File file = new File(path);

        try {
            Files.append("("+horaEnvio+") "+ usuario+ " : " +newMsg +"///",file,UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
