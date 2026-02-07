package app.tamingo.domain.home.converter;

import java.util.List;

final class RouteColorCatalog {

    private RouteColorCatalog() {}

    static final List<RouteColorResolver.SubwayRule> SUBWAY_RULES = List.of(
            // Numbered Seoul Metro lines
            new RouteColorResolver.SubwayRule("1호선", "#0D3692"),
            new RouteColorResolver.SubwayRule("2호선", "#33A23D"),
            new RouteColorResolver.SubwayRule("3호선", "#FE5B10"),
            new RouteColorResolver.SubwayRule("4호선", "#00A1DE"),
            new RouteColorResolver.SubwayRule("5호선", "#8B50A4"),
            new RouteColorResolver.SubwayRule("6호선", "#C55C1D"),
            new RouteColorResolver.SubwayRule("7호선", "#54640D"),
            new RouteColorResolver.SubwayRule("8호선", "#F14C82"),
            new RouteColorResolver.SubwayRule("9호선", "#AA9872"),

            // Metropolitan / Korail lines
            new RouteColorResolver.SubwayRule("공항철도", "#0090D2"),
            new RouteColorResolver.SubwayRule("AREX", "#0090D2"),
            new RouteColorResolver.SubwayRule("경의중앙", "#77C4A3"),
            new RouteColorResolver.SubwayRule("GYEONGUIJUNGANG", "#77C4A3"),
            new RouteColorResolver.SubwayRule("수인분당", "#F5A200"),
            new RouteColorResolver.SubwayRule("SUINBUNDANG", "#F5A200"),
            new RouteColorResolver.SubwayRule("신분당", "#D4003B"),
            new RouteColorResolver.SubwayRule("SHINBUNDANG", "#D4003B"),
            new RouteColorResolver.SubwayRule("경춘", "#0C8E72"),
            new RouteColorResolver.SubwayRule("GYEONGCHUN", "#0C8E72"),
            new RouteColorResolver.SubwayRule("경강", "#0054A6"),
            new RouteColorResolver.SubwayRule("GYEONGGANG", "#0054A6"),
            new RouteColorResolver.SubwayRule("서해", "#8FC31F"),
            new RouteColorResolver.SubwayRule("SEOHAE", "#8FC31F"),

            // Incheon Metro
            new RouteColorResolver.SubwayRule("인천1", "#7CA8D3"),
            new RouteColorResolver.SubwayRule("INCHEON1", "#7CA8D3"),
            new RouteColorResolver.SubwayRule("인천2", "#F4AB00"),
            new RouteColorResolver.SubwayRule("INCHEON2", "#F4AB00"),

            // Light rail / LRT
            new RouteColorResolver.SubwayRule("의정부", "#FF7F00"),
            new RouteColorResolver.SubwayRule("UIJEONGBU", "#FF7F00"),
            new RouteColorResolver.SubwayRule("우이신설", "#B7C452"),
            new RouteColorResolver.SubwayRule("UISINSEOL", "#B7C452"),
            new RouteColorResolver.SubwayRule("신림", "#6789CA"),
            new RouteColorResolver.SubwayRule("SILLIM", "#6789CA"),
            new RouteColorResolver.SubwayRule("김포골드", "#A17800"),
            new RouteColorResolver.SubwayRule("GIMPOGOLD", "#A17800"),
            new RouteColorResolver.SubwayRule("에버라인", "#6FB245"),
            new RouteColorResolver.SubwayRule("EVERLINE", "#6FB245"),
            new RouteColorResolver.SubwayRule("용인", "#6FB245")
    );
}
