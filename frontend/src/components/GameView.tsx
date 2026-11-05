import React, { useEffect, useRef, useState, useCallback } from 'react';
import { Flame, Gauge, Sparkles, Timer, Zap } from 'lucide-react';
import { PlayerScore, ClickParticle } from '../types';
import { Leaderboard } from './Leaderboard';
import { CountdownOverlay } from './CountdownOverlay';
import { sound } from '../utils/sound';

interface GameViewProps {
  nickname: string;
  scores: PlayerScore[];
  onSendClick: () => void;
  onFinish: (finalScores: PlayerScore[]) => void;
  isCountdown?: boolean;
  onCountdownComplete?: () => void;
}

const ROUND_DURATION_SECONDS = 60;

export const GameView: React.FC<GameViewProps> = ({
  nickname,
  scores,
  onSendClick,
  onFinish,
  isCountdown = false,
  onCountdownComplete,
}) => {
  const [timeLeft, setTimeLeft] = useState(ROUND_DURATION_SECONDS);
  const [localScore, setLocalScore] = useState(0);
  const [cps, setCps] = useState(0);
  const [particles, setParticles] = useState<ClickParticle[]>([]);
  const [isPressed, setIsPressed] = useState(false);

  const clickTimestampsRef = useRef<number[]>([]);
  const nextParticleId = useRef(0);
  const scoresRef = useRef(scores);
  scoresRef.current = scores;
  const onFinishRef = useRef(onFinish);
  onFinishRef.current = onFinish;

  // Sync with server score if server score is higher (reconciliation)
  useEffect(() => {
    const serverItem = scores.find(
      (s) => s.nickname.toLowerCase() === nickname.toLowerCase()
    );
    if (serverItem && serverItem.score > localScore) {
      setLocalScore(serverItem.score);
    }
  }, [scores, nickname, localScore]);

  // Round Timer countdown
  useEffect(() => {
    if (isCountdown) return;

    const timer = window.setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          window.clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => window.clearInterval(timer);
  }, [isCountdown]);

  useEffect(() => {
    if (timeLeft === 0) {
      onFinishRef.current(scoresRef.current);
    }
  }, [timeLeft]);

  // CPS rolling window calculation (every 250ms)
  useEffect(() => {
    const interval = window.setInterval(() => {
      const now = performance.now();
      clickTimestampsRef.current = clickTimestampsRef.current.filter(
        (t) => now - t <= 1000
      );
      setCps(clickTimestampsRef.current.length);
    }, 250);

    return () => window.clearInterval(interval);
  }, []);

  // Handle Click action (Optimistic UI + WS Dispatch + Sound + Particle)
  const handleClick = useCallback((e?: React.MouseEvent | React.TouchEvent) => {
    if (timeLeft <= 0) return;

    // 1. Sound & audio feedback
    sound.playClick();

    // 2. Optimistic local increment
    setLocalScore((prev) => prev + 1);

    // 3. Register click timestamp for CPS
    clickTimestampsRef.current.push(performance.now());

    // 4. Send over WebSocket
    onSendClick();

    // 5. Visual floating particle
    let clientX = window.innerWidth / 2;
    let clientY = window.innerHeight / 2;

    if (e) {
      if ('touches' in e && e.touches.length > 0) {
        clientX = e.touches[0].clientX;
        clientY = e.touches[0].clientY;
      } else if ('clientX' in e) {
        clientX = e.clientX;
        clientY = e.clientY;
      }
    }

    const particleId = nextParticleId.current++;
    // Add jitter
    const offsetJitter = (Math.random() - 0.5) * 40;
    setParticles((prev) => [
      ...prev.slice(-15), // keep last 15 active particles
      {
        id: particleId,
        x: clientX + offsetJitter,
        y: clientY - 20,
        value: '+1',
      },
    ]);

    // Auto cleanup particle
    window.setTimeout(() => {
      setParticles((prev) => prev.filter((p) => p.id !== particleId));
    }, 700);
  }, [timeLeft, onSendClick]);

  // Support Spacebar pressing
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.code === 'Space' && !e.repeat && timeLeft > 0 && !isCountdown) {
        e.preventDefault();
        setIsPressed(true);
        handleClick();
      }
    };

    const handleKeyUp = (e: KeyboardEvent) => {
      if (e.code === 'Space') {
        e.preventDefault();
        setIsPressed(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, [handleClick, timeLeft]);

  // Current rank
  const myRank = scores.findIndex(
    (s) => s.nickname.toLowerCase() === nickname.toLowerCase()
  ) + 1;

  const progressPercent = (timeLeft / ROUND_DURATION_SECONDS) * 100;
  const isTimeCritical = timeLeft <= 10;

  return (
    <div className="game-container">
      {/* Top Timer Bar */}
      <div className={`timer-banner glass-panel ${isTimeCritical ? 'critical' : ''}`}>
        <div className="timer-info">
          <div className="timer-left">
            <Timer className="timer-icon" size={24} />
            <span className="timer-title">ВРЕМЯ РАУНДА:</span>
          </div>
          <div className="timer-display">
            <span className="timer-seconds">{timeLeft}</span>
            <span className="timer-unit">сек</span>
          </div>
        </div>
        <div className="timer-progress-track">
          <div
            className={`timer-progress-fill ${isTimeCritical ? 'critical-fill' : ''}`}
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>

      <div className="game-grid">
        {/* Main Clicking Arena */}
        <div className="arena-card glass-panel">
          {/* Stats Bar */}
          <div className="stats-row">
            <div className="stat-pill">
              <Sparkles size={16} className="stat-icon purple" />
              <div>
                <span className="stat-label">ВАШ СЧЁТ</span>
                <strong className="stat-val">{localScore.toLocaleString()}</strong>
              </div>
            </div>

            <div className="stat-pill">
              <Gauge size={16} className="stat-icon yellow" />
              <div>
                <span className="stat-label">СКОРОСТЬ (CPS)</span>
                <strong className="stat-val">{cps} <small>кл/с</small></strong>
              </div>
            </div>

            <div className="stat-pill">
              <Flame size={16} className="stat-icon red" />
              <div>
                <span className="stat-label">ПОЗИЦИЯ</span>
                <strong className="stat-val">
                  {myRank > 0 ? `#${myRank}` : '-'}
                </strong>
              </div>
            </div>
          </div>

          {/* Click Button Area */}
          <div className="click-target-wrap">
            {isCountdown ? (
              <CountdownOverlay onComplete={onCountdownComplete || (() => {})} />
            ) : (
              <button
                className={`mega-click-btn ${isPressed ? 'pressed' : ''} ${timeLeft <= 0 ? 'disabled' : ''}`}
                onMouseDown={(e) => {
                  setIsPressed(true);
                  handleClick(e);
                }}
                onMouseUp={() => setIsPressed(false)}
                onTouchStart={(e) => {
                  setIsPressed(true);
                  handleClick(e);
                }}
                onTouchEnd={() => setIsPressed(false)}
                disabled={timeLeft <= 0}
              >
                <div className="btn-inner-glow" />
                <div className="btn-content">
                  <Zap className="mega-zap" size={64} />
                  <span className="btn-title">КЛИКАЙ!</span>
                  <span className="btn-hint">ЛКМ или Пробел</span>
                </div>
                <div className="pulse-ring" />
                <div className="pulse-ring delay" />
              </button>
            )}
          </div>

          <div className="arena-footer">
            <span>Каждый клик отправляется в Kafka топик и обрабатывается потоками Loom</span>
          </div>
        </div>

        {/* Live Leaderboard Sidebar */}
        <Leaderboard scores={scores} currentNickname={nickname} />
      </div>

      {/* Floating +1 Particles */}
      {particles.map((p) => (
        <div
          key={p.id}
          className="floating-particle"
          style={{ left: `${p.x}px`, top: `${p.y}px` }}
        >
          {p.value}
        </div>
      ))}
    </div>
  );
};
