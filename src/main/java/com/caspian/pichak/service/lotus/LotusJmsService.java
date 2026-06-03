package com.caspian.pichak.service.lotus;

import com.caspian.banking.exception.SystemException;
import com.caspian.banking.message.MessageType;
import com.caspian.banking.message.RequestType;
import com.caspian.pichak.exceptions.CoreException;
import com.caspian.pichak.service.lotus.messagecreatorprovider.MessageCreatorProvider;
import com.caspian.pichak.service.lotus.model.MessagePropertiesModel;
import com.caspian.pichak.service.lotus.receiver.MessageReceiver;
import com.caspian.pichak.utility.AAAServer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.IllegalStateException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.zip.GZIPInputStream;

@Service
public class LotusJmsService   {
    private static final Logger LOGGER = LoggerFactory.getLogger(LotusJmsService.class);

    private final JndiTemplate jndiTemplate;
    private final Environment environment;
    private final JmsTemplate jmsTemplate;
    private ConnectionFactory connectionFactory;
    private ObjectMapper objectMapper;
    private Destination requestQueue;
    private Destination responseQueue;
    private JmsTemplate template;
    private final MessageCreatorProvider messageCreatorProvider;
    private final MessageReceiver messageReceiver;

    public LotusJmsService(JndiTemplate jndiTemplate, Environment environment, MessageCreatorProvider messageCreatorProvider, MessageReceiver messageReceiver, JmsTemplate jmsTemplate) {
        this.jndiTemplate = jndiTemplate;
        this.environment = environment;
        this.messageCreatorProvider = messageCreatorProvider;
        this.messageReceiver = messageReceiver;
        this.jmsTemplate = jmsTemplate;
    }

    private static String createRandomString() {
        return Long.toHexString((new Random(System.currentTimeMillis())).nextLong());
    }

