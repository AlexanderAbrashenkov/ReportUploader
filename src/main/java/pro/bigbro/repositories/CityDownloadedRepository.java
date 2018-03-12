package pro.bigbro.repositories;

import org.springframework.data.repository.CrudRepository;
import pro.bigbro.models.CityDownloaded;

import java.util.List;

public interface CityDownloadedRepository extends CrudRepository<CityDownloaded, Integer> {
    List<CityDownloaded> findAllByMonthAndYear(int month, int year);
}
