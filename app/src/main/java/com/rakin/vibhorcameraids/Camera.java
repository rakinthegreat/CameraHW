package com.rakin.vibhorcameraids;

import android.util.Size;
import android.util.SizeF;
import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Vibhor Srivastava on 28/09/2020
 */
public class Camera {
    private final String id;
    private final boolean isFront;
    private final float focalLength;
    private final float aperture;
    private final int[] aeModes;
    private final Size[] rawSizes;
    private final Size[] jpegSizes;
    private final Size[] yuvSizes;
    private final SizeF sensorSize;
    private final Double angleOfView;
    private final boolean flashSupported;
    private final String supportedHardwareLevel;
    private final Set<String> physicalIds;
    private String type = "";
    private String name = "";

    public Camera(String id, boolean isFront, float focalLength, float aperture, SizeF sensorSize, double angleOfView, int[] aeModes, boolean flashSupported, Size[] rawSizes, Size[] jpegSizes, Size[] yuvSizes, String supportedHardwareLevel, Set<String> physicalIds) {
        this.id = id;
        this.focalLength = focalLength;
        this.aperture = aperture;
        this.sensorSize = sensorSize;
        this.angleOfView = angleOfView;
        this.aeModes = aeModes;
        this.flashSupported = flashSupported;
        this.rawSizes = rawSizes;
        this.jpegSizes = jpegSizes;
        this.yuvSizes = yuvSizes;
        this.supportedHardwareLevel = supportedHardwareLevel;
        this.isFront = isFront;
        this.physicalIds = physicalIds;
        if (!physicalIds.isEmpty())
            type = "(Logical)";
    }

    public Set<String> getPhysicalIds() {
        return physicalIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setType("\u2713");
    }

    public String getId() {
        return id;
    }

    public boolean isFront() {
        return isFront;
    }

    public float getFocalLength() {
        return focalLength;
    }

    public float getAperture() {
        return aperture;
    }

    public int[] getAeModes() {
        return aeModes;
    }

    public boolean isFlashSupported() {
        return flashSupported;
    }

    public String getSupportedHardwareLevel() {
        return supportedHardwareLevel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Size[] getRawSizes() {
        return rawSizes;
    }

    public Size[] getJpegSizes() {
        return jpegSizes;
    }

    public Size[] getYUVSizes() {
        return yuvSizes;
    }

    public SizeF getSensorSize() {
        return sensorSize;
    }

    public double getAngleOfView() {
        return angleOfView;
    }

    public boolean isNameNotSet() {
        return name.equals("");
    }

    public boolean isTypeNotSet() {
        return type.equals("");
    }

    @NonNull
    public String toString() {
        return "" + type + (isFront ? "FRONT" : "BACK") + "  " + "ID" + '[' + id + "] " + name + (physicalIds.isEmpty() ? "" : " = ID" + physicalIds.toString().replace(", ", " + ")) +
                "\n\t\t\t" +
                "SupportedHardwareLevel = " + supportedHardwareLevel +
                "\n\t\t\t" +
                "FocalLength = " + focalLength +
                "\n\t\t\t" +
                "Aperture = " + aperture +
                "\n\t\t\t" +
                "SensorSize = " + sensorSize +
                "\n\t\t\t" +
                "AngleOfView(Diagonal) = " + Math.round(angleOfView) + "\u00b0" +
                "\n\t\t\t" +
                "AEModes = " + Arrays.toString(aeModes) +
                "\n\t\t\t" +
                "FlashSupported = " + flashSupported +
                "\n\t\t\t" +
                "RAW_SENSOR sizes = " + Arrays.toString(rawSizes) +
                "\n\t\t\t" +
                "\n_______________\n" +
                "YUV_420_888 sizes = " + Arrays.deepToString(yuvSizes) +
                "\n\t\t\t" +
                "\n_______________\n" +
                "JPEG sizes = " + Arrays.toString(jpegSizes) +
                "\n\n" +
                "\n\t\t\t" +
                "\n\t\t\t" +
                "\n===============\n" +
                "\n\t\t\t" +
                "\n\n"
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Camera camera = (Camera) o;
        return isFront() == camera.isFront() &&
                Float.compare(camera.getFocalLength(), getFocalLength()) == 0 &&
                Float.compare(camera.getAperture(), getAperture()) == 0 &&
                isFlashSupported() == camera.isFlashSupported() &&
                Arrays.equals(getAeModes(), camera.getAeModes()) &&
                getSensorSize().equals(camera.getSensorSize());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(isFront(), getFocalLength(), getAperture(), getSensorSize(), isFlashSupported());
        result = 31 * result + Arrays.hashCode(getAeModes());
        return result;
    }
}
