package com.codedu.services.interfaces;

import com.codedu.models.user.User;

public interface ChatWindowManager {

    void connectUser(User user);

    void openChatWindow(User friend);
}
