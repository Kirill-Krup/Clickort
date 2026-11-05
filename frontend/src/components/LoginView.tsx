import React, { useState } from 'react';
import { ArrowRight, Flame, ShieldAlert, Sparkles, Users } from 'lucide-react';

interface LoginViewProps {
  onJoin: (nickname: string) => void;
  connected: boolean;
  error: string | null;
  onClearError: () => void;
}

export const LoginView: React.FC<LoginViewProps> = ({
  onJoin,
  connected,
  error,
  onClearError,
}) => {
  const [nick, setNick] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = nick.trim();
    if (trimmed.length > 0 && trimmed.length <= 20) {
      onJoin(trimmed);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card glass-panel">
        <div className="login-header">
          <div className="login-icon-badge">
            <Flame className="flame-icon" />
          </div>
          <h1 className="login-title">Битва Кликеров</h1>
          <p className="login-subtitle">
            Соревнуйтесь в реальном времени. Раунд 60 секунд. 
            Каждый клик обрабатывается через <span className="highlight">Kafka</span> и ранжируется в <span className="highlight">Redis ZSet</span>.
          </p>
        </div>

        {error && (
          <div className="error-banner">
            <ShieldAlert size={18} />
            <span>{error}</span>
            <button className="error-close" onClick={onClearError}>×</button>
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="input-group">
            <label htmlFor="nickname">Выберите никнейм</label>
            <div className="input-wrap">
              <input
                id="nickname"
                type="text"
                placeholder="Например: CyberSpeed"
                value={nick}
                onChange={(e) => {
                  setNick(e.target.value);
                  if (error) onClearError();
                }}
                maxLength={20}
                autoFocus
                required
              />
              <span className="char-counter">{nick.length}/20</span>
            </div>
          </div>

          <button
            type="submit"
            className="primary-btn glow-btn"
            disabled={!connected || nick.trim().length === 0}
          >
            <span>Войти в Арену</span>
            <ArrowRight size={18} />
          </button>
        </form>

        <div className="login-features">
          <div className="feature-item">
            <Users size={16} />
            <span>Лобби от 5 человек</span>
          </div>
          <div className="feature-item">
            <Sparkles size={16} />
            <span>Мгновенный рейтинг</span>
          </div>
        </div>
      </div>
    </div>
  );
};
