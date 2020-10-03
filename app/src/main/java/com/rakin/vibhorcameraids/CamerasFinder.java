package com.rakin.vibhorcameraids;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Size;
import android.util.SizeF;

import java.util.*;

/**
 * Created by Vibhor on 23/09/2020
 * Modified by Rakin on 27/09/2020
 */
public class CamerasFinder {
    private final Map<String, Camera> map = new LinkedHashMap<>();
    private final CameraManager mCameraManager;
    private String mFileName;

    public CamerasFinder(CameraManager mCameraManager) {
        this.mCameraManager = mCameraManager;
    }

    private void scanAllCameras(CameraManager cameraManager) {
        for (int id = 0; id < 512; id++) {
            CameraCharacteristics cameraCharacteristics;
            try {
                cameraCharacteristics = cameraManager.getCameraCharacteristics(String.valueOf(id));
                float[] focalLength = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                float[] aperture = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                if (focalLength != null && aperture != null) {
                    Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    Camera camera = new Camera(
                            String.valueOf(id),
                            facing == 0,
                            focalLength[0],
                            aperture[0],
                            cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE),
                            calculateAngleOfView(cameraCharacteristics),
                            cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
                            cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE),
                            getRawSizes(cameraCharacteristics),
                            getJpegSizes(cameraCharacteristics),
                            getYUVSizes(cameraCharacteristics),
                            getSupportedHWlevel(cameraCharacteristics),
                            cameraCharacteristics.getPhysicalCameraIds()
                    );
                    if (camera.isTypeNotSet() && map.containsValue(camera)) {
                        camera.setType("(Repeat)");
                    }
                    map.put(String.valueOf(id), camera);
                }
            } catch (IllegalArgumentException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateMap();
    }

    private void updateMap() {
//        TreeSet<Float> backAperturesSorted = new TreeSet<>();
//        TreeSet<Float> frontAperturesSorted = new TreeSet<>();
        TreeSet<Double> frontAnglesOfViewSorted = new TreeSet<>();
        TreeSet<Double> backAnglesOfViewSorted = new TreeSet<>();
//        Comparator<SizeF> sizeFComparator = (o1, o2) -> Float.compare(o1.getWidth() * o1.getHeight(), o2.getWidth() * o2.getHeight());
//        TreeSet<SizeF> backSensorSizeSorted = new TreeSet<>(sizeFComparator);
//        TreeSet<SizeF> frontSensorSizeSorted = new TreeSet<>(sizeFComparator);

        //Filling tree sets for later use/comparisons
        for (Camera currentCamera : map.values()) {
            if (currentCamera.isTypeNotSet())
                if (currentCamera.isFront()) {
                    frontAnglesOfViewSorted.add(currentCamera.getAngleOfView());
//                    frontAperturesSorted.add(currentCamera.getAperture());
//                    frontSensorSizeSorted.add(currentCamera.getSensorSize());
                } else {
                    backAnglesOfViewSorted.add(currentCamera.getAngleOfView());
//                    backAperturesSorted.add(currentCamera.getAperture());
//                    backSensorSizeSorted.add(currentCamera.getSensorSize());
                }
        }

        //Stores the Value of Main Back and Main Front (initialise with ID 0 and 1 respectively)
        Camera mainBackCam = map.get("0");
        Camera mainFrontCam = map.get("1");

        //Finding Main Back camera and updating the map
        for (Map.Entry<String, Camera> cameraEntry : map.entrySet()) {
            Camera currentCam = cameraEntry.getValue();
            if (currentCam.isNameNotSet() && currentCam.isTypeNotSet() && currentCam.getAeModes() != null) {
                if (!currentCam.isFront()) {
                    currentCam.setName("(Main)");
                    cameraEntry.setValue(currentCam);
                    mainBackCam = currentCam;
                    break;
                }
            }
        }
        //Finding Main Front camera and updating the map
        for (Map.Entry<String, Camera> cameraEntry : map.entrySet()) {
            Camera currentCam = cameraEntry.getValue();
            if (currentCam.isNameNotSet() && currentCam.isTypeNotSet() && currentCam.getAeModes() != null) {
                if (currentCam.isFront()) {
                    currentCam.setName("(Main)");
                    cameraEntry.setValue(currentCam);
                    mainFrontCam = currentCam;
                    break;
                }
            }
        }

        //Naming the Cameras
        for (Map.Entry<String, Camera> cameraEntry : map.entrySet()) {
            Camera currentCam = cameraEntry.getValue();
            if (mainBackCam != null && mainFrontCam != null) {
                if (currentCam.isTypeNotSet() && currentCam.isNameNotSet()) {
                    if (currentCam.getAeModes() == null) {
                        currentCam.setName("(Other)"); //Sensors such as ToF sensors
                        cameraEntry.setValue(currentCam);
                    } else if (currentCam.getAeModes().length > 2) {
                        if (!currentCam.isFront()) {
                            nameCameras(cameraEntry, mainBackCam, backAnglesOfViewSorted);
                        } else {
                            nameCameras(cameraEntry, mainFrontCam, frontAnglesOfViewSorted);
                        }
                    } else if (currentCam.getAeModes().length <= 2) {
                        if (currentCam.isFront() && currentCam.getSensorSize().getWidth() > mainFrontCam.getSensorSize().getWidth() && currentCam.getAngleOfView() > mainFrontCam.getAngleOfView()) {
                            currentCam.setName("(Wide)"); //Added this logic keeping in mind Samsung S20
                        } else {
                            currentCam.setName("(Depth/Portrait)");
                        }
                        cameraEntry.setValue(currentCam);
                    }
                }
            }
        }
    }

