package com.rush.cloud.betslip.builder;

import java.awt.image.BufferedImage;

import com.rush.cloud.betslip.request.BetSlipImageGenerationRequest;

public class RoundRobinImageBuilder extends BetSlipImageBuilder {

    @Override
    BufferedImage buildHeader(BetSlipImageGenerationRequest request, int width, int height) {
        return buildHeaderSection(request, width, height);
    }

    @Override
    BufferedImage buildBody(BetSlipImageGenerationRequest request, int width, int height) {
        return buildBodyMultisAllInfo(width, height, request);
    }

    @Override
    BufferedImage buildFooter(BetSlipImageGenerationRequest request, int width, int height) {
        return buildFooter(width, height, request);
    }
}
