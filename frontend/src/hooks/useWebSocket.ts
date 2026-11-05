import { useEffect, useRef, useState, useCallback } from 'react';
import { LobbyState, PlayerScore, ServerMessage } from '../types';

function resolveWsUrl(): string {
  const envUrl = import.meta.env.VITE_WS_URL;
  if (envUrl) {
    if (envUrl.startsWith('/')) {
      if (typeof window !== 'undefined') {
        const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        return `${proto}//${window.location.host}${envUrl}`;
      }
      return `ws://localhost:8081${envUrl}`;
    }
    return envUrl;
  }
  if (typeof window !== 'undefined') {
    const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    if (window.location.port === '3000') {
      return `${proto}//${window.location.hostname}:8081/ws`;
    }
    return `${proto}//${window.location.host}/ws`;
  }
  return 'ws://localhost:8081/ws';
}

const DEFAULT_WS_URL = resolveWsUrl();

export function useWebSocket() {
  const [connected, setConnected] = useState(false);
  const [connecting, setConnecting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [playerId, setPlayerId] = useState<string | null>(null);
  const [nickname, setNickname] = useState<string>('');
  const [lobbyState, setLobbyState] = useState<LobbyState | null>(null);
  const [scores, setScores] = useState<PlayerScore[]>([]);

  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<number | null>(null);
  const isMountedRef = useRef(false);
  const nicknameRef = useRef<string>('');
  nicknameRef.current = nickname;

  const connect = useCallback(() => {
    if (wsRef.current && (wsRef.current.readyState === WebSocket.OPEN || wsRef.current.readyState === WebSocket.CONNECTING)) {
      return;
    }

    setConnecting(true);
    setError(null);

    try {
      const ws = new WebSocket(DEFAULT_WS_URL);
      wsRef.current = ws;

      ws.onopen = () => {
        if (!isMountedRef.current) {
          try { ws.close(1000); } catch (_) {}
          return;
        }
        setConnected(true);
        setConnecting(false);
        setError(null);

        if (reconnectTimeoutRef.current) {
          window.clearTimeout(reconnectTimeoutRef.current);
          reconnectTimeoutRef.current = null;
        }

        // If nickname was already entered and connection restored, re-join seamlessly
        if (nicknameRef.current) {
          ws.send(JSON.stringify({ type: 'JOIN', nickname: nicknameRef.current }));
        }
      };

      ws.onmessage = (event) => {
        try {
          const msg: ServerMessage = JSON.parse(event.data);
          switch (msg.type) {
            case 'JOINED':
              if (msg.playerId) {
                setPlayerId(msg.playerId);
              }
              break;
            case 'LOBBY_STATE':
              if (msg.state) {
                setLobbyState(msg.state);
              }
              break;
            case 'SCORES':
              if (msg.scores) {
                setScores(msg.scores);
              }
              break;
            case 'ERROR':
              setError(msg.message || msg.code || 'Ошибка сервера');
              setPlayerId(null);
              break;
          }
        } catch (e) {
          console.error('Failed to parse WS message:', e);
        }
      };

      ws.onerror = () => {
        setConnecting(false);
        // Browser triggers onclose right after onerror
      };

      ws.onclose = () => {
        setConnected(false);
        setConnecting(false);
        wsRef.current = null;

        // Auto-reconnect if component is still active
        if (isMountedRef.current && !reconnectTimeoutRef.current) {
          reconnectTimeoutRef.current = window.setTimeout(() => {
            reconnectTimeoutRef.current = null;
            if (isMountedRef.current) {
              connect();
            }
          }, 1500);
        }
      };
    } catch (err) {
      console.error('WebSocket connection error:', err);
      setConnecting(false);
      setConnected(false);
      wsRef.current = null;
    }
  }, []);

  const sendMessage = useCallback((payload: Record<string, unknown>) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(payload));
    }
  }, []);

  const join = useCallback((nick: string) => {
    nicknameRef.current = nick;
    setNickname(nick);
    setError(null);
    sendMessage({ type: 'JOIN', nickname: nick });
  }, [sendMessage]);

  const ready = useCallback(() => {
    sendMessage({ type: 'READY' });
  }, [sendMessage]);

  const sendClick = useCallback(() => {
    sendMessage({ type: 'CLICK' });
  }, [sendMessage]);

  const leave = useCallback(() => {
    nicknameRef.current = '';
    sendMessage({ type: 'LEAVE' });
    setPlayerId(null);
    setNickname('');
    setScores([]);
    setLobbyState((prev) => {
      if (!prev) return prev;
      const updatedPlayers = prev.players.filter(
        (p) => p.nickname.toLowerCase() !== nickname.toLowerCase()
      );
      const canReady = updatedPlayers.length >= (prev.minPlayers || 5);
      const allReady = canReady && updatedPlayers.every((p) => p.ready);
      return {
        ...prev,
        players: updatedPlayers,
        canReady,
        allReady,
      };
    });
  }, [sendMessage, nickname]);

  const restart = useCallback(() => {
    sendMessage({ type: 'RESTART' });
    setScores([]);
    setLobbyState((prev) => {
      if (!prev) return prev;
      return {
        ...prev,
        allReady: false,
        players: prev.players.map((p) => ({ ...p, ready: false })),
      };
    });
  }, [sendMessage]);

  useEffect(() => {
    isMountedRef.current = true;
    connect();

    const handleBeforeUnload = () => {
      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        wsRef.current.close(1000, 'Page unloaded');
      }
    };
    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      isMountedRef.current = false;
      window.removeEventListener('beforeunload', handleBeforeUnload);
      if (reconnectTimeoutRef.current) {
        window.clearTimeout(reconnectTimeoutRef.current);
        reconnectTimeoutRef.current = null;
      }
      if (wsRef.current) {
        const ws = wsRef.current;
        wsRef.current = null;
        ws.onopen = null;
        ws.onclose = null;
        ws.onerror = null;
        ws.onmessage = null;
        try {
          ws.close(1000, 'Component unmounted');
        } catch (_) {}
      }
    };
  }, [connect]);

  return {
    connected,
    connecting,
    error,
    setError,
    playerId,
    nickname,
    lobbyState,
    scores,
    join,
    ready,
    sendClick,
    leave,
    restart,
    reconnect: connect,
  };
}
