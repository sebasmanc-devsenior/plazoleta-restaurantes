package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.port.input.ListRestaurantsPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;

public class ListRestaurantsUseCase implements ListRestaurantsPort {
    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public ListRestaurantsUseCase(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public PageResult<RestaurantListItem> execute(int page, int size) {
        validatePage(page, size);
        var result = restaurantRepositoryPort.findAll(page, size);
        return new PageResult<>(result.getContent().stream()
                .map(restaurant -> new RestaurantListItem(restaurant.getName(), restaurant.getLogoUrl()))
                .toList(), page, size, result.getTotalElements());
    }

    static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(MensajesError.PAGINA_DEBE_SER_0_O_MAYOR_Y);
        }
    }
}
