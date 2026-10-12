package com.plazoleta.application.ports.input;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;

public interface ListRestaurantsUseCase {
    PageResult<RestaurantListItem> execute(int page, int size);
}
