package lk.ac.ruhuna.dcs.cvmanagement.shared.security;

public enum RoleName {
    STUDENT("ROLE_STUDENT"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    RoleName(String authority) {
        this.authority = authority;
    }

    public String authority() {
        return authority;
    }

    public static RoleName fromAuthority(String authority) {
        for (RoleName role : values()) {
            if (role.authority.equals(authority)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unsupported role authority.");
    }
}
