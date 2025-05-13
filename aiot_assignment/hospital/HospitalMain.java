
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
import com.solacesystems.jcsmp.XMLMessageListener;
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

// class SUB implements Runnable {// 정보 받음 일반 기기 업데이트 & 비상신호 수신 후 전파
// private volatile boolean running = true;
// private final Queue NormalQueue;
// private final JCSMPSession NormalSession;
// private final FlowReceiver consumer;

// public SUB(Queue normalQueue, JCSMPSession normalSession, FlowReceiver cons)
// {
// NormalQueue = normalQueue;
// NormalSession = normalSession;
// consumer = cons;
// }

// public void run() {
// try {
// consumer.start();
// } catch (JCSMPException e) {
// System.out.println("error happened : " + e);
// return;
// }
// while (running) {
// BytesXMLMessage msg;
// try {
// msg = consumer.receive(1000); // wait max 10 minutes for a message
// } catch (JCSMPException e) {
// System.out.println("error happened : " + e);
// return;
// }
// if (msg != null) {// String getDestination();이게 토픽 이름 반환하는거임
// // System.out.printf("Message received!%n%s%n", msg.dump());
// String pathTemp = msg.getDestination().toString().trim();
// System.out.println(pathTemp);// LOAPP/test/home
// msg.ackMessage();
// String[] pathTempArr = pathTemp.split("/");// [0]은 병원이름으로 각 병원마다 큐가 있는 것
// if (pathTempArr[1].equals("AMB")) {
// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
// System.out.print("구급차 ID : " + pathTempArr[2] + "\n");
// System.out.println(((TextMessage) msg).getText());
// } else if (pathTempArr[1].equals("PATIENT")) {

// } else if (pathTempArr[1].equals("ALERT")) {

// }
// // [1]부터 구분하면 됨
// }
// }
// }

// public void stopRunning() {
// running = false;
// }

// }

// class SUBAMB implements Runnable {// 정보 받음
// private volatile boolean running = true;
// private final Queue EmergencyQueue;
// private final JCSMPSession EmergencySession;
// private final FlowReceiver consumer;

// public SUBAMB(Queue normalQueue, JCSMPSession normalSession, FlowReceiver
// cons) {
// EmergencyQueue = normalQueue;
// EmergencySession = normalSession;
// consumer = cons;
// }

// public void run() {
// try {
// consumer.start();
// } catch (JCSMPException e) {
// System.out.println("error happened : " + e);
// return;
// }
// while (running) {
// BytesXMLMessage msg;
// try {
// msg = consumer.receive(1000); // wait max 10 minutes for a message
// } catch (JCSMPException e) {
// System.out.println("error happened : " + e);
// return;
// }
// if (msg != null) {// String getDestination();이게 토픽 이름 반환하는거임
// // System.out.printf("Message received!%n%s%n", msg.dump());
// String pathTemp = msg.getDestination().toString().trim();
// System.out.println(pathTemp);// LOAPP/test/home
// msg.ackMessage();
// String[] pathTempArr = pathTemp.split("/");// [0]은 병원이름으로 각 병원마다 큐가 있는 것
// if (pathTempArr[1].equals("AMB")) {
// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
// System.out.print("구급차 ID : " + pathTempArr[2] + "\n");
// System.out.println(((TextMessage) msg).getText());
// } else if (pathTempArr[1].equals("PATIENT")) {

// } else if (pathTempArr[1].equals("ALERT")) {

// }
// // [1]부터 구분하면 됨
// }
// }
// }

// public void stopRunning() {
// running = false;
// }

// }

public class HospitalMain {
    private static Queue NormalQueue;
    private static FlowReceiver cons;
    private static JCSMPSession NormalSession;

    private static Queue EmergencyQueue;
    private static FlowReceiver cons2;
    private static JCSMPSession EmergencySession;
    private static Map<Long, List<Patient>> patientLog;
    // 메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인

    public static void main(String[] args) throws IOException, JCSMPException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Setting System...");
        String hospitalNameU = new String();
        patientLog = new HashMap<>();
        while (true) {
            System.out.print("병원 이름을 영어로 입력하세요 : ");// ex) ajou
            hospitalNameU = br.readLine().trim();
            if (isEnglishOnly(hospitalNameU)) {
                break;
            } else {
                System.out.println("영어 외의 문자가 포함되어 있습니다. 다시 입력하세요");
                continue;
            }
        }
        String hospitalName = hospitalNameU.toUpperCase();// 모두 대문자로 변경

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

        // SUB sub = new SUB(NormalQueue, NormalSession, cons);
        // Thread subTh = new Thread(sub);
        // subTh.start();
        cons.start();// normalq
        cons2.start();//

