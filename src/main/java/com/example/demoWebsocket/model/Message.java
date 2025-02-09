package com.example.demoWebsocket.model;



import lombok.Value;

import java.time.LocalDateTime;

@Value
public class Message {
    String from;
    String message;
    LocalDateTime timeStamp;
}
