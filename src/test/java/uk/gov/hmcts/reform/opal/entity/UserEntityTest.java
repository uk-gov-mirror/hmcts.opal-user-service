package uk.gov.hmcts.reform.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @ParameterizedTest(name = "{0}")
    @MethodSource("statusScenarios")
    void getStatusFromTime_returnsExpectedStatus(String scenario,
                                                 UserEntity user,
                                                 UserEntity.Status expectedStatus) {
        assertEquals(expectedStatus, user.getStatusFromTime(NOW));
    }

    private static Stream<Arguments> statusScenarios() {
        return Stream.of(
            arguments(
                "pending when activation date is missing",
                UserEntity.builder().build(),
                UserEntity.Status.PENDING
            ),
            arguments(
                "pending when activation date is in the future",
                UserEntity.builder()
                    .activationDate(NOW.plusDays(1))
                    .build(),
                UserEntity.Status.PENDING
            ),
            arguments(
                "active when activation date is in the past and no other flags",
                UserEntity.builder()
                    .activationDate(NOW.minusDays(1))
                    .build(),
                UserEntity.Status.ACTIVE
            ),
            arguments(
                "suspended when within suspension window",
                UserEntity.builder()
                    .activationDate(NOW.minusDays(1))
                    .suspensionStartDate(NOW.minusHours(1))
                    .suspensionEndDate(NOW.plusHours(1))
                    .build(),
                UserEntity.Status.SUSPENDED
            ),
            arguments(
                "deactivated when deactivation date is reached",
                UserEntity.builder()
                    .activationDate(NOW.minusDays(1))
                    .deactivationDate(NOW)
                    .build(),
                UserEntity.Status.DEACTIVATED
            ),
            arguments(
                "deactivated takes precedence over suspended and pending",
                UserEntity.builder()
                    .activationDate(NOW.plusDays(1))
                    .suspensionStartDate(NOW.minusHours(1))
                    .suspensionEndDate(NOW.plusHours(1))
                    .deactivationDate(NOW.minusMinutes(1))
                    .build(),
                UserEntity.Status.DEACTIVATED
            ),
            arguments(
                "not suspended when suspension end is exactly now",
                UserEntity.builder()
                    .activationDate(NOW.minusDays(1))
                    .suspensionStartDate(NOW.minusHours(1))
                    .suspensionEndDate(NOW)
                    .build(),
                UserEntity.Status.ACTIVE
            )
        );
    }
}
