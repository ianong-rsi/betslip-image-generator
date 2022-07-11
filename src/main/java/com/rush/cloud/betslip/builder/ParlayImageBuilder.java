package com.rush.cloud.betslip.builder;

import java.awt.image.BufferedImage;

import javax.enterprise.context.ApplicationScoped;

import com.rush.cloud.betslip.request.BetSlipImageGenerationRequest;

@ApplicationScoped
public class ParlayImageBuilder extends BetSlipImageBuilder {

    @Override
    public BufferedImage buildHeader(BetSlipImageGenerationRequest request, int width, int height) {
        return buildHeaderSection(request, width, height);
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
