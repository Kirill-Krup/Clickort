import React, { useEffect, useRef, useState } from 'react';
import { ArrowDown, Sparkles } from 'lucide-react';
import { sound } from '../utils/sound';

interface CountdownOverlayProps {
  onComplete: () => void;
}

export const CountdownOverlay: React.FC<CountdownOverlayProps> = ({ onComplete }) => {
  const [count, setCount] = useState(3);
  const onCompleteRef = useRef(onComplete);
  onCompleteRef.current = onComplete;

  useEffect(() => {
    sound.playCountdown();
    const interval = window.setInterval(() => {
      setCount((prev) => {
        if (prev <= 1) {
          window.clearInterval(interval);
          return 0;
        }
        sound.playCountdown();
        return prev - 1;
      });
    }, 1000);

    return () => window.clearInterval(interval);
  }, []);

  useEffect(() => {
    if (count === 0) {
      sound.playStart();
      onCompleteRef.current();
    }
  }, [count]);

  return (
    <div className="countdown-clicker-area">
      {/* Arrow pointing down to the timer circle */}
      <div className="clicker-arrow-guide">
        <div className="clicker-arrow-badge">
          <Sparkles size={16} className="badge-sparkle" />
          <span>Тут будет кликер</span>
        </div>
        <div className="arrow-bounce-wrap">
          <ArrowDown className="bouncing-arrow" size={34} />
        </div>
      </div>

      {/* Target circle occupying the exact 240px clicker spot */}
      <div className="countdown-target-circle">
        <div className="countdown-ring-pulse" />
        <div className="countdown-ring-pulse delay" />
        <div className="countdown-content-inner">
          <span key={count} className={`countdown-number count-${count}`}>
            {count > 0 ? count : 'GO!'}
          </span>
          <span className="countdown-sub-hint">
            {count > 0 ? 'Приготовьте пальцы...' : 'КЛИКАЙТЕ!'}
          </span>
        </div>
      </div>
    </div>
  );
};
