package pro.bigbro.repositories;

import org.springframework.data.repository.CrudRepository;
import pro.bigbro.models.City;

public interface CityRepository extends CrudRepository<City, Integer> {
}
