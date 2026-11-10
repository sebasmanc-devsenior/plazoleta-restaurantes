package com.plazoleta.infrastructure.output.persistence.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.infrastructure.output.persistence.entity.OrderEntity;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    boolean existsByClientIdAndStatusIn(Long clientId, Collection<OrderStatus> statuses);
}
