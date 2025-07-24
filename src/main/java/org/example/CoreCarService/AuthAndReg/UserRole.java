package org.example.CoreCarService.AuthAndReg;

public enum UserRole {
    CLIENT ("Клиент", new String[]{"creating_application", "reading_your_orders"}),
    DRIVER_ORGANIZATION ("Водитель организации", new String[]{"creating_application", "reading_your_orders"}),
    MASTER ("Мастер", new String[]{"creating_application", "read_orders", "write_in_order", "creating_new_order"}),
    MODERATOR ("Модератор", new String[]{"read_order", "write_in_orders"}),
    ADMIN("Администратор", new String[]{"creating_application", "read_orders", "write_in_order", "delete_order", "creating_new_order"});

    private final String label; //роли
    private final String[] permissions; // разрешения

    UserRole(String label, String[] permissions) {
        this.label = label;
        this.permissions = permissions;
    }
    public String getLabel() {
        return label;
    }
    public String[] getPermissions() {
        return permissions;
    }
}
