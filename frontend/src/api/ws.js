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

export function connectInboxSocket(userId, { onCount }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(`/topic/chat-inbox/${userId}`, (message) => {
        onCount(Number(JSON.parse(message.body)));
      });
    },
  });

  client.activate();
  return client;
}

export function connectGroupInviteSocket(userId, { onInvite }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(`/topic/group-invites/${userId}`, (message) => {
        onInvite(JSON.parse(message.body));
      });
    },
  });

  client.activate();
  return client;
}

export function connectGroupPresenceSocket(groupId, { onPresence }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(`/topic/group-presence/${groupId}`, (message) => {
        onPresence(JSON.parse(message.body));
      });
    },
  });

  client.activate();
  return client;
}

export function connectGroupChatSocket(groupId, { onMessage }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(`/topic/group-chat/${groupId}`, (message) => {
        onMessage(JSON.parse(message.body));
      });
    },
  });

  client.activate();
  return client;
}
