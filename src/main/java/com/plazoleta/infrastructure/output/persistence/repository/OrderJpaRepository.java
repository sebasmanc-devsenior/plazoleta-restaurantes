package com.plazoleta.infrastructure.output.persistence.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.infrastructure.output.persistence.entity.OrderEntity;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    boolean existsByClientIdAndStatusIn(Long clientId, Collection<OrderStatus> statuses);
    Page<OrderEntity> findByRestaurant_IdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);

    /**
     * Cambio de estado condicional: MySQL solo actualiza la fila si el pedido sigue en expectedStatus.
     * Si dos peticiones compiten, solo una cambia la fila (devuelve 1) y la otra devuelve 0.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update OrderEntity o set o.status = :newStatus, o.employeeId = :employeeId, o.securityPin = :securityPin, "
            + "o.activeClientId = :activeClientId where o.id = :id and o.status = :expectedStatus")
    int updateStatusIfCurrent(@Param("id") Long id, @Param("expectedStatus") OrderStatus expectedStatus,
                              @Param("newStatus") OrderStatus newStatus, @Param("employeeId") Long employeeId,
                              @Param("securityPin") String securityPin, @Param("activeClientId") Long activeClientId);
}
