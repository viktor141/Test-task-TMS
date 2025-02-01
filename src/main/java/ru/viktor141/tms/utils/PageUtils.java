package ru.viktor141.tms.utils;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.Sort.Direction;


import java.util.ArrayList;
import java.util.List;

public class PageUtils {

    public static Pageable createPageable(int page, int size, String[] sort) {
        List<Order> orders = new ArrayList<>();

        if(sort[0].contains(",")){
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");

                orders.add(new Order(getDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Order(getDirection(sort[1]), sort[0]));
        }


        return PageRequest.of(page, size, Sort.by(orders));
    }

    private static Direction getDirection(String direction) {
        return Direction.valueOf(direction.toUpperCase());
    }
}
