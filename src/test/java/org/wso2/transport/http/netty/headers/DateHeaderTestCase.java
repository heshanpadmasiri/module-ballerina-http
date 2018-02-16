/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.headers;

import io.netty.handler.codec.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Test case for ensuring that the Date header is correctly set.
 */
public class DateHeaderTestCase {

    private ServerConnector serverConnector;
    private ListenerConfiguration listenerConfiguration;

    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));

    @BeforeClass
    public void setup() throws InterruptedException {
        listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);

        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());
        HttpWsConnectorFactory httpWsConnectorFactory = new HttpWsConnectorFactoryImpl();

        serverConnector = httpWsConnectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new EchoMessageListener());

        serverConnectorFuture.sync();
    }

    @Test
    public void testDateHeaderFormatAndExistence() throws IOException {
        HttpURLConnection connection = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), false);
        connection.getOutputStream().write("Hello World!".getBytes());
        String date = connection.getHeaderField(Constants.DATE);

        Assert.assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_OK);
        Assert.assertNotNull(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date));
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        serverConnector.stop();
    }
}
