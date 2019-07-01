package org.axonframework.queryhandling.updatestore.repository;

import org.axonframework.queryhandling.updatestore.model.QueryUpdateEntity;
import org.axonframework.queryhandling.updatestore.model.SubscriptionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface QueryUpdateRepository extends CrudRepository<QueryUpdateEntity, Long> {

    List<QueryUpdateEntity> findBySubscription(SubscriptionEntity subscription);

    List<QueryUpdateEntity> findByCreationTimeLessThan(Instant minAge);
}
