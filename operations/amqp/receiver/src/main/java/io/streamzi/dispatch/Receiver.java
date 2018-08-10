/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package io.streamzi.dispatch;

import io.streamzi.openshift.dataflow.container.config.EnvironmentResolver;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.*;

public class Receiver {

    public static void main(String[] args) throws Exception {


        Connection connection = null;
        ConnectionFactory connectionFactory = new JmsConnectionFactory(EnvironmentResolver.get("broker.url"));

        try {

            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue(EnvironmentResolver.get("input-data"));
            System.out.println("Receiving from: " + queue.getQueueName());


            connection.start();

            MessageConsumer consumer = session.createConsumer(queue);

            while (true) {
                TextMessage m = (TextMessage) consumer.receive();
                System.out.println(m.getText());
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }

}
