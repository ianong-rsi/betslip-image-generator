package com.rush.cloud.betslip.builder;

import com.rush.cloud.betslip.common.PlayType;

public class BetTypeBuilderFactory {

    public BetSlipImageBuilder getBuilder(PlayType playType) {
        switch (playType) {
            case PARLAY:
                return new ParlayImageBuilder();
            case SGP:
            case MULTIPLE_SGP:
                return new SgpImageBuilder();
            case TRIXIE:
            case YANKEE:
            case CANADIAN:
            case HEINZ:
            case SUPER_HEINZ:
                return new RoundRobinImageBuilder();
            default:
                throw new RuntimeException("Invalid bet type");
        }
    }
}
