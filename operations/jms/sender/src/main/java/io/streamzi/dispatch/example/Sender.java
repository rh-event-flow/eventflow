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
package io.streamzi.dispatch.example;

import io.streamzi.openshift.dataflow.container.config.JMSResolver;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import java.util.Random;

public class Sender {

    private static final int DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;

    public static void main(String[] args) {

        try {

            Properties props = JMSResolver.getJMSProperties();

            Context context = new InitialContext(props);

            ConnectionFactory factory = (ConnectionFactory) context.lookup("factory");
            Destination queue = (Destination) context.lookup("queue");

            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer messageProducer = session.createProducer(queue);

            System.out.println("About to start sending messages");

            Random random = new Random();
            while (true) {
                String randomNumber = String.valueOf(random.nextDouble());
                TextMessage message = session.createTextMessage(randomNumber);

                messageProducer.send(message, DELIVERY_MODE, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
                System.out.println(randomNumber);

                Thread.sleep(2 * 1000);

            }

        } catch (Exception exp) {
            System.out.println("Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
