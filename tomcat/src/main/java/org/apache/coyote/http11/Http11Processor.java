package org.apache.coyote.http11;

import camp.nextstep.exception.UncheckedServletException;
import camp.nextstep.http.domain.*;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStreamReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             final var outputStream = new BufferedOutputStream(connection.getOutputStream())) {

            final RequestLine requestLine = new RequestLine(inputStreamReader.readLine());
            final RequestURI requestURI = requestLine.getRequestURI();
            final HttpPath path = requestURI.getPath();

            final var responseBody = getResponseBody(path);
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(ContentType.from(path));
            headers.setContentLength(responseBody.length);

            final var responseHeader = String.join(System.lineSeparator(),
                    StatusLine.createOk().convertToString(),
                    headers.convertToString(),
                    System.lineSeparator());

            outputStream.write(responseHeader.getBytes());
            outputStream.write(responseBody);
            outputStream.flush();
        } catch (final IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private byte[] getResponseBody(final HttpPath path) throws IOException {
        if ("/".equals(path.getPath())) {
            return "Hello world!".getBytes();
        }

        final Resource resource = new Resource("static" + path.getPath());
        if (resource.exists()) {
            return resource.readAllBytes();
        }
        return new Resource("static/404.html").readAllBytes();
    }
}
