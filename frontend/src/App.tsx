import React, { useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './hooks/useWebSocket';
import { GamePhase, PlayerScore } from './types';
import { Navbar } from './components/Navbar';
import { LoginView } from './components/LoginView';
import { LobbyView } from './components/LobbyView';
import { GameView } from './components/GameView';
import { ResultModal } from './components/ResultModal';

export const App: React.FC = () => {
  const {
    connected,
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
  } = useWebSocket();

  const [phase, setPhase] = useState<GamePhase>('LOGIN');
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [finalScores, setFinalScores] = useState<PlayerScore[]>([]);

  // Advance from LOGIN to LOBBY upon successful join confirmed by server
  useEffect(() => {
    if (playerId && nickname && phase === 'LOGIN') {
      setPhase('LOBBY');
    }
  }, [playerId, nickname, phase]);

  // When all players are ready, trigger countdown
  useEffect(() => {
    if (phase === 'LOBBY' && lobbyState?.allReady) {
      setPhase('COUNTDOWN');
    }
  }, [lobbyState?.allReady, phase]);

  const handleCountdownComplete = useCallback(() => {
    setPhase('PLAYING');
  }, []);

  const handleGameFinish = useCallback((endScores: PlayerScore[]) => {
    setFinalScores(endScores.length > 0 ? endScores : scores);
    setPhase('FINISHED');
  }, [scores]);

  const handlePlayAgain = useCallback(() => {
    restart();
    setPhase('LOBBY');
  }, [restart]);

  const handleLeaveLobby = useCallback(() => {
    leave();
    setPhase('LOGIN');
  }, [leave]);

  return (
    <div className="app-shell">
      <Navbar
        connected={connected}
        soundEnabled={soundEnabled}
        onToggleSound={() => setSoundEnabled((prev) => !prev)}
        nickname={phase !== 'LOGIN' ? nickname : undefined}
      />

      <main className="main-content">
        {phase === 'LOGIN' && (
          <LoginView
            onJoin={join}
            connected={connected}
            error={error}
            onClearError={() => setError(null)}
          />
        )}

        {phase === 'LOBBY' && (
          <LobbyView
            nickname={nickname}
            lobbyState={lobbyState}
            onReady={ready}
            onLeave={handleLeaveLobby}
            error={error}
            onClearError={() => setError(null)}
          />
        )}

        {(phase === 'COUNTDOWN' || phase === 'PLAYING') && (
          <GameView
            nickname={nickname}
            scores={scores}
            onSendClick={sendClick}
            onFinish={handleGameFinish}
            isCountdown={phase === 'COUNTDOWN'}
            onCountdownComplete={handleCountdownComplete}
          />
        )}

        {phase === 'FINISHED' && (
          <>
            <GameView
              nickname={nickname}
              scores={scores}
              onSendClick={() => {}}
              onFinish={() => {}}
            />
            <ResultModal
              scores={finalScores.length > 0 ? finalScores : scores}
              currentNickname={nickname}
              onPlayAgain={handlePlayAgain}
            />
          </>
        )}
      </main>

      <footer className="footer-credits">
        <span>Clickort Realtime Arena • Spring Boot 4 + Virtual Threads + Kafka + Redis ZSet</span>
      </footer>
    </div>
  );
};
