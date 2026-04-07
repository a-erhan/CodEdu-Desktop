package com.codedu.services.interfaces;

import com.codedu.dtos.user.LoginResult;
import com.codedu.dtos.user.UserLoginDTO;
import com.codedu.dtos.user.UserRegisterDTO;

public interface AuthService {

    String register(UserRegisterDTO dto);

    LoginResult login(UserLoginDTO dto);

    String verifyEmailToken(String token);

    String resendVerificationEmail(String email);
}
