package cnic.sdc.androidaudiorecorder;

import com.cleveroad.audiovisualization.DbmHandler;

public class VisualizerHandler extends DbmHandler<Float> {

    /**
     * @param amplitude 振幅
     * @param layersCount 图层数
     * @param dBmArray 标准化后的dBm数组, 长度等于{@code layersCount}
     * @param ampsArray 振幅数组, 长度等于{@code layersCount}, 该值用于调节播放时气泡的外观
     */
    @Override
    protected void onDataReceivedImpl(Float amplitude, int layersCount, float[] dBmArray, float[] ampsArray) {
        amplitude = amplitude / 100;
        if(amplitude <= 0.5){
            amplitude = 0.0f;
        } else if(amplitude > 0.5 && amplitude <= 0.6){
            amplitude = 0.2f;
        } else if(amplitude > 0.6 && amplitude <= 0.7){
            amplitude = 0.6f;
        } else if(amplitude > 0.7){
            amplitude = 1f;
        }
        try {
            dBmArray[0] = amplitude;
            ampsArray[0] = amplitude;
        } catch (Exception e){ }
    }

    /**
     * 停止渲染
     */
    public void stop() {
        try {
            calmDownAndStopRendering();
        } catch (Exception e){ }
    }

}