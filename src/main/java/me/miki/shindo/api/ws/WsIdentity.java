package me.miki.shindo.api.ws;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WsIdentity {
    private final String uuid;
    private final String name;
    private final String[] roles;
    private final AccountType accountType;
}
