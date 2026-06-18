import { useState, useEffect, useRef } from 'react';
import EyeBall from './EyeBall';
import Pupil from './Pupil';

interface LoginCharactersProps {
  /** 密码字段是否有内容 */
  hasPassword: boolean;
  /** 密码是否可见（明文） */
  passwordVisible: boolean;
  /** 输入框是否获得焦点 */
  isTyping: boolean;
}

/**
 * 登录页左侧动画角色组件
 * 4个角色跟随鼠标移动，输入时互相看，密码可见时偷看
 */
const LoginCharacters = ({ hasPassword, passwordVisible, isTyping }: LoginCharactersProps) => {
  const [mouseX, setMouseX] = useState(0);
  const [mouseY, setMouseY] = useState(0);
  const [isBlueBlinking, setIsBlueBlinking] = useState(false);
  const [isDarkBlinking, setIsDarkBlinking] = useState(false);
  const [isLookingAtEachOther, setIsLookingAtEachOther] = useState(false);
  const [isBluePeeking, setIsBluePeeking] = useState(false);

  const blueRef = useRef<HTMLDivElement>(null);
  const darkRef = useRef<HTMLDivElement>(null);
  const lightRef = useRef<HTMLDivElement>(null);
  const skyRef = useRef<HTMLDivElement>(null);

  // 鼠标追踪
  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      setMouseX(e.clientX);
      setMouseY(e.clientY);
    };
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);

  // 蓝色角色眨眼
  useEffect(() => {
    let timeout: ReturnType<typeof setTimeout>;
    const scheduleBlink = () => {
      timeout = setTimeout(() => {
        setIsBlueBlinking(true);
        setTimeout(() => {
          setIsBlueBlinking(false);
          scheduleBlink();
        }, 150);
      }, Math.random() * 4000 + 3000);
    };
    scheduleBlink();
    return () => clearTimeout(timeout);
  }, []);

  // 深色角色眨眼
  useEffect(() => {
    let timeout: ReturnType<typeof setTimeout>;
    const scheduleBlink = () => {
      timeout = setTimeout(() => {
        setIsDarkBlinking(true);
        setTimeout(() => {
          setIsDarkBlinking(false);
          scheduleBlink();
        }, 150);
      }, Math.random() * 4000 + 3000);
    };
    scheduleBlink();
    return () => clearTimeout(timeout);
  }, []);

  // 打字时互相看
  useEffect(() => {
    if (isTyping) {
      setIsLookingAtEachOther(true);
      const timer = setTimeout(() => setIsLookingAtEachOther(false), 800);
      return () => clearTimeout(timer);
    } else {
      setIsLookingAtEachOther(false);
    }
  }, [isTyping]);

  // 密码可见时偷看动画
  useEffect(() => {
    if (hasPassword && passwordVisible) {
      const peekTimeout = setTimeout(() => {
        setIsBluePeeking(true);
        setTimeout(() => setIsBluePeeking(false), 800);
      }, Math.random() * 3000 + 2000);
      return () => clearTimeout(peekTimeout);
    } else {
      setIsBluePeeking(false);
    }
  }, [hasPassword, passwordVisible, isBluePeeking]);

  const calculatePosition = (ref: React.RefObject<HTMLDivElement | null>) => {
    if (!ref.current) return { faceX: 0, faceY: 0, bodySkew: 0 };
    const rect = ref.current.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 3;
    const deltaX = mouseX - centerX;
    const deltaY = mouseY - centerY;
    const faceX = Math.max(-15, Math.min(15, deltaX / 20));
    const faceY = Math.max(-10, Math.min(10, deltaY / 30));
    const bodySkew = Math.max(-6, Math.min(6, -deltaX / 120));
    return { faceX, faceY, bodySkew };
  };

  const bluePos = calculatePosition(blueRef);
  const darkPos = calculatePosition(darkRef);
  const lightPos = calculatePosition(lightRef);
  const skyPos = calculatePosition(skyRef);

  const showPeekState = hasPassword && passwordVisible;
  const showTypeState = isTyping || (hasPassword && !passwordVisible);

  return (
    <div className="characters-wrapper">
      {/* 蓝色高个角色 - 后层 */}
      <div
        ref={blueRef}
        className="character character--purple"
        style={{
          height: showTypeState ? 440 : 400,
          transform: showPeekState
            ? 'skewX(0deg)'
            : showTypeState
              ? `skewX(${(bluePos.bodySkew || 0) - 12}deg) translateX(40px)`
              : `skewX(${bluePos.bodySkew || 0}deg)`,
        }}
      >
        <div
          className="character__eyes"
          style={{
            left: showPeekState ? 20 : isLookingAtEachOther ? 55 : 45 + bluePos.faceX,
            top: showPeekState ? 35 : isLookingAtEachOther ? 65 : 40 + bluePos.faceY,
          }}
        >
          <EyeBall
            size={18}
            pupilSize={7}
            maxDistance={5}
            isBlinking={isBlueBlinking}
            forceLookX={showPeekState ? (isBluePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : undefined}
            forceLookY={showPeekState ? (isBluePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : undefined}
            mouseX={mouseX}
            mouseY={mouseY}
          />
          <EyeBall
            size={18}
            pupilSize={7}
            maxDistance={5}
            isBlinking={isBlueBlinking}
            forceLookX={showPeekState ? (isBluePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : undefined}
            forceLookY={showPeekState ? (isBluePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : undefined}
            mouseX={mouseX}
            mouseY={mouseY}
          />
        </div>
      </div>

      {/* 深色高个角色 - 中层 */}
      <div
        ref={darkRef}
        className="character character--dark"
        style={{
          transform: showPeekState
            ? 'skewX(0deg)'
            : isLookingAtEachOther
              ? `skewX(${(darkPos.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`
              : showTypeState
                ? `skewX(${(darkPos.bodySkew || 0) * 1.5}deg)`
                : `skewX(${darkPos.bodySkew || 0}deg)`,
        }}
      >
        <div
          className="character__eyes"
          style={{
            left: showPeekState ? 10 : isLookingAtEachOther ? 32 : 26 + darkPos.faceX,
            top: showPeekState ? 28 : isLookingAtEachOther ? 12 : 32 + darkPos.faceY,
          }}
        >
          <EyeBall
            size={16}
            pupilSize={6}
            maxDistance={4}
            isBlinking={isDarkBlinking}
            forceLookX={showPeekState ? -4 : isLookingAtEachOther ? 0 : undefined}
            forceLookY={showPeekState ? -4 : isLookingAtEachOther ? -4 : undefined}
            mouseX={mouseX}
            mouseY={mouseY}
          />
          <EyeBall
            size={16}
            pupilSize={6}
            maxDistance={4}
            isBlinking={isDarkBlinking}
            forceLookX={showPeekState ? -4 : isLookingAtEachOther ? 0 : undefined}
            forceLookY={showPeekState ? -4 : isLookingAtEachOther ? -4 : undefined}
            mouseX={mouseX}
            mouseY={mouseY}
          />
        </div>
      </div>

      {/* 浅蓝半圆角色 - 前左 */}
      <div
        ref={lightRef}
        className="character character--orange"
        style={{
          transform: showPeekState ? 'skewX(0deg)' : `skewX(${lightPos.bodySkew || 0}deg)`,
        }}
      >
        <div
          className="character__eyes character__eyes--pupil"
          style={{
            left: showPeekState ? 50 : 82 + (lightPos.faceX || 0),
            top: showPeekState ? 85 : 90 + (lightPos.faceY || 0),
          }}
        >
          <Pupil size={12} maxDistance={5} forceLookX={showPeekState ? -5 : undefined} forceLookY={showPeekState ? -4 : undefined} mouseX={mouseX} mouseY={mouseY} />
          <Pupil size={12} maxDistance={5} forceLookX={showPeekState ? -5 : undefined} forceLookY={showPeekState ? -4 : undefined} mouseX={mouseX} mouseY={mouseY} />
        </div>
      </div>

      {/* 天蓝圆顶角色 - 前右 */}
      <div
        ref={skyRef}
        className="character character--sky"
        style={{
          transform: showPeekState ? 'skewX(0deg)' : `skewX(${skyPos.bodySkew || 0}deg)`,
        }}
      >
        <div
          className="character__eyes character__eyes--pupil"
          style={{
            left: showPeekState ? 20 : 52 + (skyPos.faceX || 0),
            top: showPeekState ? 35 : 40 + (skyPos.faceY || 0),
          }}
        >
          <Pupil size={12} maxDistance={5} forceLookX={showPeekState ? -5 : undefined} forceLookY={showPeekState ? -4 : undefined} mouseX={mouseX} mouseY={mouseY} />
          <Pupil size={12} maxDistance={5} forceLookX={showPeekState ? -5 : undefined} forceLookY={showPeekState ? -4 : undefined} mouseX={mouseX} mouseY={mouseY} />
        </div>
        {/* 嘴巴横线 */}
        <div
          className="character__mouth"
          style={{
            left: showPeekState ? 10 : 40 + (skyPos.faceX || 0),
            top: showPeekState ? 88 : 88 + (skyPos.faceY || 0),
          }}
        />
      </div>
    </div>
  );
};

export default LoginCharacters;
