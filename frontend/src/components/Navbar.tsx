import React from 'react';
import { Zap, Volume2, VolumeX, Wifi, WifiOff } from 'lucide-react';
import { sound } from '../utils/sound';

interface NavbarProps {
  connected: boolean;
  soundEnabled: boolean;
  onToggleSound: () => void;
  nickname?: string;
}

export const Navbar: React.FC<NavbarProps> = ({
  connected,
  soundEnabled,
  onToggleSound,
  nickname,
}) => {
  return (
    <header className="navbar">
      <div className="navbar-logo">
        <div className="logo-icon-wrap">
          <Zap className="logo-icon" />
        </div>
        <span className="logo-title">CLICKORT</span>
        <span className="logo-badge">ARENA</span>
      </div>

      <div className="navbar-actions">
        {nickname && (
          <div className="player-badge">
            <span className="avatar-dot" />
            <span className="player-nick">{nickname}</span>
          </div>
        )}

        <button
          className="icon-btn"
          onClick={() => {
            sound.enabled = !soundEnabled;
            onToggleSound();
          }}
          title={soundEnabled ? 'Выключить звук' : 'Включить звук'}
        >
          {soundEnabled ? <Volume2 size={18} /> : <VolumeX size={18} />}
        </button>

        <div className={`status-pill ${connected ? 'online' : 'offline'}`}>
          {connected ? <Wifi size={14} /> : <WifiOff size={14} />}
          <span>{connected ? 'Онлайн (8081)' : 'Отключен'}</span>
        </div>
      </div>
    </header>
  );
};
