package ua.edu.duikt.restaurant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.edu.duikt.restaurant.entity.RestaurantTable;
import ua.edu.duikt.restaurant.entity.enums.TableStatus;
import ua.edu.duikt.restaurant.repository.RestaurantTableRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private RestaurantTableRepository tableRepository;

    @InjectMocks
    private TableService tableService;

    @Test
    void listAllTables_shouldReturnAllTables() {
        List<RestaurantTable> tables = List.of(
                buildTable(1L, TableStatus.FREE),
                buildTable(2L, TableStatus.OCCUPIED),
                buildTable(3L, TableStatus.RESERVED)
        );

        when(tableRepository.findAll()).thenReturn(tables);

        List<RestaurantTable> result = tableService.listAllTables();

        assertThat(result).hasSize(3);
        verify(tableRepository).findAll();
    }

    @Test
    void getFreeTables_shouldReturnOnlyFreeTables() {
        List<RestaurantTable> freeTables = List.of(
                buildTable(1L, TableStatus.FREE),
                buildTable(2L, TableStatus.FREE)
        );

        when(tableRepository.findByStatus(TableStatus.FREE)).thenReturn(freeTables);

        List<RestaurantTable> result = tableService.getFreeTables();

        assertThat(result).hasSize(2);
        for (RestaurantTable t : result) {
            assertThat(t.getStatus()).isEqualTo(TableStatus.FREE);
        }
        verify(tableRepository).findByStatus(TableStatus.FREE);
    }

    private RestaurantTable buildTable(Long id, TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setNumber(id.intValue());
        table.setCapacity(4);
        table.setStatus(status);
        return table;
    }
}
