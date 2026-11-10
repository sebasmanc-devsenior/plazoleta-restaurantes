package com.plazoleta.infrastructure.output.persistence.adapter;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.infrastructure.output.persistence.entity.OrderEntity;
import com.plazoleta.infrastructure.output.persistence.repository.OrderJpaRepository;

@Repository
public class OrderPersistenceAdapter implements OrderRepositoryPort {
    private final OrderJpaRepository orderJpaRepository;

    public OrderPersistenceAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(OrderEntity.fromDomain(order)).toDomain();
    }

    @Override
    public boolean existsByClientIdAndStatusIn(Long clientId, Set<OrderStatus> statuses) {
        return orderJpaRepository.existsByClientIdAndStatusIn(clientId, statuses);
    }
}
