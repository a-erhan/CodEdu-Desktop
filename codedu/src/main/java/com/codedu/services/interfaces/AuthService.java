package com.codedu.services.interfaces;

import com.codedu.models.user.User;

public interface AuthService {

    String register(String email, String password);

    User login(String email, String password);
}
