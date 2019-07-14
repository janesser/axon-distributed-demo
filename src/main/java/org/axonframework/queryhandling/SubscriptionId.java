package org.axonframework.queryhandling;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;
import org.springframework.util.DigestUtils;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Embeddable
public class SubscriptionId implements Serializable {

    public static <Q> SerializedObject<byte[]> serialize(Q query, Serializer serializer) {
        return serializer.serialize(query, byte[].class);
    }

    private static String hash(byte[] payload) {
        return DigestUtils.md5DigestAsHex(payload);
    }

    public static SubscriptionId from(String nodeId, SubscriptionQueryMessage<?, ?, ?> message, Serializer serializer) {
        SerializedObject<byte[]> serialized = serialize(message.getPayload(), serializer);

        return new SubscriptionId(
                nodeId,
                serialized.getData()
        );
    }

    private String nodeId;

    private String queryPayloadHash;

    public SubscriptionId(String nodeId, Object query, Serializer serializer) {
        this(nodeId, serialize(query, serializer).getData());
    }

    private SubscriptionId(String nodeId, byte[] payload) {
        this.nodeId = nodeId;

        this.queryPayloadHash = hash(payload);
    }

    public SubscriptionId(String nodeId, String payloadHash) {
        this.nodeId = nodeId;

        this.queryPayloadHash = payloadHash;
    }
}
