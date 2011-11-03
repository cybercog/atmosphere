/*
* Copyright 2011 Jeanfrancois Arcand
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/
package org.atmosphere.websocket.protocol;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.websocket.WebSocketProcessor;
import org.atmosphere.websocket.WebSocketProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * Like the {@link org.atmosphere.cpr.AsynchronousProcessor} class, this class is responsible for dispatching WebSocket messages to the
 * proper {@link org.atmosphere.websocket.WebSocket} implementation by wrapping the Websocket message's bytes within
 * an {@link javax.servlet.http.HttpServletRequest}.
 * <p/>
 * The content-type is defined using {@link org.atmosphere.cpr.ApplicationConfig#WEBSOCKET_CONTENT_TYPE} property
 * The method is defined using {@link org.atmosphere.cpr.ApplicationConfig#WEBSOCKET_METHOD} property
 * <p/>
 *
 * @author Jeanfrancois Arcand
 */
public class SimpleHttpProtocol implements WebSocketProtocol, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AtmosphereServlet.class);
    private String contentType;
    private String methodType;
    private String delimiter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(AtmosphereServlet.AtmosphereConfig config) {
        String contentType = config.getInitParameter(ApplicationConfig.WEBSOCKET_CONTENT_TYPE);
        if (contentType == null) {
            contentType = "text/html";
        }
        this.contentType = contentType;

        String methodType = config.getInitParameter(ApplicationConfig.WEBSOCKET_METHOD);
        if (methodType == null) {
            methodType = "POST";
        }
        this.methodType = methodType;

        String delimiter = config.getInitParameter(ApplicationConfig.WEBSOCKET_PATH_DELIMITER);
        if (delimiter == null) {
            delimiter = "@@";
        }
        this.delimiter = delimiter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpServletRequest onMessage(AtmosphereResource<HttpServletRequest, HttpServletResponse> resource, String d) {
        String pathInfo = resource.getRequest().getPathInfo();
        if (d.startsWith(delimiter)) {
            String[] token = d.split(delimiter);
            pathInfo = token[1];
            d = token[2];
        }

        return new AtmosphereRequest.Builder()
                .request(resource.getRequest())
                .method(methodType)
                .contentType(contentType)
                .body(d)
                .pathInfo(pathInfo)
                .headers(WebSocketProcessor.configureHeader(resource.getRequest()))
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpServletRequest onMessage(AtmosphereResource<HttpServletRequest, HttpServletResponse> resource, byte[] d, final int offset, final int length) {
        return onMessage(resource, new String(d, offset, length));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(AtmosphereResource<HttpServletRequest, HttpServletResponse> resource) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(AtmosphereResource<HttpServletRequest, HttpServletResponse> resource) {
    }

}

