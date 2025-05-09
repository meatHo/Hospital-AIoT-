
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageProducer;
import java.util.*;
import java.io.*;

// class stopth {
//     private volatile boolean running = true;

//     public void stopRunning() {
//         this.running = false;
//     }
// }

class PUB implements Runnable {// 구급차에 보냄
    private static volatile boolean running = true;

    public void run() {

    }

    public static void stopRunning() {
        running = false;
    }
}

class SUB implements Runnable {// 정보 받음
    private volatile boolean running = true;
    private final Queue NormalQueue;
    private final JCSMPSession NormalSession;
    private final FlowReceiver consumer;

    public SUB(Queue normalQueue, JCSMPSession normalSession, FlowReceiver cons) {
        NormalQueue = normalQueue;
        NormalSession = normalSession;
        consumer = cons;
    }

    public void run() {
        try {
            consumer.start();
        } catch (JCSMPException e) {
            System.out.println("error happened : " + e);
            return;
        }
        while (running) {
            BytesXMLMessage msg;
            try {
                msg = consumer.receive(1000); // wait max 10 minutes for a message
            } catch (JCSMPException e) {
                System.out.println("error happened : " + e);
                return;
            }
            if (msg != null) {// String getDestination();이게 토픽 이름 반환하는거임
                // System.out.printf("Message received!%n%s%n", msg.dump());
                String pathTemp = msg.getDestination().toString().trim();
                System.out.println(pathTemp);// LOAPP/test/home
                msg.ackMessage();
                String[] pathTempArr = pathTemp.split("/");// [0]은 병원이름으로 각 병원마다 큐가 있는 것
                // [1]부터 구분하면 됨
            }
        }
    }

    public void stopRunning() {
        running = false;
    }

}

public class HospitalMain {
    private static Queue NormalQueue;
    private static FlowReceiver cons;
    private static JCSMPSession NormalSession;
    private static JCSMPSession ExclusiveSession;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Setting System...");
        System.out.print("병원 이름을 영어로 입력하세요 : ");// ex) ajou
        String hospitalName = br.readLine().trim();
        try {
            if (!QueueBootstrap(hospitalName)) {
                System.out.println("booting the app failed... try again");
                return;
            }
        } catch (JCSMPException e) {
            System.err.println("An error occurred during queue bootstrap: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println("Setting Complete!");// ------------------------------------START

        SUB sub = new SUB(NormalQueue, NormalSession, cons);
        Thread subTh = new Thread(sub);
        // Thread subPatTh = new Thread(new SUBpatient());
        subTh.start();
        // subPatTh.start();

        while (true) {
            System.out.println("응급 상황이면 신호를 줍니다. 종료하려면 \"!quit\"을 입력하세요");
            String input = br.readLine().trim();
            if (input.equals("!quit")) {
                br.close();
                sub.stopRunning();
                PUB.stopRunning();
                break;
            }

            // 여기다가 응급 상황 큐 구독해놓음
            // 환자센서에서 오는 것은 바로 의료진 앱으로 가도록 하는게 낳을듯

        }
        System.out.println("시스템 종료");
    }

    private static boolean QueueBootstrap(String hospitalName) throws JCSMPException {
        // ---------------------------normal queue(emergency messages)
        // Create a JCSMP Session
        final JCSMPProperties properties1 = new JCSMPProperties();
        properties1.setProperty(JCSMPProperties.HOST, "tcp://mr-connection-sz6f1qqcsll.messaging.solace.cloud:55555"); // msg-backbone
                                                                                                                       // ip:port

        properties1.setProperty(JCSMPProperties.VPN_NAME, "ajou"); // message-vpn
        properties1.setProperty(JCSMPProperties.USERNAME, "LOAPP");
        properties1.setProperty(JCSMPProperties.PASSWORD, "1234");
        String queueName1 = "NORMALQ.LOAPP";
        NormalSession = JCSMPFactory.onlyInstance().createSession(properties1);

        final EndpointProperties endpointProps1 = new EndpointProperties();
        // queue를 consume과 nonexclusive로 설정
        endpointProps1.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps1.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);

        NormalQueue = JCSMPFactory.onlyInstance().createQueue(queueName1);
        NormalSession.provision(NormalQueue, endpointProps1, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        // 큐가 없다면 생성하고 이미 있으면 무시

        final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
        flow_prop.setEndpoint(NormalQueue);
        flow_prop.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        EndpointProperties endpoint_props = new EndpointProperties();
        endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
        cons = NormalSession.createFlow(null, flow_prop, endpoint_props);

        // ---------------------------exclusive queue(message to the amb)
        final JCSMPProperties properties2 = new JCSMPProperties();
        properties2.setProperty(JCSMPProperties.HOST, "tcp://mr-connection-sz6f1qqcsll.messaging.solace.cloud:55555"); // msg-backbone
                                                                                                                       // ip:port
        properties2.setProperty(JCSMPProperties.VPN_NAME, "ajou"); // message-vpn
        properties2.setProperty(JCSMPProperties.USERNAME, "LOAPP");
        properties2.setProperty(JCSMPProperties.PASSWORD, "1234");

        // String queueName2 = hospitalName + "." + "EXCLUSIVEQ.LOAPP";
        // ExclusiveSession = JCSMPFactory.onlyInstance().createSession(properties2);
        // final EndpointProperties endpointProps2 = new EndpointProperties();
        // endpointProps2.setPermission(EndpointProperties.PERMISSION_CONSUME);
        // endpointProps2.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

        // ExclusiveSession.provision(ExclusiveQueue, endpointProps2,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        // ExclusiveQueue = JCSMPFactory.onlyInstance().createQueue(queueName2);
        // ExclusiveSession.provision(ExclusiveQueue, endpointProps2,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        // ExclusiveSession.connect();
        ExclusiveSession = JCSMPFactory.onlyInstance().createSession(properties2);
        ExclusiveSession.connect();
        return true;
    }
}
