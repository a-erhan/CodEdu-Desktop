package com.codedu.services.interfaces;

import com.codedu.models.user.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public interface FriendshipUIManager {

    void setupAddFriendButton(Button addFriendButton, User currentUser, User profileUser, boolean viewingSelf);

    void renderFriendsList(VBox friendsList, Label noFriendsLabel, User currentUser, boolean viewingSelf,
            Runnable onRefresh, Consumer<User> onProfileClick);
}
