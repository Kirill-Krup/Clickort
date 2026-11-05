import React from 'react';
import { CheckCircle2, Clock, Crown, ShieldAlert, Sparkles, UserCheck, Users, Zap } from 'lucide-react';
import { LobbyState } from '../types';

interface LobbyViewProps {
  nickname: string;
  lobbyState: LobbyState | null;
  onReady: () => void;
  onLeave?: () => void;
  error: string | null;
  onClearError: () => void;
}

export const LobbyView: React.FC<LobbyViewProps> = ({
  nickname,
  lobbyState,
  onReady,
  onLeave,
  error,
  onClearError,
}) => {
  const players = lobbyState?.players || [];
  const minPlayers = lobbyState?.minPlayers || 5;
  const canReady = lobbyState?.canReady || false;
  const allReady = lobbyState?.allReady || false;

  const currentPlayer = players.find(
    (p) => p.nickname.toLowerCase() === nickname.toLowerCase()
  );
  const isSelfReady = currentPlayer?.ready || false;
  const readyCount = players.filter((p) => p.ready).length;

  return (
    <div className="lobby-container">
      {error && (
        <div className="error-banner">
          <ShieldAlert size={18} />
          <span>{error}</span>
          <button className="error-close" onClick={onClearError}>×</button>
        </div>
      )}

      <div className="lobby-grid">
        {/* Main Room Card */}
        <div className="lobby-card glass-panel main-panel">
          <div className="panel-header">
            <div>
              <span className="room-label">ОБЩАЯ КОМНАТА</span>
              <h2 className="panel-title">Ожидание игроков</h2>
            </div>
            <div className="players-count-badge">
              <Users size={16} />
              <span>{players.length} / {minPlayers} игроков</span>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="progress-section">
            <div className="progress-labels">
              <span>Готовность комнаты:</span>
              <span className="progress-value">
                {players.length >= minPlayers 
                  ? `${readyCount} из ${players.length} готовы` 
                  : `Не хватает ${minPlayers - players.length} участников`}
              </span>
            </div>
            <div className="progress-track">
              <div
                className={`progress-fill ${canReady ? 'active' : ''}`}
                style={{
                  width: `${Math.min(100, (players.length / minPlayers) * 100)}%`,
                }}
              />
            </div>
          </div>

          {/* Players Grid */}
          <div className="players-list-wrap">
            <h3 className="section-title">Участники лобби:</h3>
            <div className="players-grid">
              {players.map((p) => {
                const isMe = p.nickname.toLowerCase() === nickname.toLowerCase();
                return (
                  <div
                    key={p.nickname}
                    className={`player-item-card ${isMe ? 'self' : ''} ${p.ready ? 'ready' : ''}`}
                  >
                    <div className="player-avatar">
                      {p.nickname.slice(0, 2).toUpperCase()}
                      {isMe && <Crown className="self-crown" size={14} />}
                    </div>
                    <div className="player-info">
                      <span className="p-nick">
                        {p.nickname} {isMe && <span className="you-tag">(Вы)</span>}
                      </span>
                      <span className={`p-status ${p.ready ? 'ready' : 'waiting'}`}>
                        {p.ready ? (
                          <>
                            <CheckCircle2 size={12} /> Готов
                          </>
                        ) : (
                          <>
                            <Clock size={12} /> Ожидает
                          </>
                        )}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Ready Button Section */}
          <div className="ready-action-box">
            {allReady ? (
              <div className="all-ready-banner">
                <Sparkles className="spin-icon" size={20} />
                <span>Все игроки готовы! Раунд вот-вот начнется...</span>
              </div>
            ) : isSelfReady ? (
              <div className="self-ready-status">
                <UserCheck size={22} className="check-glow" />
                <div>
                  <h4>Вы подтвердили готовность!</h4>
                  <p>Ожидаем, пока остальные игроки нажмут «ГОТОВ»...</p>
                </div>
              </div>
            ) : (
              <button
                className={`ready-btn glow-btn ${!canReady ? 'disabled' : ''}`}
                onClick={onReady}
                disabled={!canReady}
              >
                <Zap size={20} />
                <span>{canReady ? 'Я ГОТОВ!' : `Нужно минимум ${minPlayers} игроков`}</span>
              </button>
            )}
            {onLeave && (
              <button
                className="leave-lobby-btn"
                onClick={onLeave}
                title="Покинуть лобби"
              >
                ← Покинуть комнату
              </button>
            )}
          </div>
        </div>

        {/* Rules Sidebar */}
        <div className="rules-card glass-panel">
          <h3 className="rules-title">Правила Арены</h3>
          <ul className="rules-list">
            <li>
              <div className="rule-num">1</div>
              <div>
                <strong>Сбор лобби</strong>
                <p>Кнопка «ГОТОВ» активируется при сборе от 5 участников.</p>
              </div>
            </li>
            <li>
              <div className="rule-num">2</div>
              <div>
                <strong>Раунд 60 секунд</strong>
                <p>Игра стартует одновременно для всех после готовности каждого игрока.</p>
              </div>
            </li>
            <li>
              <div className="rule-num">3</div>
              <div>
                <strong>Высокая нагрузка</strong>
                <p>Клики обрабатываются асинхронно через виртуальные потоки и Kafka.</p>
              </div>
            </li>
            <li>
              <div className="rule-num">4</div>
              <div>
                <strong>Победа</strong>
                <p>Побеждает игрок с наибольшим числом кликов в таблице лидеров Redis.</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
};
