package com.plazoleta.infrastructure.output.persistence.adapter;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.OrderNotFoundException;
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
        if (order.getId() == null) {
            return orderJpaRepository.save(OrderEntity.fromDomain(order)).toDomain();
        }
        // Pedido que ya existe: se actualiza su fila (estado y empleado) sin volver a crear sus platos.
        OrderEntity entity = orderJpaRepository.findById(order.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order was not found"));
        entity.applyChanges(order);
        return orderJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id).map(OrderEntity::toDomain);
    }

    @Override
    public boolean existsByClientIdAndStatusIn(Long clientId, Set<OrderStatus> statuses) {
        return orderJpaRepository.existsByClientIdAndStatusIn(clientId, statuses);
    }

    @Override
    public PageResult<Order> findByRestaurantAndStatus(Long restaurantId, OrderStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").and(Sort.by("id")));
        var result = orderJpaRepository.findByRestaurant_IdAndStatus(restaurantId, status, pageable);
        return new PageResult<>(result.getContent().stream().map(OrderEntity::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }
}
