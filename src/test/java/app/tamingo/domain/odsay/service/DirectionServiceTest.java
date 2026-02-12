package app.tamingo.domain.odsay.service;

import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.odsay.client.OdsayTransitClient;
import app.tamingo.domain.odsay.dto.OdsayTransitResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DirectionServiceTest {

    @Test
    void calculateRoute_usesGeoEstimateWhenWithinShortEta() {
        OdsayTransitClient odsayTransitClient = mock(OdsayTransitClient.class);
        GeoService geoService = mock(GeoService.class);
        DirectionService directionService = new DirectionService(odsayTransitClient, geoService);

        when(geoService.distanceKm(1.0, 2.0, 3.0, 4.0)).thenReturn(0.2);
        when(geoService.estimateShortDistanceMinutes(0.2)).thenReturn(3);

        DirectionResult result = directionService.calculateRoute(1.0, 2.0, 3.0, 4.0);

        assertNotNull(result);
        assertEquals(3, result.getTotalMinutes());
        verifyNoInteractions(odsayTransitClient);
    }

    @Test
    void calculateRoute_callsOdsayWhenShortEtaExceedsThreshold() {
        OdsayTransitClient odsayTransitClient = mock(OdsayTransitClient.class);
        GeoService geoService = mock(GeoService.class);
        DirectionService directionService = new DirectionService(odsayTransitClient, geoService);

        when(geoService.distanceKm(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(2.0);
        when(geoService.estimateShortDistanceMinutes(2.0)).thenReturn(11);
        DirectionResult expected = new DirectionResult(42, List.of());
        when(odsayTransitClient.route(1.0, 2.0, 3.0, 4.0)).thenReturn(expected);

        DirectionResult result = directionService.calculateRoute(1.0, 2.0, 3.0, 4.0);

        assertEquals(expected, result);
        verify(odsayTransitClient).route(1.0, 2.0, 3.0, 4.0);
    }

    @Test
    void calculateRouteDetail_usesGeoEstimateWhenWithinShortEta() {
        OdsayTransitClient odsayTransitClient = mock(OdsayTransitClient.class);
        GeoService geoService = mock(GeoService.class);
        DirectionService directionService = new DirectionService(odsayTransitClient, geoService);

        when(geoService.distanceKm(1.0, 2.0, 3.0, 4.0)).thenReturn(0.2);
        when(geoService.estimateShortDistanceMinutes(0.2)).thenReturn(3);

        OdsayTransitResponse response = directionService.calculateRouteDetail(1.0, 2.0, 3.0, 4.0);

        assertNotNull(response);
        assertNotNull(response.metaData());
        assertNotNull(response.metaData().plan());
        assertEquals(1, response.metaData().plan().itineraries().size());
        OdsayTransitResponse.Itinerary itinerary = response.metaData().plan().itineraries().get(0);
        assertEquals(180, itinerary.totalTime());
        assertEquals(200, itinerary.totalDistance());
        verifyNoInteractions(odsayTransitClient);
    }

    @Test
    void calculateRouteDetail_callsOdsayWhenShortEtaExceedsThreshold() {
        OdsayTransitClient odsayTransitClient = mock(OdsayTransitClient.class);
        GeoService geoService = mock(GeoService.class);
        DirectionService directionService = new DirectionService(odsayTransitClient, geoService);

        when(geoService.distanceKm(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(2.0);
        when(geoService.estimateShortDistanceMinutes(2.0)).thenReturn(11);
        OdsayTransitResponse expected = new OdsayTransitResponse(
                new OdsayTransitResponse.MetaData(
                        new OdsayTransitResponse.Plan(List.of(
                                new OdsayTransitResponse.Itinerary(600, 600, 1000, 1000, List.of())
                        ))
                ),
                null
        );
        when(odsayTransitClient.routeResponse(1.0, 2.0, 3.0, 4.0)).thenReturn(expected);

        OdsayTransitResponse result = directionService.calculateRouteDetail(1.0, 2.0, 3.0, 4.0);

        assertEquals(expected, result);
        verify(odsayTransitClient).routeResponse(1.0, 2.0, 3.0, 4.0);
    }
}
