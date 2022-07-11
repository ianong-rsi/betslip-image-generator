package com.rush.cloud.betslip.builder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.rush.cloud.betslip.common.PlayType;

@ApplicationScoped
public class BetTypeBuilderFactory {

    private final ParlayImageBuilder parlayImageBuilder;
    private final SgpImageBuilder sgpImageBuilder;
    private final RoundRobinImageBuilder roundRobinImageBuilder;

    @Inject
    public BetTypeBuilderFactory(
            ParlayImageBuilder parlayImageBuilder,
            SgpImageBuilder sgpImageBuilder,
            RoundRobinImageBuilder roundRobinImageBuilder) {

        this.parlayImageBuilder = parlayImageBuilder;
        this.sgpImageBuilder = sgpImageBuilder;
        this.roundRobinImageBuilder = roundRobinImageBuilder;
    }

    public BetSlipImageBuilder getBuilder(PlayType playType) {
        switch (playType) {
            case PARLAY:
                return parlayImageBuilder;
            case SGP:
            case MULTIPLE_SGP:
                return sgpImageBuilder;
            case TRIXIE:
            case YANKEE:
            case CANADIAN:
            case HEINZ:
            case SUPER_HEINZ:
                return roundRobinImageBuilder;
            default:
                throw new RuntimeException("Invalid bet type");
        }
    }
}
