package pro.bigbro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.bigbro.models.City;
import pro.bigbro.repositories.CityRepository;
import pro.bigbro.services.counting.SeleniumService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class Main {
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private SeleniumService seleniumService;

    public void runReportMaker() {
        System.out.println("Main started");

        int returnCode;

        // downloading datas
        LocalDateTime stageStart = LocalDateTime.now();

        try {
            List<City> cityList = (List<City>) cityRepository.findAll();
            returnCode = seleniumService.uploadFiles(cityList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LocalDateTime stageFinish = LocalDateTime.now();
        reportDurationTime("Uploading", stageStart, stageFinish);

        System.exit(0);
    }

    private void reportDurationTime(String stage, LocalDateTime start, LocalDateTime finish) {
        System.out.println("Stage: " + stage);
        System.out.println("Starting time: " + start);
        System.out.println("Finish time: " + finish);
        long millis = Duration.between(start, finish).toMillis();
        System.out.printf("Total time estimated: %02d:%02d:%02d \n",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        System.out.println("----------------------------------------------------------------------------");
    }
}
