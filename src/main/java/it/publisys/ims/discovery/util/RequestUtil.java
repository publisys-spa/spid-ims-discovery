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

package it.publisys.ims.discovery.util;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>10/02/16</pre>
 */
public class RequestUtil {

    public static String getServerUrl(HttpServletRequest request)
            throws MalformedURLException, URISyntaxException {
        URL url = new URL(request.getRequestURL().toString());
        String host = url.getHost();
        String scheme = url.getProtocol();
        int port = url.getPort();
        String path = request.getContextPath();

        URI uri = new URI(scheme, null, host, port, path, null, null);
        return uri.toString();
    }

    public static void printAll(HttpServletRequest request) {
        printHeader(request);
        printAttribute(request);
        printParameter(request);
    }

    public static void printHeader(HttpServletRequest request) {
        String name, value;

        Enumeration<String> e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            name = e.nextElement();
            value = request.getHeader(name);

            System.out.println(String.format("H %s = %s", name, value));
        }
    }

    public static void printAttribute(HttpServletRequest request) {
        String name, value;

        Enumeration<String> e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            name = e.nextElement();
            value = request.getAttribute(name) + "";

            System.out.println(String.format("A %s = %s", name, value));
        }
    }

    public static void printParameter(HttpServletRequest request) {
        String name, value;

        Enumeration<String> e = request.getParameterNames();
        while (e.hasMoreElements()) {
            name = e.nextElement();
            value = request.getParameter(name);

            System.out.println(String.format("P %s = %s", name, value));
        }
    }

}