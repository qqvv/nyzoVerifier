package co.nyzo.verifier.messages;

import co.nyzo.verifier.MessageObject;
import co.nyzo.verifier.NewVerifierVoteManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NewVerifierVotemapResponse implements MessageObject {

    private Map<ByteBuffer, ByteBuffer> voteMap;

    public NewVerifierVotemapResponse(List<Node> voteMap) {
        this.voteMap = voteMap;
    }

    public List<Node> getVoteMap() {
        return voteMap;
    }

    @Override
    public int getByteSize() {

        return 4 + voteMap.size() * (voteMap.getByteSizeStatic());
    }

    @Override
    public byte[] getBytes() {

        byte[] result = new byte[getByteSize()];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putInt(voteMap.size());
        for (Node votingVerifier: voteMap) {
            buffer.put(votingVerifier.array());
            buffer.put(voteMap.get(votingVerifier).array());
        }

        return result;
    }

    public static MeshResponse fromByteBuffer(ByteBuffer buffer) {

        MeshResponse result = null;

        try {
            Map<ByteBuffer, ByteBuffer> voteMap = new HashMap<>();

            int numberOfVotes = buffer.getInt();
            for (int i = 0; i < numberOfVotes; i++) {
                voteMap.put(votingIdentifierBuffer, ByteBuffer.wrap(vote.getIdentifier()));
            }

            result = new NewVerifierVotemapResponse(voteMap);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[NewVerifierVotemapResponse(" + voteMap.size() + "): {");
        String separator = "";
        for (int i = 0; i < mesh.size() && i < 5; i++) {
            result.append(separator).append(mesh.get(i).toString());
            separator = ",";
        }
        if (mesh.size() > 5) {
            result.append("...");
        }
        result.append("}]");

        return result.toString();
    }
}
