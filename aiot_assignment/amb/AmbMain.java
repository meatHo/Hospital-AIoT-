package aiot_assignment.amb;

import com.solacesystems.jcsmp.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

class SolaceConfig {
    public static final String HOST = "tcp://mr-connection-sz6f1qqcsll.messaging.solace.cloud:55555";
    public static final String VPN_NAME = "ajou";
    public static final String USERNAME = "LOAPP";
    public static final String PASSWORD = "1234";
    public static final String REQ_TOPIC = "REQ/STATUS";
    public static String REPLY_QUEUE;
}

public class AmbMain {
    public static void main(String[] args) throws IOException, JCSMPException {
        System.out.println("구급차 번호 : ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String ambId = br.readLine().trim();
        SolaceConfig.REPLY_QUEUE = "LOAPP." + ambId + ".REPLYQ";
        System.out.println("Setting System...");

        JCSMPProperties props = new JCSMPProperties();
        props.setProperty(JCSMPProperties.HOST, SolaceConfig.HOST);
        props.setProperty(JCSMPProperties.VPN_NAME, SolaceConfig.VPN_NAME);
        props.setProperty(JCSMPProperties.USERNAME, SolaceConfig.USERNAME);
        props.setProperty(JCSMPProperties.PASSWORD, SolaceConfig.PASSWORD);

        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(props);
        session.connect();

        // 응답용 큐 생성 및 바인딩
        Queue replyQueue = JCSMPFactory.onlyInstance().createQueue(SolaceConfig.REPLY_QUEUE);
        EndpointProperties ep = new EndpointProperties();
        ep.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        ep.setPermission(EndpointProperties.PERMISSION_CONSUME);
        session.provision(replyQueue, ep, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        // 응답 받는 리스너
        ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
        flowProps.setEndpoint(replyQueue);
        flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        FlowReceiver replyReceiver = session.createFlow(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage msg) {
                if (msg instanceof TextMessage) {
                    System.out.println("병원 응답 수신: " + ((TextMessage) msg).getText());
                    System.out.println("CorrelationID: " + msg.getCorrelationKey());
                }
                msg.ackMessage();
            }

            @Override
            public void onException(JCSMPException e) {
                System.err.println("메시지 수신 예외 발생: " + e);
            }
        }, flowProps, ep);
        replyReceiver.start();// 쓰레드 구현됨 내부에 비동기 쓰레드
        while (true) {
            System.out.println("메시지를 보내고 싶은 병원 이름을 입력하세요 : ");
            String hospitalName = br.readLine().trim().toUpperCase();
            System.out.println("환자 상태를 입력하세요 : ");
            String patientStatus = br.readLine().trim();

            // 메시지 전송
            String emergencyQueueName = "LOAPP." + hospitalName + ".EMERGENCYQ";
            Queue emergencyQueue = JCSMPFactory.onlyInstance().createQueue(emergencyQueueName);
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setText("구급차ID: " + ambId + " / 상태: " + patientStatus);
            message.setReplyTo(replyQueue); // reply-to 설정
            message.setCorrelationKey(UUID.randomUUID().toString()); // 식별자 설정

            XMLMessageProducer producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
                public void responseReceived(String messageID) {
                    System.out.println("요청 메시지 전송 성공: " + messageID);
                }

                public void handleError(String messageID, JCSMPException e, long timestamp) {
                    System.err.println("전송 실패: " + e);
                }
            });
            producer.send(message, emergencyQueue);

        }
    }
}
