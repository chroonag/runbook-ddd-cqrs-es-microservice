package io.ordermanagement.domain.model;

import io.ordermanagement.application.OpenTab;
import io.ordermanagement.application.PlaceOrder;

import java.util.List;
import java.util.stream.Collectors;

class Tab implements Aggregate {

    private DomainEventPublisher domainEventPublisher;
    private boolean open = false;

    Tab(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    void handle(OpenTab c) {
        TabOpened tabOpened = new TabOpened(
                c.getId(),
                c.getTableNumber(),
                c.getWaiter());

        domainEventPublisher.publish(tabOpened);
        apply(tabOpened);
    }

    void handle(PlaceOrder c) {
        if (!open) throw new TabNotOpen();

        List<OrderItem> drinks = c.getItems().stream()
                .filter(OrderItem::isDrink)
                .collect(Collectors.toList());

        if (!drinks.isEmpty()) {
            DrinksOrdered drinksOrdered = new DrinksOrdered(
                    c.getId(),
                    drinks);
            domainEventPublisher.publish(drinksOrdered);
        }

        List<OrderItem> food = c.getItems().stream()
                .filter(item -> !item.isDrink())
                .collect(Collectors.toList());
        if (!food.isEmpty()) {
            FoodOrdered foodOrdered = new FoodOrdered(
                    c.getId(),
                    food);
            domainEventPublisher.publish(foodOrdered);
        }
    }

    void apply(TabOpened e) {
        this.open = true;
    }
}
