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

import io.streamzi.openshift.dataflow.container.config.JMSResolver;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public class Receiver {

    public static void main(String[] args) {

        try {

            Properties props = JMSResolver.getJMSProperties();
            Context context = new InitialContext(props);

            ConnectionFactory factory = (ConnectionFactory) context.lookup("factory");
            Destination queue = (Destination) context.lookup("queue");

            Connection connection = factory.createConnection();
            connection.setExceptionListener(new MyExceptionListener());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer messageConsumer = session.createConsumer(queue);

            while (true) {

                Message message = messageConsumer.receive();
                TextMessage tm = (TextMessage) message;

                if (message == null) {
                    System.out.println("Null message received, stopping.");
                    break;
                }

                System.out.println("tm.getText() = " + tm.getText());

                message.acknowledge();
            }

            connection.close();
        } catch (Exception exp) {
            System.out.println("Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static class MyExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
            System.out.println("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
