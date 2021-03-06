package com.rush.cloud.betslip.builder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectionLayoutConfiguration {

    // Font Sizes
    private float labelFontSize;
    private float oddsFontSize;
    private float oddsBoostedFontSize;
    private float marketTypeFontSize;
    private float eventNameFontSize;
    private float eventStartDateFontSize;

    // Lines
    private float dividerLineThickness;
    private float ovalLineThickness;
    private int bulletDiameter;

    // OddBoosted
    private int flashWidth;
    private int flashHeight;
    private int flashY;
    private int oddsBoostedBadgeWidth;
    private int oddsBoostedBadgeHeight;
    private int oddsBoostedBadgeY;
}
