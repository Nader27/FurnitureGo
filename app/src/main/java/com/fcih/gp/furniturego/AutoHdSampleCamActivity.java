package com.fcih.gp.furniturego;

import com.wikitude.common.camera.CameraSettings;

public class AutoHdSampleCamActivity extends SampleCamActivity {

    @Override
    public CameraSettings.CameraResolution getCameraResolution() {
        return CameraSettings.CameraResolution.AUTO;
    }
}
