package com.codedu.dtos.user;

public record LoginResult(boolean success, UserDTO user, String message) {

    public static LoginResult ok(UserDTO user) {
        return new LoginResult(true, user, null);
    }

    public static LoginResult fail(String message) {
        return new LoginResult(false, null, message);
    }
}
