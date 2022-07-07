package com.rush.cloud.betslip.builder;

import java.awt.image.BufferedImage;

import com.rush.cloud.betslip.request.BetSlipImageGenerationRequest;

public class SgpImageBuilder extends BetSlipImageBuilder {

    @Override
    BufferedImage buildHeader(BetSlipImageGenerationRequest request, int width, int height) {
        return buildHeaderSection(request, width, height,
                                  "/images/badges/badge-sgp.png", 75, 30,
                                  request.getHeader().getDescriptionLeft(), request.getHeader().getDescriptionRight());
    }

    @Override
    public BufferedImage buildBody(BetSlipImageGenerationRequest request, int width, int height) {
        return buildBodyMultisAllInfo(width, height, request);
    }

    @Override
    public BufferedImage buildFooter(BetSlipImageGenerationRequest request, int width, int height) {
        return buildFooter(width, height, request);
    }
}
