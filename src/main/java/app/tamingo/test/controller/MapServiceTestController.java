package app.tamingo.test.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.kakao.dto.KakaoAddressResponseDto;
import app.tamingo.domain.kakao.service.KakaoGeoService;
import app.tamingo.domain.odsay.service.DirectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/maps")
public class MapServiceTestController {

    private final DirectionService directionService;
    private final KakaoGeoService kakaoGeoService;

    @GetMapping("/odsay/route")
    public ApiResponse<DirectionResult> getRoute(
            @RequestParam("startLat") double startLat,
            @RequestParam("startLng") double startLng,
            @RequestParam("goalLat") double goalLat,
            @RequestParam("goalLng") double goalLng
    ) {
        DirectionResult result = directionService.calculateRoute(
                startLat,
                startLng,
                goalLat,
                goalLng
        );
        return ApiResponse.onSuccess(result, SuccessCode.OK);
    }

    @GetMapping("/kakao/geo")
    public ApiResponse<KakaoAddressResponseDto> getAddress(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng
    ) {
        KakaoAddressResponseDto response = kakaoGeoService.getAddress(lng, lat);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}
