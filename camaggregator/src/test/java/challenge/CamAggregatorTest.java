package challenge;

import challenge.dto.inner.AggregatedAvailableCam;
import challenge.dto.outer.AvailableCam;
import challenge.dto.outer.SourceData;
import challenge.dto.outer.TokenData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CamAggregatorTest {

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void initRestTemplate() {
        String sourceDataUrl1 = "sourceDataUrl1";
        String sourceDataUrl2 = "sourceDataUrl2";
        String tokenDataUrl1= "tokenDataUrl1";
        String tokenDataUrl2 = "tokenDataUrl2";
        AvailableCam[] cams = new AvailableCam[2];
        AvailableCam cam1 = new AvailableCam();
        cam1.setSourceDataUrl(sourceDataUrl1);
        cam1.setTokenDataUrl(tokenDataUrl1);
        AvailableCam cam2 = new AvailableCam();
        cam2.setSourceDataUrl(sourceDataUrl2);
        cam2.setTokenDataUrl(tokenDataUrl2);
        cams[0] = cam1;
        cams[1] = cam2;
        SourceData sourceData1 = new SourceData();
        sourceData1.setUrlType("urlType1");
        sourceData1.setVideoUrl("videoUrl1");
        SourceData sourceData2 = new SourceData();
        sourceData2.setUrlType("urlType2");
        sourceData2.setVideoUrl("videoUrl2");
        TokenData tokenData1 = new TokenData();
        tokenData1.setTtl(1);
        tokenData1.setValue("value1");
        TokenData tokenData2 = new TokenData();
        tokenData2.setTtl(2);
        tokenData2.setValue("value2");
        Mockito.when(restTemplate.getForObject(CamAggregator.BASE_URL, AvailableCam[].class))
                .thenReturn(cams);
        Mockito.when(restTemplate.getForObject(sourceDataUrl1, SourceData.class))
                .thenReturn(sourceData1);
        Mockito.when(restTemplate.getForObject(tokenDataUrl1, TokenData.class))
                .thenReturn(tokenData1);
        Mockito.when(restTemplate.getForObject(sourceDataUrl2, SourceData.class))
                .thenReturn(sourceData2);
        Mockito.when(restTemplate.getForObject(tokenDataUrl2, TokenData.class))
                .thenReturn(tokenData2);
    }

    @Test
    public void fetchAggregatedCamDataSuccess() throws ExecutionException {
        List<AggregatedAvailableCam> cams = CamAggregator.fetchAggregatedCamData(CamAggregator.BASE_URL, restTemplate);
        assertEquals(2, cams.size());
        assertEquals("value1", cams.get(0).getValue());
        assertEquals("value2", cams.get(1).getValue());
    }

}
