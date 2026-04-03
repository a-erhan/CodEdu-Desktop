package com.codedu.services.interfaces;

import com.codedu.dtos.user.UserDTO;
import com.codedu.dtos.user.UserLoginDTO;
import com.codedu.dtos.user.UserRegisterDTO;

public interface AuthService {

    String register(UserRegisterDTO dto);

    UserDTO login(UserLoginDTO dto);
}
