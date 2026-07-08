package lk.ac.ruhuna.dcs.cvmanagement.support;

import java.util.UUID;

/**
 * Factory for creating test data objects used across Sprint 1 tests.
 * <p>Expanded in future sprints as domain entities are implemented.
 */
public final class TestDataFactory {

    public static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String ADMIN_EMAIL = "admin@dcs.ruh.ac.lk";
    public static final String STUDENT_INDEX = "SC-2020-001";
    public static final String STUDENT_EMAIL = "sc2020001@dcs.ruh.ac.lk";

    private TestDataFactory() {
    }
}
