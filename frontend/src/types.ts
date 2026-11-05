export interface Player {
  nickname: string;
  ready: boolean;
}

export interface LobbyState {
  players: Player[];
  minPlayers: number;
  canReady: boolean;
  allReady: boolean;
}

export interface PlayerScore {
  nickname: string;
  score: number;
}

export type ServerMessageType = 'JOINED' | 'LOBBY_STATE' | 'SCORES' | 'ERROR';

export interface ServerMessage {
  type: ServerMessageType;
  playerId?: string;
  state?: LobbyState;
  scores?: PlayerScore[];
  code?: string;
  message?: string;
}

export type GamePhase = 'LOGIN' | 'LOBBY' | 'COUNTDOWN' | 'PLAYING' | 'FINISHED';

export interface ClickParticle {
  id: number;
  x: number;
  y: number;
  value: string;
}
