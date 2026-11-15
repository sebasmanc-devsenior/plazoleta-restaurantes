package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;

public interface ListRestaurantsPort {
    PageResult<RestaurantListItem> execute(int page, int size);
}
