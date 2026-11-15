package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.port.input.CreateRestaurantUseCase;
import com.plazoleta.application.port.input.GetRestaurantUseCase;
import com.plazoleta.application.port.input.ListRestaurantsUseCase;

/** Delega en los casos de uso de restaurantes. Se registra como @Bean en BeanConfiguration (sin Spring aquí). */
public class RestaurantHandler implements IRestaurantHandler {
    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final ListRestaurantsUseCase listRestaurantsUseCase;
    private final GetRestaurantUseCase getRestaurantUseCase;

    public RestaurantHandler(CreateRestaurantUseCase createRestaurantUseCase,
                             ListRestaurantsUseCase listRestaurantsUseCase,
                             GetRestaurantUseCase getRestaurantUseCase) {
        this.createRestaurantUseCase = createRestaurantUseCase;
        this.listRestaurantsUseCase = listRestaurantsUseCase;
        this.getRestaurantUseCase = getRestaurantUseCase;
    }

    @Override
    public RestaurantResponse createRestaurant(CreateRestaurantCommand command) {
        return createRestaurantUseCase.execute(command);
    }

    @Override
    public PageResult<RestaurantListItem> listRestaurants(int page, int size) {
        return listRestaurantsUseCase.execute(page, size);
    }

    @Override
    public RestaurantResponse getRestaurant(Long restaurantId) {
        return getRestaurantUseCase.execute(restaurantId);
    }
}
