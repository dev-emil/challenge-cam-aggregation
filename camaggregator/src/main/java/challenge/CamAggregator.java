package challenge;

import challenge.dto.inner.AggregatedAvailableCam;
import challenge.dto.outer.AvailableCam;
import challenge.dto.outer.SourceData;
import challenge.dto.outer.TokenData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CamAggregator {
    public static String BASE_URL = "http://www.mocky.io/v2/5c51b9dd3400003252129fb5";

    public static void main(String[] args) throws ExecutionException, JsonProcessingException {
        List<AggregatedAvailableCam> cams = fetchAggregatedCamData(BASE_URL, new RestTemplate());
        System.out.println(new ObjectMapper().writeValueAsString(cams));
    }

    public static List<AggregatedAvailableCam> fetchAggregatedCamData(String baseUrl, RestTemplate injectedRestTemplate) throws ExecutionException {
        List<AggregatedAvailableCam> aggregatedAvailableCams;
        RestTemplate restTemplate = injectedRestTemplate;
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        List<AvailableCam> availableCams = Arrays.asList(restTemplate.getForObject(baseUrl, AvailableCam[].class));
        aggregatedAvailableCams = new ArrayList<>(availableCams.size());
        List<Future> futures = new ArrayList<>();
        for (AvailableCam availableCam : availableCams) {
            AggregatedAvailableCam aggregatedAvailableCam = new AggregatedAvailableCam();
            aggregatedAvailableCam.setId(availableCam.getId());
            futures.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    TokenData tokenData = restTemplate.getForObject(availableCam.getTokenDataUrl(),TokenData.class);
                    aggregatedAvailableCam.setValue(tokenData.getValue());
                    aggregatedAvailableCam.setTtl(tokenData.getTtl());
                }
            }));
            futures.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    SourceData tokenData = restTemplate.getForObject(availableCam.getSourceDataUrl(),SourceData.class);
                    aggregatedAvailableCam.setUrlType(tokenData.getUrlType());
                    aggregatedAvailableCam.setVideoUrl(tokenData.getVideoUrl());
                }
            }));
            aggregatedAvailableCams.add(aggregatedAvailableCam);
        }
        try {
            for (Future future : futures) {
                future.get();
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        }
        executorService.shutdown();
        return aggregatedAvailableCams;
    }
}
