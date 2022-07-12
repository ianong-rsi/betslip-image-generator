package com.rush.cloud.betslip.request;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.rush.cloud.betslip.common.Badge;
import com.rush.cloud.betslip.common.Platform;
import com.rush.cloud.betslip.common.PlayType;
import com.rush.cloud.betslip.request.validation.ValidEnum;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class BetSlipImageGenerationRequest {

    @NotNull
    @ValidEnum(enumClass = Platform.class)
    private String platform;
    @NotNull
    @ValidEnum(enumClass = PlayType.class)
    private String playType;
    private List<@ValidEnum(enumClass = Badge.class) String> badges;
    @NotNull
    @Valid
    private HeaderContent header;
    @NotEmpty
    private List<@Valid Selection> selections;
    @NotNull
    @Valid
    private FooterContent footer;

    @Data
    @RegisterForReflection
    public static class Selection {
        @NotEmpty
        private String label;
        @NotEmpty
        private String odds;
        private String oddsBoosted;
        @NotEmpty
        private String bettingMarketType;
        private String eventName;
        private String eventStartDate;
    }

    @Data
    @RegisterForReflection
    public static class HeaderContent {
        @NotEmpty
        private String myBetsText;
        private String betTitle;
        private String totalOdds;
        private String descriptionLeft;
        private String descriptionRight;
        private String lineDescription;
    }

    @Data
    @RegisterForReflection
    public static class FooterContent {
        @NotEmpty
        private String totalWagerText;
        @NotEmpty
        private String totalWagerAmount;
        @NotEmpty
        private String potentialPayoutText;
        @NotEmpty
        private String potentialPayoutAmount;
        private String potentialPayoutAmountBoosted;
        @NotEmpty
        private String gamblingProblemLine1;
        @NotEmpty
        private String gamblingProblemLine2;
        @NotEmpty
        private String betDateTime;
    }

    public PlayType getPlayTypeEnum() {
        return PlayType.valueOf(this.playType);
    }

    public Platform getPlatformEnum() {
        return Platform.valueOf(this.platform);
    }

    public List<Badge> getBadgesEnum() {
        return Optional.ofNullable(this.badges)
                .map(badges -> badges.stream()
                        .map(Badge::valueOf)
                        .collect(Collectors.toList()))
                .orElse(null);
    }
}