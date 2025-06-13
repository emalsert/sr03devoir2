import React from 'react';
import Lottie from 'lottie-react';
import lottieLoader from '../static/lotties/lottieLoader.json';

const LottieLoader = ({ size = 120, style = {} }) => {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', ...style }}>
      <Lottie
        animationData={lottieLoader}
        loop
        style={{ width: size, height: size }}
      />
    </div>
  );
};

export default LottieLoader; 