    @PostConstruct
    public void init() throws NamingException {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"));
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiTemplate(jndiTemplate);
        factoryBean.setJndiName(environment.getProperty("gateway.spi.jms.connectionFactory"));
        factoryBean.setExpectedType(ConnectionFactory.class);
        factoryBean.afterPropertiesSet();
        connectionFactory = (ConnectionFactory)factoryBean.getObject();
        JndiObjectFactoryBean jndiRequestBean = new JndiObjectFactoryBean();
        jndiRequestBean.setJndiTemplate(jndiTemplate);
        jndiRequestBean.setJndiName(environment.getProperty("gateway.spi.jms.requestQueue"));
        jndiRequestBean.setExpectedType(Destination.class);
        jndiRequestBean.afterPropertiesSet();
        requestQueue = (Destination)jndiRequestBean.getObject();
        JndiObjectFactoryBean jndiResponseBean = new JndiObjectFactoryBean();
        jndiResponseBean.setJndiTemplate(jndiTemplate);
        jndiResponseBean.setJndiName(environment.getProperty("gateway.spi.jms.responseQueue"));
        jndiResponseBean.setExpectedType(Destination.class);
        jndiResponseBean.afterPropertiesSet();
        responseQueue = (Destination)jndiResponseBean.getObject();
        template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultDestination(requestQueue);
        template.setReceiveTimeout(environment.getProperty("queue.request.timeout", Long.class));
        template.setTimeToLive(environment.getProperty("queue.request.timeout", Long.class));
        template.setSessionTransacted(true);
        template.setSessionAcknowledgeMode(1);
        template.afterPropertiesSet();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String send(String username, String branchCode, String serviceId, RequestType requestType, String input) {
        String RANDOM_STRING = createRandomString();
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        template.send(requestQueue, (session) -> {
            BytesMessage message = session.createBytesMessage();
            message.setJMSCorrelationID(RANDOM_STRING);
            message.setStringProperty("channel", environment.getProperty("lotus.core.channel"));
            message.setStringProperty("clientVersion", environment.getProperty("lotus.core.version"));
            message.setStringProperty("gatewayVersion", environment.getProperty("lotus.core.version"));
            message.setStringProperty("filter", environment.getProperty("lotus.core.filter"));
            message.setStringProperty("serviceId", serviceId);
            message.setStringProperty("payloadSchema", "object/json");
            message.setStringProperty("messageType", MessageType.REQUEST.name());
            message.setStringProperty("requestType", requestType.name());
            message.setStringProperty("securityAliasName", "lotus-host");
            message.setStringProperty("transactionType", "INPUT");
            message.setStringProperty("userCredentials", username + "@" + branchCode + ":IRR");
            message.setStringProperty("transactionId", Long.toHexString(randomLong));

            try {
                message.writeBytes(input.getBytes("utf-8"));
                return message;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return RANDOM_STRING;
    }

    public <I, O> O sendInquiry(final I inbound) throws JMSException, IOException, SystemException {
        final MessagePropertiesModel messageProperties = new MessagePropertiesModel.Builder().setRequestType(RequestType.INQUIRY).build();
        return send(inbound, messageProperties);
    }
    public <I, O> O send(final I inbound, final MessagePropertiesModel messageProperties) throws JMSException, IOException, SystemException {
        final Class<?> outboundClass = MessageHelper.INSTANCE.getOutboundClass(inbound);
//        LOGGER.debug("Sending with outbound class: " + ((outboundClass == null) ? null : outboundClass.getName()));
        return (O) send(inbound, outboundClass, messageProperties);
    }

    public <I, O> O send(final I inbound, final Class<O> outboundClass, final MessagePropertiesModel messageProperties) throws JMSException, IOException, SystemException {
        final String messageCorrelationID = send0(inbound, messageProperties);
        return receive0(messageCorrelationID, outboundClass);
    }

    protected <I> String send0(final I inbound, final MessagePropertiesModel messageProperties) {
        final String messageCorrelationID = MessageHelper.INSTANCE.createRandomString();
        final MessageCreator messageCreator = messageCreatorProvider.provide(inbound, messageProperties, messageCorrelationID);
       jmsTemplate.send(requestQueue, messageCreator);
        LOGGER.info("Message Sent successfully with correlationID:" + messageCorrelationID);
        return messageCorrelationID;
    }

    protected <O> O receive0(final String messageCorrelationID, final Class<O> outboundClass) throws JMSException, IOException, SystemException {
        final String formattedMessageCorrelationID = messageReceiver.formatMessageCorrelationID(messageCorrelationID);
        final Message receivedMessage = jmsTemplate.receiveSelected(responseQueue, formattedMessageCorrelationID);
        LOGGER.info("Message received successfully with correlationID:" + messageCorrelationID);
        return messageReceiver.handledBytesMessage(receivedMessage, outboundClass);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String send(String username, String branchCode, String serviceId, RequestType requestType, Object input) {
        String RANDOM_STRING = createRandomString();
        Random random = new Random(System.currentTimeMillis());
         template.send(requestQueue, (session) -> {
            BytesMessage message = session.createBytesMessage();
            message.setJMSCorrelationID(RANDOM_STRING);
            message.setStringProperty("channel", environment.getProperty("lotus.core.channel"));
            message.setStringProperty("clientVersion", environment.getProperty("lotus.core.version"));
            message.setStringProperty("gatewayVersion", environment.getProperty("lotus.core.version"));
            message.setStringProperty("filter", environment.getProperty("lotus.core.filter"));
            message.setStringProperty("serviceId", serviceId);
            message.setStringProperty("payloadSchema", "object/json");
            message.setStringProperty("messageType", MessageType.REQUEST.name());
            message.setStringProperty("requestType", requestType.name());
            message.setStringProperty("securityAliasName", "lotus-host");
            message.setStringProperty("transactionType", "INPUT");
            message.setStringProperty("userCredentials", username + "@" + branchCode + ":IRR");
            message.setStringProperty("transactionId", RANDOM_STRING);

            try {
                message.writeBytes(this.objectMapper.writeValueAsString(input).getBytes("utf-8"));
                return message;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return RANDOM_STRING;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T receive(String correlationId, Class<T> resultClass) throws JMSException, IOException {
        Message receive = this.template.receiveSelected(this.responseQueue, String.format("JMSCorrelationID='%s'", correlationId));
        if (receive instanceof BytesMessage) {
            String responseType = receive.getStringProperty("responseType");
            if ("FAILED".equals(responseType)) {
                String s = this.extractMessageBody(receive);
                throw new CoreException(s);
            } else {
                String s = this.extractMessageBody(receive);
                return this.objectMapper.readValue(s, resultClass);
            }
        } else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String receive(String correlationId) throws JMSException, IOException {
        Message receive = null;
        try {
            receive = template.receiveSelected(responseQueue, String.format("JMSCorrelationID='%s'", correlationId));
        }catch (Exception e) {
            System.out.println("*****************");
            System.out.println(e.getMessage());
            AAAServer.logger.error("Error receive data from JMS ", e);
        }
        if (receive instanceof BytesMessage) {
            String responseType = receive.getStringProperty("responseType");
            if ("FAILED".equals(responseType)) {
                String s = extractMessageBody(receive);
                throw new CoreException(s);
            } else {
                return extractMessageBody(receive);
            }
        } else {
            return null;
        }

    }

    private String extractMessageBody(Message receive) throws JMSException, IOException {
        if (!(receive instanceof BytesMessage)) {
            throw new IllegalStateException("Text message not supported");
        } else {
            BytesMessage bytesMessage = (BytesMessage)receive;
            byte[] buffer = new byte[(int)bytesMessage.getBodyLength()];
            bytesMessage.readBytes(buffer);
            if (!receive.getBooleanProperty("compressed")) {
                return new String(buffer, "utf-8");
            } else {
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                GZIPInputStream gis = new GZIPInputStream(bais);
                BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
                StringBuilder outStr = new StringBuilder();

                String line;
                while((line = bf.readLine()) != null) {
                    outStr.append(line);
                }

                bais.close();
                gis.close();
                bf.close();
                return outStr.toString();
            }
        }
    }
}
