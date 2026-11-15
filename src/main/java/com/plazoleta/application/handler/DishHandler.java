package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.port.input.CreateDishPort;
import com.plazoleta.application.port.input.ListRestaurantDishesPort;
import com.plazoleta.application.port.input.SetDishAvailabilityPort;
import com.plazoleta.application.port.input.UpdateDishPort;

/** Delega en los casos de uso de platos. Se registra como @Bean en BeanConfiguration (sin Spring aquí). */
public class DishHandler implements IDishHandler {
    private final CreateDishPort createDishUseCase;
    private final UpdateDishPort updateDishUseCase;
    private final SetDishAvailabilityPort setDishAvailabilityUseCase;
    private final ListRestaurantDishesPort listRestaurantDishesUseCase;

    public DishHandler(CreateDishPort createDishUseCase, UpdateDishPort updateDishUseCase,
                       SetDishAvailabilityPort setDishAvailabilityUseCase,
                       ListRestaurantDishesPort listRestaurantDishesUseCase) {
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
