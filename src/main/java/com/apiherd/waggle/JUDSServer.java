package com.apiherd.waggle;

import com.apiherd.api.APIRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.apiherd.api.APIable;
import com.apiherd.api.RawsRequest;

import com.etsy.net.UnixDomainSocket;
import com.etsy.net.UnixDomainSocketServer;

public class JUDSServer {
    private int sockType;
    private String sockFile;
    private APIable api = null;
    private UnixDomainSocketServer unixServer;
    protected static final Logger log = LoggerFactory.getLogger(JUDSServer.class);

    private class SocketConnection {
        private InputStream is;
        private OutputStream os;
        private byte[] expected = new byte[8192];
        public SocketConnection(UnixDomainSocket pUnixDomainSocket) throws IOException {
            is = pUnixDomainSocket.getInputStream();
            os = pUnixDomainSocket.getOutputStream();
        }

        public String recieve() throws IOException {
            int read = is.read(expected);
            return new String(expected, 0, read, Charset.forName("UTF-8"));
        }

        public void send(String pSentence) {
            try {
                os.write(pSentence.getBytes(Charset.forName("UTF-8")));
            } catch (Exception exp) {
                ;
            }
        }
    }

    public JUDSServer(APIable api, String dir, int sockType) {
        this.api = api;
        this.sockType = sockType;
        this.sockFile = dir + "/metrics.sock";
        File sock = new File(this.sockFile);
        sock.deleteOnExit();
    }

    public void initUnixServer() {
        try {
            this.unixServer = new UnixDomainSocketServer(this.sockFile, this.sockType);
        } catch (Exception exp) {
            log.error("", exp);
        }
    }

    public void runServer() {
        if(this.unixServer == null)
            return;
        Thread serverThread = new Thread() {
            public void run() {
                try {
                    while(true) {
                        this.run();
                    }
                } catch (Exception exp) {
                    log.error("", exp);
                }
            }
        };
    }

    private void run() {
        try {
            UnixDomainSocket socket = this.unixServer.accept();
            SocketConnection connection = new SocketConnection(this.unixServer);
            String request = connection.recieve();
            RawsRequest raw = new RawsRequest().setJson(request);
            APIRequest apiRequest = APIRequest.parseAPIRequest(raw);
            JSONObject response = api.invokeAPI(apiRequest, null);
            connection.send(response.toString());
            socket.close();
        } catch (Exception exp) {
            log.error("",exp);
        }
    }
}
