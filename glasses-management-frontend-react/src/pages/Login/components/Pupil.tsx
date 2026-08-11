import { useRef } from 'react';

interface PupilProps {
  size?: number;
  maxDistance?: number;
  pupilColor?: string;
  forceLookX?: number;
  forceLookY?: number;
  mouseX?: number;
  mouseY?: number;
}

const Pupil = ({
  size = 12,
  maxDistance = 5,
  pupilColor = '#2D2D2D',
  forceLookX,
  forceLookY,
  mouseX = 0,
  mouseY = 0,
}: PupilProps) => {
  const pupilRef = useRef<HTMLDivElement>(null);

  const calculatePosition = () => {
    if (forceLookX !== undefined && forceLookY !== undefined) {
      return { x: forceLookX, y: forceLookY };
    }
    if (!pupilRef.current) return { x: 0, y: 0 };
    const rect = pupilRef.current.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const deltaX = mouseX - centerX;
    const deltaY = mouseY - centerY;
    const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), maxDistance);
    const angle = Math.atan2(deltaY, deltaX);
    return { x: Math.cos(angle) * distance, y: Math.sin(angle) * distance };
  };

  const pos = calculatePosition();

  return (
    <div
      ref={pupilRef}
      className="pupil-dot"
      style={{
        width: size,
        height: size,
        backgroundColor: pupilColor,
        transform: `translate(${pos.x}px, ${pos.y}px)`,
      }}
    />
  );
};

export default Pupil;
