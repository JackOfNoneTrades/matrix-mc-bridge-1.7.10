package de.jojii.matrixclientserver.Callbacks;

import java.io.IOException;
import java.util.List;

import de.jojii.matrixclientserver.Bot.Member;

public interface MemberCallback {

    void onResponse(List<Member> roomMember) throws IOException;
}
