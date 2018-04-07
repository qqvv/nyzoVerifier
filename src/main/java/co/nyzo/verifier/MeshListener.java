package co.nyzo.verifier;

import co.nyzo.verifier.messages.*;
import co.nyzo.verifier.util.IpUtil;
import co.nyzo.verifier.util.UpdateUtil;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class MeshListener {

    public static void main(String[] args) {
        start();
    }

    private static final AtomicBoolean alive = new AtomicBoolean(false);

    public static boolean isAlive() {
        return alive.get();
    }

    public static final int standardPort = 9444;

    private static ServerSocket serverSocket = null;
    private static int port;

    public static int getPort() {
        return port;
    }

    public static void start() {

        if (!alive.getAndSet(true)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        serverSocket = new ServerSocket(standardPort);
                        port = serverSocket.getLocalPort();

                        // Now is the appropriate time to add this node to the verifier list. The verifier wallet was
                        // set before this mesh listener was started, and the IP address can be determined at any time.
                        NodeManager.updateNode(Verifier.getIdentifier(), IpUtil.myPublicIp(), port, true);

                        System.out.println("actual port is " + port);
                        while (!UpdateUtil.shouldTerminate()) {
                            Socket clientSocket = serverSocket.accept();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        Message message = Message.readFromStream(clientSocket.getInputStream(),
                                                IpUtil.addressFromString(clientSocket.getRemoteSocketAddress() + ""));
                                        System.out.println("got message of type: " + message.getType());
                                        Message response = response(message);
                                        System.out.println("response is " + response);
                                        if (response != null) {
                                            clientSocket.getOutputStream().write(response.getBytesForTransmission());
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        clientSocket.close();
                                    } catch (Exception ignored) { }
                                }
                            }).start();
                        }

                        closeServerSocket();

                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }

                    alive.set(false);
                }
            }).start();
        }
    }

    public static void closeServerSocket() {

        try {
            serverSocket.close();
        } catch (Exception ignored) { }
        serverSocket = null;
    }

    public static Message response(Message message) {

        // This is the single point of dispatch for responding to all received messages.

        Message response = null;

        try {
            if (message != null && message.isValid()) {
                MessageType messageType = message.getType();
                System.out.println("message type is " + messageType);

                if (messageType == MessageType.NodeListRequest1) {

                    System.out.println("returning NodeListResponse");
                    response = new Message(MessageType.NodeListResponse2, new NodeListResponse(NodeManager.getNodePool()));

                } else if (messageType == MessageType.NodeJoin3) {

                    NodeJoinMessage nodeJoinMessage = (NodeJoinMessage) message.getContent();
                    NodeManager.updateNode(message.getSourceNodeIdentifier(), message.getSourceIpAddress(),
                            nodeJoinMessage.getPort(), nodeJoinMessage.isFullNode());
                    response = new Message(MessageType.NodeJoinAcknowledgement4, null);

                } else if (messageType == MessageType.Transaction5) {

                    response = new Message(MessageType.TransactionResponse6,
                            new TransactionResponse((Transaction) message.getContent()));

                } else if (messageType == MessageType.PreviousHashRequest7) {

                    response = new Message(MessageType.PreviousHashResponse8, new PreviousHashResponse());

                } else if (messageType == MessageType.TransactionPoolRequest13) {

                    response = new Message(MessageType.TransactionPoolResponse14,
                            new TransactionPoolResponse(TransactionPool.allTransactions()));

                } else if (messageType == MessageType.HighestBlockFrozenRequest15) {

                    response = new Message(MessageType.HighestBlockFrozenResponse16,
                            new HighestBlockFrozenResponse(BlockManager.highestBlockFrozen()));

                } else if (messageType == MessageType.Ping200) {

                    response = new Message(MessageType.PingResponse201, new PingResponse());

                } else if (messageType == MessageType.GenesisBlock500) {

                    BlockMessageObject genesisBlock = (BlockMessageObject) message.getContent();
                    response = new Message(MessageType.GenesisBlockAcknowledgement501,
                            new GenesisBlockAcknowledgement(genesisBlock.getBlock()));
                }
            }
        } catch (Exception ignored) { }

        if (response == null) {
            response = new Message(MessageType.Error65534, null);
        }

        System.out.println("response message is " + response);

        return response;
    }

}