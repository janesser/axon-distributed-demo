package org.axonframework.queryhandling.jpa.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.queryhandling.SubscriptionQueryUpdateMessage;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;

import javax.persistence.*;
import java.time.Instant;

/**
 * @see org.axonframework.eventhandling.AbstractEventEntry
 */
@Entity
@Data
@NoArgsConstructor
public class QueryUpdateEntity {

    @Id
    @GeneratedValue
    Long updateId;

    @ManyToOne
    SubscriptionEntity subscription;

    @Lob
    @Column(length = 16 * 1024)
    byte[] updatePayload;

    String updatePayloadType;

    String updatePayloadRevision;

    Instant creationTime = Instant.now();

    public QueryUpdateEntity(SubscriptionEntity subscription, SubscriptionQueryUpdateMessage<?> updateMessage, Serializer serializer) {
        this.subscription = subscription;

        SerializedObject<byte[]> serializePayload = updateMessage.serializePayload(serializer, byte[].class);
        this.updatePayload = serializePayload.getData();
        this.updatePayloadType = serializePayload.getType().getName();
        this.updatePayloadRevision = serializePayload.getType().getRevision();
    }

    public <U> U getPayload(Serializer serializer) {
        SerializedObject<byte[]> sso = new SimpleSerializedObject<>(
                updatePayload,
                byte[].class,
                updatePayloadType,
                updatePayloadRevision
        );
        return serializer.deserialize(sso);
    }
}
