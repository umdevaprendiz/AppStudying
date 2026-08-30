import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { API_BASE_URL } from "./http";

export function connectStudyRequestSocket(userId, { onNotification, onStatusChange }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    reconnectDelay: 5000,
    onConnect: () => {
      onStatusChange?.(true);
      client.subscribe(`/topic/study-requests/${userId}`, (message) => {
        onNotification(JSON.parse(message.body));
      });
    },
    onWebSocketClose: () => {
      onStatusChange?.(false);
    },
  });

  client.activate();
  return client;
}

export function connectChatSocket(userId, { onMessage, onStatusChange }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    reconnectDelay: 5000,
    onConnect: () => {
      onStatusChange?.(true);
      client.subscribe(`/topic/chat/${userId}`, (message) => {
        onMessage(JSON.parse(message.body));
      });
    },
    onWebSocketClose: () => {
      onStatusChange?.(false);
    },
  });

  client.activate();
  return client;
}
