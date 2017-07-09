package no.susoft.mobile.pos.network.auth;

import cz.msebera.android.httpclient.*;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.client.methods.RequestBuilder;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpDigestAuth {

    public static int ncounter = 0;

    public static String get(String protocol, String host, int port,
                             String uristring, String user, String password, String contentType, String charset, String xml) throws Exception {

        return call("GET", protocol, host, port, uristring, user, password, contentType, charset, xml);
    }

    public static String post(String protocol, String host, int port,
                              String uristring, String user, String password, String contentType, String charset, String xml) throws Exception {

        return call("POST", protocol, host, port, uristring, user, password, contentType, charset, xml);
    }

    public static String call(String method, String protocol, String host, int port,
                              String uristring, String user, String password, String contentType, String charset, String xml) throws Exception {

        HttpHost target = new HttpHost(host, port, protocol);

        CloseableHttpClient httpclient = null;

        try {
            if (charset == null || charset.trim().length() == 0 || !Charset.isSupported(charset))
                charset = "ISO-8859-1";
            Charset charsetToUse = Charset.forName(charset);

            httpclient = HttpClients.createDefault();

            URI uri = new URIBuilder(uristring)
                    .setCharset(charsetToUse)
                    .build();

            RequestBuilder builder = RequestBuilder
                    .post()
                    .setUri(uri);
            if (xml != null) builder.setEntity(new ByteArrayEntity(xml.getBytes(charsetToUse)));

            builder.setCharset(charsetToUse);

            if (contentType == null || contentType.trim().length() == 0)
                builder.setHeader(HttpHeaders.CONTENT_TYPE, contentType);

            HttpUriRequest httpUriReq = builder.build();

            HttpResponse response = httpclient.execute(target, httpUriReq);

            switch (response.getStatusLine().getStatusCode()) {

                case 401: // auth
                    Header[] authHeaders = response.getHeaders("WWW-Authenticate");
                    Header authHeader = authHeaders[0];
                    System.out.println("authHeader = " + authHeader.getValue());

                    StringBuilder authorizationHaderValue = createAuthorizationHeader(uristring, user, password, method, authHeader);

                    System.out.println("authorizationHaderValue=" + authorizationHaderValue.toString());

                    httpUriReq.addHeader("Authorization", authorizationHaderValue.toString());

                    response = httpclient.execute(target, httpUriReq);
                    System.out.println("Auth status: " + response.getStatusLine().getStatusCode());

                    switch (response.getStatusLine().getStatusCode()) {

                        case 401: // still unauthorized
                            throw new HttpException("Unauthorized");
                        case 200: // ok
                            return readResultStreamString(response.getEntity(), httpclient);
                    }
                    break;

                case 200: // ok
                    return readResultStreamString(response.getEntity(), httpclient);

                default:
                    return "Status:" + response.getStatusLine().getStatusCode();
            }
        } finally {
            if (httpclient != null) httpclient.close();
        }

        return "";
    }

    private static StringBuilder createAuthorizationHeader(String uri, String user, String password, String method, Header authHeader) throws Exception {
        Map<String, String> maps = new HashMap<>();

        HeaderElement[] authHeaderElements = authHeader.getElements();
        for (HeaderElement authHeaderElement : authHeaderElements) {

            String authHeaderElementName = authHeaderElement.getName();
            if (authHeaderElementName.startsWith("Digest")) {
                authHeaderElementName = authHeaderElementName.substring(7);
            }

            maps.put(authHeaderElementName, authHeaderElement.getValue());
        }

        maps.put("method", method);

        maps.put("username", user);
        maps.put("password", password);

        maps.put("nc", "" + ++ncounter);
        maps.put("cnonce", System.currentTimeMillis() + "");
        maps.put("uri", uri);
        maps.put("response", makeResponse(maps));

        return new StringBuilder()
                .append("Digest username=\"").append(maps.get("username")).append("\", ")
                .append("realm=\"").append(maps.get("realm")).append("\", ")
                .append("nonce=\"").append(maps.get("nonce")).append("\", ")
                .append("uri=\"").append(maps.get("uri")).append("\", ")
                .append("algorithm=MD5, ")
                .append("response=\"").append(maps.get("response")).append("\", ")
                .append("opaque=\"").append(maps.get("opaque")).append("\", ")
                .append("qop=").append(maps.get("qop")).append(", ")
                .append("nc=").append(maps.get("nc")).append(", ")
                .append("cnonce=\"").append(maps.get("cnonce")).append("\"");
    }

    protected static String readResultStreamString(HttpEntity httpEntity, CloseableHttpClient defaultHttpClient) throws IOException {

        String result;
        InputStream resultStream = null;

        ByteArrayOutputStream outputStream = null;

        try {
            resultStream = httpEntity.getContent();

            outputStream = new ByteArrayOutputStream(45555);

            byte[] temp = new byte[4096];
            int length = resultStream.read(temp);
            while (length > 0) {
                outputStream.write(temp, 0, length);

                length = resultStream.read(temp);
            }

            defaultHttpClient.close();
        } catch (IOException ioEx) {
            throw new IOException(ioEx);
        } finally {
            if (null != resultStream) {
                try {
                    resultStream.close();
                } catch (Exception ignored) {
                }
            }
        }

        result = outputStream.toString();
        return result;
    }

    public static String makeResponse(Map<String, String> maps) throws Exception {

        String HA1 = MD5Helper.encrypt(maps.get("username") + ":" + maps.get("realm") + ":" + maps.get("password"));

        String HA2 = MD5Helper.encrypt(maps.get("method") + ":" + maps.get("uri"));

        return MD5Helper.encrypt(HA1 + ":" +
                maps.get("nonce") + ":" +
                maps.get("nc") + ":" +
                maps.get("cnonce") + ":" +
                maps.get("qop") + ":" +
                HA2);
    }

    public static void main(String[] args) throws Exception {

        System.out.println(post("http", "192.168.100.88", 8081, "/xccsp", "pos123", "pos123", "text/xml", "ISO-8859-1",
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                        "<request xmlns=\"http://www.retailinnovation.se/xccsp\">\n" +
                        "<type>RequestStatus</type>\n" +
                        "<data>\n" +

                        "<StatusRequest>\n" +
                        "<PosId>123</PosId>\n" +
                        "<OrgNo>5566778899</OrgNo>\n" +
                        "</StatusRequest>\n" +

                        "</data>\n" +
                        "</request>"));

        //System.out.println(get("http", "httpbin.org", 80, "/digest-auth/auth/user/passwd", "user", "passwd", null, null, null));

        /*
        Authorization
        Digest username="admin", realm="CleanCash Server", nonce="13812c965b754e78b1f5f53787b829fb", uri="/xccsp"
                , algorithm=MD5, response="c0f20de21e814fd13ef79057cbcbf691", opaque="b444a613d34c469696db2acec5ee36e9"
                , qop=auth, nc=00000002, cnonce="288857e745b21221"
         */
    }
}
