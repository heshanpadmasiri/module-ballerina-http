/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.stdlib.http.transport.method.head;

import io.ballerina.stdlib.http.transport.contract.Constants;
import io.ballerina.stdlib.http.transport.contract.HttpConnectorListener;
import io.ballerina.stdlib.http.transport.message.HttpCarbonMessage;
import io.ballerina.stdlib.http.transport.message.HttpCarbonResponse;
import io.ballerina.stdlib.http.transport.message.HttpMessageDataStreamer;
import io.ballerina.stdlib.http.transport.util.TestUtil;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener to process the HEAD request related test cases.
 */
public class HeadRequestMessageProcessorListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(HeadRequestMessageProcessorListener.class);

    @Override public void onMessage(HttpCarbonMessage httpRequest) {
        Thread.startVirtualThread(() -> {
            try {
                HttpCarbonMessage httpResponse = new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                                                                                                HttpResponseStatus.OK));
                httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
                HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpResponse);
                httpRequest.respond(httpResponse);
                if (Boolean.valueOf(httpRequest.getHeader("x-large-payload"))) {
                    LOG.info("Request for large payload");
                    httpMessageDataStreamer.getOutputStream().write(TestUtil.largeEntity.getBytes());
                } else {
                    httpMessageDataStreamer.getOutputStream().write(TestUtil.smallEntity.getBytes());
                }
                httpMessageDataStreamer.getOutputStream().close();
            } catch (Exception e) {
                LOG.error("Error occurred during message notification: " + e.getMessage());
            }
        });
    }

    @Override public void onError(Throwable throwable) {
        LOG.error("Error while receiving the request", throwable);
    }
}
