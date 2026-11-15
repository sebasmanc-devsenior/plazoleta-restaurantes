package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.port.input.CreateDishUseCase;
import com.plazoleta.application.port.input.ListRestaurantDishesUseCase;
import com.plazoleta.application.port.input.SetDishAvailabilityUseCase;
import com.plazoleta.application.port.input.UpdateDishUseCase;

/** Delega en los casos de uso de platos. Se registra como @Bean en BeanConfiguration (sin Spring aquí). */
public class DishHandler implements IDishHandler {
    private final CreateDishUseCase createDishUseCase;
    private final UpdateDishUseCase updateDishUseCase;
    private final SetDishAvailabilityUseCase setDishAvailabilityUseCase;
    private final ListRestaurantDishesUseCase listRestaurantDishesUseCase;

    public DishHandler(CreateDishUseCase createDishUseCase, UpdateDishUseCase updateDishUseCase,
                       SetDishAvailabilityUseCase setDishAvailabilityUseCase,
                       ListRestaurantDishesUseCase listRestaurantDishesUseCase) {
        this.createDishUseCase = createDishUseCase;
        this.updateDishUseCase = updateDishUseCase;
        this.setDishAvailabilityUseCase = setDishAvailabilityUseCase;
        this.listRestaurantDishesUseCase = listRestaurantDishesUseCase;
    }

    @Override
    public DishResponse createDish(CreateDishCommand command, Long ownerId) {
        return createDishUseCase.execute(command, ownerId);
    }

    @Override
    public DishResponse updateDish(Long dishId, UpdateDishCommand command, Long ownerId) {
        return updateDishUseCase.execute(dishId, command, ownerId);
    }

    @Override
    public DishResponse setDishAvailability(Long dishId, boolean active, Long ownerId) {
        return setDishAvailabilityUseCase.execute(dishId, active, ownerId);
    }

    @Override
    public PageResult<DishResponse> listRestaurantDishes(Long restaurantId, String category, int page, int size) {
        return listRestaurantDishesUseCase.execute(restaurantId, category, page, size);
    }
}
