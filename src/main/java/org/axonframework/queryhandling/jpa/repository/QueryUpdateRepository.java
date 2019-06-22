package org.axonframework.queryhandling.jpa.repository;

import org.axonframework.queryhandling.jpa.model.QueryUpdateEntity;
import org.axonframework.queryhandling.jpa.model.SubscriptionEntity;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.NamedQuery;
import java.util.List;

@NamedQuery(name = "QueryUpdateEntity.findBySubscription",
        query = "SELECT u " +
                "FROM QueryUpdateEntity u " +
                "WHERE u.subscription = :subscription " +
                "ORDER BY u.creationTime ASC")
public interface QueryUpdateRepository extends CrudRepository<QueryUpdateEntity, Long> {

    List<QueryUpdateEntity> findBySubscription(SubscriptionEntity subscription);
}
