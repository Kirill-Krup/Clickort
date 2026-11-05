import React from 'react';
import { Trophy, Medal, Flame } from 'lucide-react';
import { PlayerScore } from '../types';

interface LeaderboardProps {
  scores: PlayerScore[];
  currentNickname: string;
}

export const Leaderboard: React.FC<LeaderboardProps> = ({
  scores,
  currentNickname,
}) => {
  return (
    <div className="leaderboard-card glass-panel">
      <div className="leaderboard-header">
        <div className="flex-center gap-2">
          <Trophy className="gold-trophy" size={20} />
          <h3 className="leaderboard-title">Таблица Лидеров</h3>
        </div>
        <span className="live-tag">
          <span className="live-dot" /> LIVE
        </span>
      </div>

      <div className="leaderboard-list">
        {scores.length === 0 ? (
          <div className="leaderboard-empty">
            <Flame size={24} className="fade-icon" />
            <p>Кликов пока нет. Начните нажимать!</p>
          </div>
        ) : (
          scores.map((item, index) => {
            const rank = index + 1;
            const isMe = item.nickname.toLowerCase() === currentNickname.toLowerCase();

            return (
              <div
                key={item.nickname}
                className={`leaderboard-item rank-${rank} ${isMe ? 'self-item' : ''}`}
              >
                <div className="rank-col">
                  {rank === 1 && <span className="rank-badge gold"><Trophy size={14} /></span>}
                  {rank === 2 && <span className="rank-badge silver"><Medal size={14} /></span>}
                  {rank === 3 && <span className="rank-badge bronze"><Medal size={14} /></span>}
                  {rank > 3 && <span className="rank-badge number">#{rank}</span>}
                </div>

                <div className="nick-col">
                  <span className="item-nick">
                    {item.nickname} {isMe && <span className="me-badge">(Вы)</span>}
                  </span>
                </div>

                <div className="score-col">
                  <span className="item-score">{item.score.toLocaleString()}</span>
                  <span className="score-label">кликов</span>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
