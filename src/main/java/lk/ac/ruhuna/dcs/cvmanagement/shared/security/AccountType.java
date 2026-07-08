package lk.ac.ruhuna.dcs.cvmanagement.shared.security;

public enum AccountType {
    STUDENT(RoleName.STUDENT),
    ADMIN(RoleName.ADMIN);

    private final RoleName role;

    AccountType(RoleName role) {
        this.role = role;
    }

    public RoleName role() {
        return role;
    }
}
