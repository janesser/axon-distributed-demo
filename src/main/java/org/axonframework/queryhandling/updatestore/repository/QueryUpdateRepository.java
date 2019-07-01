package org.axonframework.queryhandling.updatestore.repository;

import org.axonframework.queryhandling.SubscriptionId;
import org.axonframework.queryhandling.updatestore.model.QueryUpdateEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface QueryUpdateRepository extends CrudRepository<QueryUpdateEntity, Long> {

    List<QueryUpdateEntity> findBySubscriptionId(SubscriptionId subscriptionId);

    // TODO untested with Redis
    List<QueryUpdateEntity> findByCreationTimeLessThan(Instant minAge);
}
