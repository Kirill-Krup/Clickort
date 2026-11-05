import React, { useEffect } from 'react';
import confetti from 'canvas-confetti';
import { Trophy, Medal, RotateCcw, Award } from 'lucide-react';
import { PlayerScore } from '../types';
import { sound } from '../utils/sound';

interface ResultModalProps {
  scores: PlayerScore[];
  currentNickname: string;
  onPlayAgain: () => void;
}

export const ResultModal: React.FC<ResultModalProps> = ({
  scores,
  currentNickname,
  onPlayAgain,
}) => {
  const winner = scores[0];
  const myIndex = scores.findIndex(
    (s) => s.nickname.toLowerCase() === currentNickname.toLowerCase()
  );
  const myRank = myIndex + 1;
  const isMeWinner = winner && winner.nickname.toLowerCase() === currentNickname.toLowerCase();

  useEffect(() => {
    sound.playVictory();

    // Trigger celebration confetti
    const duration = 2.5 * 1000;
    const end = Date.now() + duration;

    const frame = () => {
      confetti({
        particleCount: 3,
        angle: 60,
        spread: 55,
        origin: { x: 0 },
      });
      confetti({
        particleCount: 3,
        angle: 120,
        spread: 55,
        origin: { x: 1 },
      });

      if (Date.now() < end) {
        requestAnimationFrame(frame);
      }
    };
    frame();
  }, []);

  return (
    <div className="modal-overlay">
      <div className="result-modal glass-panel">
        <div className="modal-header">
          <div className="trophy-badge">
            <Trophy className="modal-trophy" size={48} />
          </div>
          <h2 className="modal-title">РАУНД ЗАВЕРШЁН!</h2>
          <p className="modal-subtitle">
            {isMeWinner
              ? '🎉 Поздравляем! Вы заняли 1-е место!'
              : winner
              ? `Победитель раунда: ${winner.nickname}`
              : 'Время вышло!'}
          </p>
        </div>

        {/* Podium Top 3 */}
        <div className="podium-grid">
          {scores.slice(0, 3).map((item, idx) => {
            const place = idx + 1;
            const isMe = item.nickname.toLowerCase() === currentNickname.toLowerCase();
            return (
              <div key={item.nickname} className={`podium-card place-${place} ${isMe ? 'self' : ''}`}>
                <div className="podium-rank">
                  {place === 1 && <Trophy size={20} />}
                  {place === 2 && <Medal size={20} />}
                  {place === 3 && <Medal size={20} />}
                  <span>{place} МЕСТО</span>
                </div>
                <div className="podium-nick">{item.nickname}</div>
                <div className="podium-score">{item.score.toLocaleString()} кликов</div>
              </div>
            );
          })}
        </div>

        {/* Your Result Banner */}
        <div className="personal-result-card">
          <div className="flex-center gap-2">
            <Award className="award-icon" size={24} />
            <div>
              <span className="res-label">ВАШ РЕЗУЛЬТАТ</span>
              <h4 className="res-value">
                {myRank > 0 ? `#${myRank} место` : 'Вне рейтинга'} —{' '}
                {myIndex >= 0 ? scores[myIndex].score.toLocaleString() : 0} кликов
              </h4>
            </div>
          </div>
        </div>

        <button className="primary-btn glow-btn modal-btn" onClick={onPlayAgain}>
          <RotateCcw size={18} />
          <span>Вернуться в Лобби</span>
        </button>
      </div>
    </div>
  );
};
