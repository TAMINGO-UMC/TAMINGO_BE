package app.tamingo.common.time;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class VirtualTimeService {

    private static final double DEFAULT_SCALE = 1.0;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 1000.0;

    private final Clock clock = Clock.systemDefaultZone();
    private final ZoneId zoneId = ZoneId.systemDefault();

    private volatile double scale = DEFAULT_SCALE;
    private volatile Instant anchorReal = clock.instant();
    private volatile Instant anchorVirtual = anchorReal;
    private volatile long shiftSeconds = 0L;

    public LocalDateTime now() {
        Instant realNow = clock.instant();
        return LocalDateTime.ofInstant(resolveVirtualInstant(realNow), zoneId);
    }

    public LocalDate today() {
        return now().toLocalDate();
    }

    public synchronized TimeState updateScale(double newScale) {
        validateScale(newScale);
        Instant realNow = clock.instant();
        Instant virtualNow = resolveVirtualInstant(realNow);
        this.anchorReal = realNow;
        this.anchorVirtual = virtualNow.minusSeconds(this.shiftSeconds);
        this.scale = newScale;
        return toState(realNow, virtualNow);
    }

    public synchronized TimeState reset() {
        this.shiftSeconds = 0L;
        return updateScale(DEFAULT_SCALE);
    }

    public synchronized TimeState shiftMinutes(long minutes) {
        if (minutes == 0) {
            return currentState();
        }

        long deltaSeconds;
        try {
            deltaSeconds = Math.multiplyExact(minutes, 60L);
            this.shiftSeconds = Math.addExact(this.shiftSeconds, deltaSeconds);
        } catch (ArithmeticException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "minutes is too large");
        }

        Instant realNow = clock.instant();
        Instant virtualNow = resolveVirtualInstant(realNow);
        return toState(realNow, virtualNow);
    }

    public synchronized TimeState resetShift() {
        this.shiftSeconds = 0L;
        Instant realNow = clock.instant();
        Instant virtualNow = resolveVirtualInstant(realNow);
        return toState(realNow, virtualNow);
    }

    public TimeState currentState() {
        Instant realNow = clock.instant();
        Instant virtualNow = resolveVirtualInstant(realNow);
        return toState(realNow, virtualNow);
    }

    private Instant resolveVirtualInstant(Instant realNow) {
        Instant localAnchorReal = this.anchorReal;
        Instant localAnchorVirtual = this.anchorVirtual;
        double localScale = this.scale;

        long elapsedNanos = Duration.between(localAnchorReal, realNow).toNanos();
        long scaledNanos = Math.round(elapsedNanos * localScale);
        return localAnchorVirtual.plusNanos(scaledNanos).plusSeconds(this.shiftSeconds);
    }

    private TimeState toState(Instant realNow, Instant virtualNow) {
        return new TimeState(
                this.scale,
                this.shiftSeconds / 60,
                LocalDateTime.ofInstant(realNow, zoneId),
                LocalDateTime.ofInstant(virtualNow, zoneId)
        );
    }

    private void validateScale(double newScale) {
        if (Double.isNaN(newScale) || Double.isInfinite(newScale)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 숫자입니다");
        }
        if (newScale < MIN_SCALE || newScale > MAX_SCALE) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "0.1에서 1000사이여야 합니다");
        }
    }

    public record TimeState(
            double scale,
            long shiftMinutes,
            LocalDateTime realNow,
            LocalDateTime virtualNow
    ) {
        public boolean accelerated() {
            return scale != DEFAULT_SCALE;
        }
    }
}
