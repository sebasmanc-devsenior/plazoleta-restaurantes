package com.plazoleta.infrastructure.output.persistence.adapter;

import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.ActiveOrderExistsException;
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
        if (order.getId() != null) {
            throw new IllegalStateException("save is only for new orders; use updateIfStatus to change an order");
        }
        try {
            return orderJpaRepository.save(OrderEntity.fromDomain(order)).toDomain();
        } catch (DataIntegrityViolationException ex) {
            // Dos pedidos simultáneos del mismo cliente: la columna única active_client_id deja pasar solo uno.
            String cause = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
            if (cause.contains(OrderEntity.ACTIVE_CLIENT_CONSTRAINT)) {
                throw new ActiveOrderExistsException(
                        "You already have an order in progress (pending, in preparation or ready)");
            }
            throw ex;
        }
    }

    @Override
    public boolean updateIfStatus(Order order, OrderStatus expectedStatus) {
        int updatedRows = orderJpaRepository.updateStatusIfCurrent(order.getId(), expectedStatus, order.getStatus(),
                order.getEmployeeId(), order.getSecurityPin(), OrderEntity.activeClientIdFor(order));
        return updatedRows == 1;
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