    //Set Names for Wide/Macro/Tele cameras
    private void nameCameras(Map.Entry<String, Camera> cameraEntry, Camera mainCam, TreeSet<Double> sortedListOfAngles) {
        Camera currentCam = cameraEntry.getValue();
        if (currentCam.getAngleOfView() > mainCam.getAngleOfView()) {
            if (currentCam.getAngleOfView() == sortedListOfAngles.last()) { //largest angle of view
                currentCam.setName("(Wide)");
            } else {
                currentCam.setName("(Macro)"); //TODO improve logic
            }
            cameraEntry.setValue(currentCam);
        } else if (currentCam.getAngleOfView() < mainCam.getAngleOfView()) {
            currentCam.setName("(Tele)"); //angle of view less than Main Camera
            cameraEntry.setValue(currentCam);
        }
    }


    private Double calculateAngleOfView(CameraCharacteristics cc) {
        float focalLength = cc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
        double sensorDiagonal = Math.sqrt(Math.pow(cc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth(), 2)
                + Math.pow(cc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight(), 2)
        );
        return Math.toDegrees(2 * Math.atan(sensorDiagonal / (2 * focalLength)));
    }

    private Size[] getRawSizes(CameraCharacteristics cameraCharacteristics) {
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        return streamConfigurationMap.getOutputSizes(ImageFormat.RAW_SENSOR);

    }

    private Size[] getYUVSizes(CameraCharacteristics cameraCharacteristics) {
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        return streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888);

    }

    private Size[] getJpegSizes(CameraCharacteristics cameraCharacteristics) {
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        return streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);

    }

    private String getSupportedHWlevel(CameraCharacteristics cameraCharacteristics) {
        return hwLevelName(cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));
    }

    private String hwLevelName(int level) {
        return level == 0 ? "LIMITED COMPATIBILITY"
                : level == 1 ? "FULL\n\t\t\t (GCAM COMPATIBLE)"
                : level == 2 ? "LEGACY \n\t\t\t (GCAM INCOMPATIBLE)"
                : level == 3 ? "LEVEL3 \n\t\t\t (BEST GCAM COMPATIBILITY)"
                : level == 4 ? "EXTERNAL"
                : "";
    }

    /**
     * @return filename based on the Device's brand and model name
     */
    public String getFileName() {
        return mFileName;
    }

    private void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    /**
     * Scans all IDs and returns usable string of data
     *
     * @return the formatted displayable string of data
     */
    public String getResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Build.BRAND).append(", ").append(Build.MODEL).append(", ").append(Build.MANUFACTURER).append(", ").append(Build.DEVICE);
        setFileName("CameraHW-".concat(sb.toString().replace(", ", "-")));
        sb.append("\n\n");
        sb.append("Android ").append(Build.VERSION.RELEASE).append(" - ").append(System.getProperty("os.version"));
        sb.append("\n");

        scanAllCameras(mCameraManager);
        try {
            sb.append("\n===============\n");
            sb.append("\nCamera IDs visible to Normal Apps = ");
            sb.append(Arrays.toString(mCameraManager.getCameraIdList()));
            sb.append("\n\n===============\n");
            sb.append("All Cameras IDs = ").append(map.keySet()).append("\n");
            sb.append("\n");
            for (Camera camera : map.values())
                sb.append(camera);
            sb.append("===============\n");

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}

