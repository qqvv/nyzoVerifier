package co.nyzo.verifier.messages;

import co.nyzo.verifier.FieldByteSize;
import co.nyzo.verifier.MessageObject;
import co.nyzo.verifier.NewVerifierVoteManager;

import java.nio.ByteBuffer;
import java.util.*;

public class NewVerifierVotemapResponse implements MessageObject {

    private Map<ByteBuffer, ByteBuffer> voteMap = new HashMap<>();

    public NewVerifierVotemapResponse(Map<ByteBuffer, ByteBuffer> voteMap) {
        this.voteMap = voteMap;
    }

    public Map<ByteBuffer, ByteBuffer> getVoteMap() {
        return voteMap;
    }

    @Override
    public int getByteSize() {

        return 4 + voteMap.size() * (FieldByteSize.identifier + FieldByteSize.identifier);
    }

    @Override
    public byte[] getBytes() {

        byte[] result = new byte[getByteSize()];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putInt(voteMap.size());
        for (ByteBuffer votingVerifier : voteMap.keySet()) {
            buffer.put(votingVerifier.array());
            buffer.put(voteMap.get(votingVerifier).array());
        }

        return result;
    }

    public static NewVerifierVotemapResponse fromByteBuffer(ByteBuffer buffer) {

        NewVerifierVotemapResponse result = null;

        try {
            Map<ByteBuffer, ByteBuffer> voteMap = new HashMap<>();

            int numberOfVotes = buffer.getInt();
            for (int i = 0; i < numberOfVotes; i++) {
              byte[] votingVerifier = new byte[FieldByteSize.identifier];
              buffer.get(votingVerifier);
              byte[] vote = new byte[FieldByteSize.identifier];
              buffer.get(vote);
              voteMap.put(ByteBuffer.wrap(votingVerifier), ByteBuffer.wrap(vote));
            }

            result = new NewVerifierVotemapResponse(voteMap);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[NewVerifierVotemapResponse(" + voteMap.size() + ")]");
        return result.toString();
    }
}
