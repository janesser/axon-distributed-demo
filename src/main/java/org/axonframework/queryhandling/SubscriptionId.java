package org.axonframework.queryhandling;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.queryhandling.jpa.model.SubscriptionEntity;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;
import org.springframework.util.DigestUtils;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SubscriptionId implements Serializable {

    public static <Q> SerializedObject<byte[]> serialize(Q query, Serializer serializer) {
        return serializer.serialize(query, byte[].class);
    }

    public static String hash(byte[] payload) {
        return DigestUtils.md5DigestAsHex(payload);
    }

    static SubscriptionId from(SubscriptionEntity entity) {
        return new SubscriptionId(
                entity.getNodeId(),
                entity.getQueryPayloadHash()
        );
    }

    public static SubscriptionId from(String nodeId, SubscriptionQueryMessage<?, ?, ?> message, Serializer serializer) {
        SerializedObject<byte[]> serialized = serialize(message.getPayload(), serializer);

        return new SubscriptionId(
                nodeId,
                serialized.getData()
        );
    }

    String nodeId;

    String queryPayloadHash;

    public SubscriptionId(String nodeId, Object query, Serializer serializer) {
        this(nodeId, serialize(query, serializer).getData());
    }

    private SubscriptionId(String nodeId, byte[] payload) {
        this.nodeId = nodeId;

        this.queryPayloadHash = hash(payload);
    }

    private SubscriptionId(String nodeId, String payloadHash) {
        this.nodeId = nodeId;

        this.queryPayloadHash = payloadHash;
    }
}
