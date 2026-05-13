package ua.edu.duikt.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;
import ua.edu.duikt.restaurant.repository.RestaurantTableRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final RestaurantTableRepository tableRepository;

    @Transactional(readOnly = true)
    public List<RestaurantTable> listAllTables() {
        return tableRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RestaurantTable> getFreeTables() {
        return tableRepository.findByStatus(TableStatus.FREE);
    }
}