        while (true) {
            System.out.println("응급 상황이면 신호를 줍니다. 종료하려면 \"!quit\"을 입력하세요.\n환자 상태 확인 1\n긴급호출 2");
            String input = br.readLine().trim();
            if (input.equals("!quit")) {
                br.close();
                cons.close();
                cons2.close();
                break;
            } else if (Integer.parseInt(input) == 1) {
                System.out.println("환자 상태 확인 메뉴입니다. 확인하고 싶은 환자의 ID를 입력하세요");
                long temp = Long.parseLong(br.readLine().trim());
                System.out.println("== 환자 ID: " + temp + " ==");
                List<Patient> history = patientLog.get(temp);
                if (history != null) {
                    int start = Math.max(history.size() - 50, 0);
                    for (int i = start; i < history.size(); i++) {
                        System.out.println(history.get(i));
                    }
                } else {
                    System.out.println("해당 ID의 환자 기록이 없습니다.");
                }

            } else if (Integer.parseInt(input) == 2) {

            }

            // 여기다가 응급 상황 큐 구독해놓음
            // 환자센서에서 오는 것은 바로 의료진 앱으로 가도록 하는게 낳을듯

        }
        System.out.println("시스템 종료");
    }

    // 메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인메인

    private static boolean QueueBootstrap(String hospitalName) throws JCSMPException {
        // ---------------------------normal queue
        // Create a JCSMP Session
        final JCSMPProperties properties1 = new JCSMPProperties();
        properties1.setProperty(JCSMPProperties.HOST, "tcp://mr-connection-sz6f1qqcsll.messaging.solace.cloud:55555"); // msg-backbone
                                                                                                                       // ip:port

        properties1.setProperty(JCSMPProperties.VPN_NAME, "ajou"); // message-vpn
        properties1.setProperty(JCSMPProperties.USERNAME, "LOAPP");
        properties1.setProperty(JCSMPProperties.PASSWORD, "1234");
        String queueName1 = "LOAPP." + hospitalName + ".NORMALQ";
        NormalSession = JCSMPFactory.onlyInstance().createSession(properties1);

        final EndpointProperties endpointProps1 = new EndpointProperties();
        // queue를 consume과 nonexclusive로 설정
        endpointProps1.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps1.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);

        NormalQueue = JCSMPFactory.onlyInstance().createQueue(queueName1);
        // NormalSession.provision(NormalQueue, endpointProps1,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        // 큐가 없다면 생성하고 이미 있으면 무시

        // try {
        // NormalSession.provision(NormalQueue, endpointProps1,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        // // 큐에 토픽을 매핑
        // String topicString = "hospital/" + hospitalName + "/AMB/>"; // 예시 토픽
        // Topic topic = JCSMPFactory.onlyInstance().createTopic(topicString);
        // NormalSession.addSubscription(NormalQueue, topic,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        // topicString = "hospital/" + hospitalName + "/ALERT/>"; // 여기서는 patient에게 비상오면
        // 받는거
        // topic = JCSMPFactory.onlyInstance().createTopic(topicString);
        // NormalSession.addSubscription(NormalQueue, topic,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        // System.out.println("큐와 토픽 구독을 설정했습니다: " + topicString);

        // } catch (JCSMPException e) {
        // e.printStackTrace();
        // }

        NormalSession = JCSMPFactory.onlyInstance().createSession(properties1);

        final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
        flow_prop.setEndpoint(NormalQueue);
        flow_prop.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        EndpointProperties endpoint_props = new EndpointProperties();
        endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
        cons = NormalSession.createFlow(new XMLMessageListener() {
            public void onReceive(BytesXMLMessage msg) {
                // System.out.println("메시지 수신: " + msg.dump());
                if (msg != null) {// String getDestination();이게 토픽 이름 반환하는거임
                    // System.out.printf("Message received!%n%s%n", msg.dump());
                    String pathTemp = msg.getDestination().toString().trim();
                    // System.out.println(pathTemp);// LOAPP/test/home
                    msg.ackMessage();
                    String[] pathTempArr = pathTemp.split("/");// [0]은 병원이름으로 각 병원마다 큐가 있는 것
                    if (pathTempArr[1].equals("PATIENT")) {
                        long id = Long.parseLong(pathTempArr[2]);
                        // 메시지 본문 예시: "홍길동,심박수 120"
                        String payload = ((TextMessage) msg).getText();
                        String[] parts = payload.split(",", 2); // [0]: 이름, [1]: 상태정보

                        if (parts.length == 2) {
                            String name = parts[0].trim();
                            String status = parts[1].trim();
                            Patient newStatus = new Patient(name, status);

                            patientLog.putIfAbsent(id, new ArrayList<>());
                            patientLog.get(id).add(newStatus);

                            System.out.println("기록 저장됨: " + newStatus);
                        } else {
                            System.out.println("메시지 형식 오류: " + payload);
                        }

                    }
                }

                msg.ackMessage(); // CLIENT_ACK 모드일 경우 필수
            }

            public void onException(JCSMPException e) {
                System.err.println("에러 발생: " + e);
            }
        }, flow_prop, endpoint_props);

        // ----------------------------emergency queue
        // Create a JCSMP Session
        final JCSMPProperties properties2 = new JCSMPProperties();
        properties2.setProperty(JCSMPProperties.HOST, "tcp://mr-connection-sz6f1qqcsll.messaging.solace.cloud:55555"); // msg-backbone
                                                                                                                       // ip:port

        properties2.setProperty(JCSMPProperties.VPN_NAME, "ajou"); // message-vpn
        properties2.setProperty(JCSMPProperties.USERNAME, "LOAPP");
        properties2.setProperty(JCSMPProperties.PASSWORD, "1234");
        String queueName2 = "LOAPP." + hospitalName + ".EMERGENCYQ";
        EmergencySession = JCSMPFactory.onlyInstance().createSession(properties2);

        final EndpointProperties endpointProps2 = new EndpointProperties();
        // queue를 consume과 exclusive로 설정
        endpointProps2.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps2.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

        EmergencyQueue = JCSMPFactory.onlyInstance().createQueue(queueName1);
        // NormalSession.provision(NormalQueue, endpointProps1,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        // 큐가 없다면 생성하고 이미 있으면 무시

        // try {
        // NormalSession.provision(NormalQueue, endpointProps1,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        // // 큐에 토픽을 매핑
        // String topicString = "hospital/" + hospitalName + "/AMB/>"; // 예시 토픽
        // Topic topic = JCSMPFactory.onlyInstance().createTopic(topicString);
        // NormalSession.addSubscription(NormalQueue, topic,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        // topicString = "hospital/" + hospitalName + "/ALERT/>"; // 여기서는 patient에게 비상오면
        // 받는거
        // topic = JCSMPFactory.onlyInstance().createTopic(topicString);
        // NormalSession.addSubscription(NormalQueue, topic,
        // JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        // System.out.println("큐와 토픽 구독을 설정했습니다: " + topicString);

        // } catch (JCSMPException e) {
        // e.printStackTrace();
        // }

        NormalSession = JCSMPFactory.onlyInstance().createSession(properties1);

        final ConsumerFlowProperties flow_prop2 = new ConsumerFlowProperties();
        flow_prop2.setEndpoint(EmergencyQueue);
        flow_prop2.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        EndpointProperties endpoint_props2 = new EndpointProperties();
        endpoint_props2.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
        cons2 = NormalSession.createFlow(new XMLMessageListener() {
            public void onReceive(BytesXMLMessage msg) {
                System.out.println("메시지 수신: " + msg.dump());
                if (msg != null) {// String getDestination();이게 토픽 이름 반환하는거임
                    // System.out.printf("Message received!%n%s%n", msg.dump());
                    String pathTemp = msg.getDestination().toString().trim();
                    // System.out.println(pathTemp);// LOAPP/test/home
                    msg.ackMessage();
                    String[] pathTempArr = pathTemp.split("/");// [0]은 병원이름으로 각 병원마다 큐가 있는 것
                    if (pathTempArr[1].equals("PATIENT")) {

                    }
                }

                msg.ackMessage(); // CLIENT_ACK 모드일 경우 필수
            }

            public void onException(JCSMPException e) {
                System.err.println("에러 발생: " + e);
            }
        }, flow_prop2, endpoint_props2);

        // ---------------------------exclusive queue(message to the amb)
        // final JCSMPProperties properties2 = new JCSMPProperties();
        // properties2.setProperty(JCSMPProperties.HOST,
        // "tcp://mr-connection-sz6f1qqcsll.messaging.solace.cloud:55555"); //
        // msg-backbone
        // // ip:port
        // properties2.setProperty(JCSMPProperties.VPN_NAME, "ajou"); // message-vpn
        // properties2.setProperty(JCSMPProperties.USERNAME, "LOAPP");
        // properties2.setProperty(JCSMPProperties.PASSWORD, "1234");

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
        // ExclusiveSession = JCSMPFactory.onlyInstance().createSession(properties2);
        // ExclusiveSession.connect();
        return true;
    }

    public static boolean isEnglishOnly(String str) {
        return str.matches("[a-zA-Z]+");
    }
}
