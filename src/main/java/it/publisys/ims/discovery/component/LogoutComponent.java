/*
 * IMS SPID Discovery
 * Copyright (c) 2016 Publisys S.p.A. srl (http://www.publisys.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.publisys.ims.discovery.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>31/05/16</pre>
 */
@Component
public class LogoutComponent {

    private static final Logger _log = LoggerFactory.getLogger(LogoutComponent.class);

    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
    }

    /**
     * Effettua una richiesta di Logout
     *
     * @param url     url di logout
     * @param request {@link HttpServletRequest}
     */
    public void logout(String url, HttpServletRequest request) {
        String _cookies = _prepareCookies(request);
        _connect(url, _cookies);
    }

    /**
     * Effettua una {@link java.net.HttpURLConnection} inviando anche i cookies
     *
     * @param url     url
     * @param cookies cookies
     */
    @SuppressWarnings("ThrowFromFinallyBlock")
    private void _connect(String url, String cookies) {
        int responseCode = -1;
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] xcs, String string)
                                throws CertificateException {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] xcs, String string)
                                throws CertificateException {
                        }

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

        HttpURLConnection connection = null;
        try {
            URL _url = new URL(url);
            connection = (HttpURLConnection) _url.openConnection(Proxy.NO_PROXY);
            connection.setRequestProperty("Cookie", cookies);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            responseCode = connection.getResponseCode();
            _log.info("Logout Shibb response code: " + responseCode);

            if (responseCode == 200 && _log.isDebugEnabled()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "UTF-8")
                    );
                    StringBuilder _buffer = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        _buffer.append(line);
                    }
                    _log.debug(_buffer.toString());
                } finally {
                    if (br != null) {
                        br.close();
                    }
                }

            }

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    /**
     * Prepara i {@link Cookie}s da inoltrare
     *
     * @param request {@link HttpServletRequest}
     * @return cookies string
     */
    private String _prepareCookies(HttpServletRequest request) {
        StringBuilder _cookieBuffer = new StringBuilder();
        try {
            Cookie[] _cookies = request.getCookies();
            for (Cookie _cookie : _cookies) {
                _cookieBuffer.append(_cookie.getName()).append("=")
                        .append(URLEncoder.encode(_cookie.getValue(), "UTF-8"));
                _cookieBuffer.append("; ");
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace(System.err);
        }

        if (_cookieBuffer.length() > 2) {
            _cookieBuffer.delete(_cookieBuffer.length() - 2,
                    _cookieBuffer.length());
        }
        return _cookieBuffer.toString();
    }

}
