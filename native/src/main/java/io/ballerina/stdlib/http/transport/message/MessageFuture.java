/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.http.transport.message;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents future contents of the message.
 */
public class MessageFuture {

    private static final Logger LOG = LoggerFactory.getLogger(MessageFuture.class);
    private MessageListener messageListener;
    private final HttpCarbonMessage httpCarbonMessage;
    private final ReentrantLock futureLock = new ReentrantLock();

    public MessageFuture(HttpCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    public void setMessageListener(MessageListener messageListener) {
        futureLock.lock();
        try {
            this.messageListener = messageListener;
            while (!httpCarbonMessage.isEmpty()) {
                HttpContent httpContent = httpCarbonMessage.getHttpContent();
                notifyMessageListener(httpContent);
                if (httpContent instanceof LastHttpContent) {
                    this.httpCarbonMessage.removeMessageFuture();
                    return;
                }
            }

            // Removes Inbound throttling listener during passthrough so that only backpressure handling would be
            // present.
            if (httpCarbonMessage.isPassthrough()) {
                httpCarbonMessage.removeInboundContentListener();
            }
        } finally {
            futureLock.unlock();
        }
    }

    void notifyMessageListener(HttpContent httpContent) {
        if (this.messageListener != null) {
            this.messageListener.onMessage(httpContent);
        } else {
            LOG.error("The message chunk will be lost because the MessageListener is not set.");
        }
    }

    public boolean isMessageListenerSet() {
        return messageListener != null;
    }

    public HttpContent sync() {
        return this.httpCarbonMessage.getBlockingEntityCollector().getHttpContent();
    }
}
