package com.rush.cloud.betslip.builder;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.rush.cloud.betslip.common.Badge;
import com.rush.cloud.betslip.common.Platform;
import com.rush.cloud.betslip.common.Position;
import com.rush.cloud.betslip.common.SystemColor;
import com.rush.cloud.betslip.common.SystemFont;
import com.rush.cloud.betslip.common.XAlignment;
import com.rush.cloud.betslip.common.YAlignment;
import com.rush.cloud.betslip.request.BetSlipImageGenerationRequest;

public abstract class BetSlipImageBuilder {
    private static final Integer DEFAULT_IMAGE_WIDTH = 1080;
    private static final Integer DEFAULT_IMAGE_HEIGHT = 1080;
    private static final Integer DEFAULT_LEFT_PADDING = 40;
    private static final Integer DEFAULT_RIGHT_PADDING = 40;
    private static final Integer DEFAULT_HEADER_LINE_THICKNESS = 3;
    private static final Integer MAX_ITEMS_BEFORE_SPLIT = 7;
    private static final int FLASH_WIDTH_SIZE = 30;
    private static final int FLASH_HEIGHT_SIZE = 42;
    private static final int ODDS_BOOST_BADGE_WIDTH_SIZE = 244;
    private static final int ODDS_BOOST_BADGE_HEIGHT_SIZE = 38;

    abstract BufferedImage buildHeader(BetSlipImageGenerationRequest request, int width, int height);
    abstract BufferedImage buildBody(BetSlipImageGenerationRequest request, int width, int height);
    abstract BufferedImage buildFooter(BetSlipImageGenerationRequest request, int width, int height);

    public BufferedImage buildImage(BetSlipImageGenerationRequest request) {

        BufferedImage header = buildHeader(request, getWidth(), getHeaderHeight());
        BufferedImage body = buildBody(request, getWidth(), getBodyHeight());
        BufferedImage footer = buildFooter(request, getWidth(), getFooterHeight());

        Color bgColor = Platform.SUGARHOUSE.equals(Platform.valueOf(request.getPlatform()))
                        ? SystemColor.SUGAR_HOUSE_BG
                        : SystemColor.BET_RIVERS_BG;

        return new BufferedImageBuilder(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB)
                .addImage(getClass().getResource("/images/soccer.png"), getWidth(), getHeight(), 0, 0, 50)
                .addOverlay(bgColor, AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.85f))
                .addSection(header, header.getWidth(), header.getHeight(), 0, 0)
                .addSection(body, body.getWidth(), body.getHeight(), 0, header.getHeight())
                .addSection(footer, footer.getWidth(), footer.getHeight(), 0, header.getHeight() + body.getHeight())
                .build();
    }

    int getWidth() {
        return DEFAULT_IMAGE_WIDTH;
    }

    int getHeight() {
        return DEFAULT_IMAGE_HEIGHT;
    }

    int getLeftPadding() {
        return DEFAULT_LEFT_PADDING;
    }

    int getRightPadding() {
        return DEFAULT_RIGHT_PADDING;
    }

    int getHeaderHeight() {
        return getHeight() / 5;
    }

    int getBodyHeight() {
        int height = getHeight();
        return height - (2 * (height / 5));
    }

    int getFooterHeight() {
        return getHeight() / 5;
    }

    BufferedImage buildHeaderSection(BetSlipImageGenerationRequest request, int width, int height) {

        int halfHeaderHeight = height / 2;
        BufferedImage logoSectionHeader = buildHeaderLogoSection(
                width, halfHeaderHeight, request.getPlatformEnum(), request.getHeader().getMyBetsText());

        BufferedImage titleSectionHeader = buildHeaderTitleSection(
                width, halfHeaderHeight, request.getHeader().getBetTitle(), request.getHeader().getTotalOdds());

        int linePosY = height - DEFAULT_HEADER_LINE_THICKNESS;
        return new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB)
                .addSection(logoSectionHeader, width, halfHeaderHeight, 0, 0)
                .addSection(titleSectionHeader, width, halfHeaderHeight, 0, halfHeaderHeight)
                .addLine(DEFAULT_HEADER_LINE_THICKNESS, SystemColor.LINE_COLOR, false, getLeftPadding(), linePosY, width - getRightPadding(), linePosY)
                .build();
    }

    BufferedImage buildHeaderSection(BetSlipImageGenerationRequest request, int width, int height,
                                     String badgeResource, int badgeWidth, int badgeHeight,
                                     String descriptionLeft, String descriptionRight) {


        int halfHeaderHeight = height / 2;
        BufferedImage logoSectionHeader = buildHeaderLogoSection(
                width, halfHeaderHeight, request.getPlatformEnum(), request.getHeader().getMyBetsText());

        BufferedImage titleSectionHeader = buildHeaderTitleSection(
                width, halfHeaderHeight, request.getHeader().getBetTitle(), request.getHeader().getTotalOdds(),
                badgeResource, badgeWidth, badgeHeight, descriptionLeft, descriptionRight);

        return new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB)
                .addSection(logoSectionHeader, width, halfHeaderHeight, 0, 0)
                .addSection(titleSectionHeader, width, halfHeaderHeight, 0, halfHeaderHeight)
                .build();
    }

    BufferedImage buildHeaderLogoSection(int width, int height, Platform platform, String myBetsText) {

        Font myBetsFont = SystemFont.TEKO_SEMI_BOLD.deriveFont(Font.ITALIC, 70f);
        Canvas canvas = new Canvas();
        FontMetrics fontMetrics = canvas.getFontMetrics(myBetsFont);

        int headerTagWidth = 25;
        int headerTagHeight = 84;

        int textWidth = fontMetrics.stringWidth(myBetsText);
        int myBetsWidth = textWidth+(headerTagWidth * 2);

        String headerTagResourcePath = "/images/headerTagNBorder.png";

        BufferedImage myBetsSection = new BufferedImageBuilder(myBetsWidth, headerTagHeight, BufferedImage.TYPE_INT_ARGB)
                .addImage(getClass().getResource(headerTagResourcePath), headerTagWidth, headerTagHeight, 0, 0)
                .addRectangle(SystemColor.BET_RIVERS_YELLOW, 0, 0, textWidth, headerTagHeight, headerTagWidth,0)
                .addText(myBetsText , XAlignment.CENTER, 0, YAlignment.CENTER, 0, myBetsFont, SystemColor.HEADER_MY_BETS_COLOR, -5,  5)
                .addImage(getClass().getResource(headerTagResourcePath), headerTagWidth, headerTagHeight, myBetsWidth - headerTagWidth, 0, 0, 180, false)
                .build();

        int titleLineThickness = 4;

        int lineX1 = 50;
        int lineY1 = height - titleLineThickness + 2;
        int lineX2 = width - getRightPadding();
        int lineY2 = height - titleLineThickness + 2;

        int logoWidth = 0;
        int logoHeight = 0;
        int logoPosX = 0;
        int logoPosY = 0;

        String logoResourcePath = null;

        BufferedImageBuilder logoSectionBuilder = new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB)
                .addSection(myBetsSection, myBetsSection.getWidth(), myBetsSection.getHeight(), 38, lineY2 - headerTagHeight + titleLineThickness);

        if (Platform.BETRIVERS.equals(platform)) {
            logoWidth = 540;
            logoHeight = 35;
            logoPosX = width - logoWidth - getRightPadding();
            logoPosY = 50;

            logoResourcePath = "/images/betriversLogo.png";

        } else if (Platform.SUGARHOUSE.equals(platform)) {

            logoWidth = 135;
            logoHeight = 69;
            logoPosX = width - logoWidth - getRightPadding();
            logoPosY = 40;

            lineX2 = logoPosX - 20;

            logoResourcePath = "/images/sugarhouseLogo.png";
        }

        Objects.requireNonNull(logoResourcePath);

        return logoSectionBuilder
                .addImage(getClass().getResource(logoResourcePath), logoWidth, logoHeight, logoPosX, logoPosY)
                .addLine(titleLineThickness, SystemColor.BET_RIVERS_YELLOW, false, lineX1, lineY1, lineX2, lineY2)
                .build();

    }

    BufferedImage buildHeaderTitleSection(int width, int height, String title, String totalOdds) {
        BufferedImageBuilder titleHeaderSectionBuilder = new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB);

        return titleHeaderSectionBuilder
                .addText(title, XAlignment.LEFT, 50, YAlignment.CENTER, 0, SystemFont.DM_SANS_BOLD.deriveFont(54f), Color.white)
                .addText(totalOdds, XAlignment.RIGHT, 60, YAlignment.CENTER, 0, SystemFont.DM_SANS_BOLD.deriveFont(54f), Color.white)
                .build();
    }

    private BufferedImage buildHeaderTitleSection(int width, int height, String title, String totalOdds,
                                          String badgeResource, int badgeWidth, int badgeHeight, String leftDescription, String rightDescription) {
        BufferedImageBuilder titleHeaderSectionBuilder = new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB);
        Canvas canvas = new Canvas();

        Font titleFont = SystemFont.DM_SANS_BOLD.deriveFont(54f);
        FontMetrics titleFontMetrics = canvas.getFontMetrics(titleFont);

        int textPadding = 20;
        int badgeYPosition = (titleFontMetrics.getHeight() - (badgeHeight / 2)) / 2;
        int linePosY = height - DEFAULT_HEADER_LINE_THICKNESS;

        return titleHeaderSectionBuilder
                .addImage(getClass().getResource(badgeResource), badgeWidth, badgeHeight, getLeftPadding(), badgeYPosition)
                .addText(title, XAlignment.LEFT, getLeftPadding() + badgeWidth + textPadding, YAlignment.TOP, 0, titleFont, Color.white)
                .addText(totalOdds, XAlignment.RIGHT, getLeftPadding(), YAlignment.TOP, 0, titleFont, Color.white)
                .addText(leftDescription, XAlignment.LEFT, getLeftPadding() + 20, YAlignment.BOTTOM, 5, SystemFont.DM_SANS_REGULAR.deriveFont(20f), Color.white)
                .addText(rightDescription, XAlignment.RIGHT, getRightPadding(), YAlignment.BOTTOM, 5, SystemFont.DM_SANS_REGULAR.deriveFont(20f), Color.white)
                .addLine(DEFAULT_HEADER_LINE_THICKNESS, SystemColor.LINE_COLOR, false, getLeftPadding(), linePosY, width - getRightPadding(), linePosY)
                .build();
    }

    BufferedImage buildHeaderTitleSection(int width, int height, String title, String totalOdds,
                                          String badgeResource, int badgeWidth, int badgeHeight, String lineDescription) {
        BufferedImageBuilder titleHeaderSectionBuilder = new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB);
        Canvas canvas = new Canvas();

        Font lineDescriptionFont = SystemFont.DM_SANS_REGULAR.deriveFont(20f);
        FontMetrics lineDescFontMetrics = canvas.getFontMetrics(lineDescriptionFont);

        int textWidth = lineDescFontMetrics.stringWidth(lineDescription);
        int textHeight = lineDescFontMetrics.getHeight();

        int lineY = height - (textHeight / 2);
        int textPadding = 20;

        int preTextLineX1 = getLeftPadding();
        int preTextLineX2 = getLeftPadding() + badgeWidth;
        int postTextLineX1 = preTextLineX2 + textWidth + (textPadding * 2);
        int postTextLineX2 = width - getRightPadding();

        Font titleFont = SystemFont.DM_SANS_BOLD.deriveFont(54f);
        FontMetrics titleFontMetrics = canvas.getFontMetrics(titleFont);

        int badgeYPosition = (titleFontMetrics.getHeight() - (badgeHeight / 2)) / 2;

        return titleHeaderSectionBuilder
                .addImage(getClass().getResource(badgeResource), badgeWidth, badgeHeight, getLeftPadding(), badgeYPosition)
                .addText(title, XAlignment.LEFT, getLeftPadding() + badgeWidth + textPadding, YAlignment.TOP, 0, titleFont, Color.white)
                .addText(totalOdds, XAlignment.RIGHT, getLeftPadding(), YAlignment.TOP, 0, titleFont, Color.white)
                .addLine(DEFAULT_HEADER_LINE_THICKNESS, SystemColor.LINE_COLOR, false, preTextLineX1, lineY, preTextLineX2, lineY)
                .addText(lineDescription, XAlignment.LEFT, getLeftPadding() + badgeWidth + textPadding, YAlignment.BOTTOM, 0, lineDescriptionFont, Color.white)
                .addLine(DEFAULT_HEADER_LINE_THICKNESS, SystemColor.LINE_COLOR, false, postTextLineX1, lineY, postTextLineX2, lineY)
                .build();
    }

    BufferedImage buildBodyMultisAllInfo(int width, int height, BetSlipImageGenerationRequest request) {
        List<BetSlipImageGenerationRequest.Selection> selections = request.getSelections();
        BufferedImageBuilder contentBuilder = new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB);

        int numOfColumns = selections.size() > MAX_ITEMS_BEFORE_SPLIT ? 2 : 1;
        int numOfRows = selections.size() > MAX_ITEMS_BEFORE_SPLIT ? (int) Math.ceil((double) selections.size() / 2) : selections.size();

        int bulletLeftPadding = numOfColumns == 1 ? 30 : 15;

        int bodyTopPadding = 20;
        int bodyBottomPadding = 20;
        int sectionWidth = (width - getLeftPadding() - getRightPadding()) / numOfColumns;
        int sectionHeight = (height - bodyTopPadding - bodyBottomPadding) / numOfRows;

        SelectionLayoutConfiguration configuration = getConfigurationForSelectionSize(selections.size());
        List<List<BetSlipImageGenerationRequest.Selection>> contentColumnItems = choppedListToParts(selections, numOfRows);

        int indexSelection = 0; // Count the selection to avoid IndexOutOfBoundsException when the elements size are odd
        for (int row = 0; row < numOfRows; row++) {
            for (int col = 0; col < numOfColumns && indexSelection < selections.size() ; col++) {

                ++indexSelection;

                BetSlipImageGenerationRequest.Selection content = contentColumnItems.get(col).get(row);

                int textLeftPadding = getLeftPadding() + bulletLeftPadding;
                int textRightPadding = numOfColumns > 1 ? getRightPadding() / 2 : 0;

                BufferedImageBuilder sectionBuilder = new BufferedImageBuilder(sectionWidth, sectionHeight, BufferedImage.TYPE_INT_ARGB);

                // If more than one rows in the column
                int bulletRadius = configuration.getBulletDiameter() / 2;
                if (contentColumnItems.get(col).size() > 1) {
                    int centerOfBullet = bulletLeftPadding + bulletRadius;
                    if (row == 0) {
                        // If first row, ONLY draw vertical line below bullet + divider line
                        sectionBuilder
                                .addLine(configuration.getOvalLineThickness(), SystemColor.LINE_COLOR, false, centerOfBullet, sectionHeight / 2, centerOfBullet, sectionHeight)
                                .addLine(configuration.getDividerLineThickness(), SystemColor.LINE_COLOR_ALPHA_70, false, textLeftPadding, sectionHeight - (int) configuration.getDividerLineThickness(), sectionWidth - textRightPadding, sectionHeight - (int) configuration.getDividerLineThickness());

                    } else if (row == contentColumnItems.get(col).size() - 1) {
                        // If last row, ONLY draw vertical line above bullet
                        sectionBuilder
                                .addLine(configuration.getOvalLineThickness(), SystemColor.LINE_COLOR, false, centerOfBullet, 0, centerOfBullet, sectionHeight / 2);

                    } else {
                        // Else middle row, draw both vertical lines above oval and below bullet + divider line
                        sectionBuilder
                                .addLine(configuration.getOvalLineThickness(), SystemColor.LINE_COLOR, false, centerOfBullet, sectionHeight / 2, centerOfBullet, sectionHeight)
                                .addLine(configuration.getOvalLineThickness(), SystemColor.LINE_COLOR, false, centerOfBullet, 0, centerOfBullet, sectionHeight / 2)
                                .addLine(configuration.getDividerLineThickness(), SystemColor.LINE_COLOR_ALPHA_70, false, textLeftPadding, sectionHeight - (int) configuration.getDividerLineThickness(), sectionWidth - textRightPadding, sectionHeight - (int) configuration.getDividerLineThickness());
                    }
                }

                // Draw bullet
                sectionBuilder.addBullet(configuration.getOvalLineThickness(), SystemColor.LINE_COLOR, SystemColor.FILL_COLOR, configuration.getBulletDiameter(), configuration.getBulletDiameter(), bulletLeftPadding, (sectionHeight / 2) - bulletRadius);

                int topLineTextHeight = getTextHeight(SystemFont.DM_SANS_BOLD.deriveFont(configuration.getLabelFontSize()));
                int midLineTextHeight = getTextHeight(SystemFont.DM_SANS_REGULAR.deriveFont(configuration.getMarketTypeFontSize()));
                int bottomLineTextHeight = getTextHeight(SystemFont.DM_SANS_REGULAR.deriveFont(configuration.getEventNameFontSize()));

                int topPadding = (sectionHeight / 2) - topLineTextHeight - (midLineTextHeight / 4);
                int bottomPadding = (sectionHeight / 2) - bottomLineTextHeight - (midLineTextHeight / 2);

                // Add texts
                sectionBuilder
                        // Top Line
                        .addText(content.getLabel(), XAlignment.LEFT, textLeftPadding, YAlignment.TOP, topPadding, SystemFont.DM_SANS_BOLD.deriveFont(configuration.getLabelFontSize()), SystemColor.BET_RIVERS_YELLOW)
                        // Middle Line
                        .addText(content.getBettingMarketType(), XAlignment.LEFT, textLeftPadding, YAlignment.CENTER, 0, SystemFont.DM_SANS_REGULAR.deriveFont(configuration.getMarketTypeFontSize()), Color.white)
                        // Line 3
                        .addText(content.getEventName(), XAlignment.LEFT, textLeftPadding, YAlignment.BOTTOM, bottomPadding, SystemFont.DM_SANS_REGULAR.deriveFont(configuration.getEventNameFontSize()), SystemColor.BET_RIVERS_WHITE_ALPHA_70)
                        .addText(content.getEventStartDate(), XAlignment.RIGHT, textRightPadding, YAlignment.BOTTOM, bottomPadding, SystemFont.DM_SANS_REGULAR.deriveFont(configuration.getEventStartDateFontSize()), SystemColor.BET_RIVERS_WHITE_ALPHA_70);

                if (request.getBadgesEnum() != null && request.getBadgesEnum().contains(Badge.ODDS_BOOST) && content.getOddsBoosted() != null) {

                    int oddsBoostedWidth = getTextWidth(content.getOddsBoosted(), SystemFont.DM_SANS_BOLD.deriveFont(configuration.getOddsFontSize()));
                    int oddsWidth = getTextWidth(content.getOdds(), SystemFont.DM_SANS_BOLD.deriveFont(configuration.getOddsFontSize()));
                    int oddsHeight = getTextHeight(SystemFont.DM_SANS_BOLD.deriveFont(configuration.getOddsFontSize()));
                    int oddsAscent = getTextAscent(SystemFont.DM_SANS_BOLD.deriveFont(configuration.getOddsFontSize()));

                    int flashWidth = configuration.getFlashWidth();
                    int flashHeight = configuration.getFlashHeight();
                    int flashY = configuration.getFlashY();

                    int oddsXPadding = textRightPadding + oddsBoostedWidth + (flashWidth * 2);
                    int oddsX = sectionWidth - oddsWidth - oddsXPadding - (oddsHeight / 10);
                    int oddsBoostedX = sectionWidth - oddsBoostedWidth - textRightPadding;

                    int flashX =  ((oddsBoostedX - (oddsX+ oddsWidth)) / 2 - (flashWidth / 2)) + (oddsX+ oddsWidth) ;

                    sectionBuilder.addText(content.getOdds(), XAlignment.RIGHT, oddsXPadding, YAlignment.TOP, topPadding, SystemFont.DM_SANS_BOLD.deriveFont(configuration.getOddsFontSize()), Color.white);
                    sectionBuilder.addLine(DEFAULT_HEADER_LINE_THICKNESS, SystemColor.BET_RIVERS_YELLOW, false, oddsX  , topPadding + (oddsHeight/3) , sectionWidth - oddsXPadding , oddsAscent + topPadding - (oddsHeight / 10) );
                    sectionBuilder.addText(content.getOddsBoosted(), XAlignment.RIGHT, textRightPadding, YAlignment.TOP, topPadding, SystemFont.DM_SANS_BOLD.deriveFont(configuration.getOddsFontSize()), Color.white);

                    sectionBuilder.addImage(
                            getClass().getResource("/images/flash.png"),
                            flashWidth,
                            flashHeight,
                            flashX + (flashWidth / 5),
                            flashY,
                            0,
                            0,
                            true
                    );

                    int oddsBoostedBadgeHeight = configuration.getOddsBoostedBadgeHeight();
                    int oddsBoostedBadgeWidth = configuration.getOddsBoostedBadgeWidth();

                    sectionBuilder.addImage(
                            getClass().getResource("/images/badges/badge-oddsboost.png"),
                            oddsBoostedBadgeWidth,
                            oddsBoostedBadgeHeight,
                            (oddsBoostedX + oddsBoostedWidth) - oddsBoostedBadgeWidth,
                            configuration.getOddsBoostedBadgeY(),
                            0,
                            0,
                            true
                    );
                } else {
                    sectionBuilder.addText(content.getOdds(), XAlignment.RIGHT, textRightPadding, YAlignment.TOP, topPadding, SystemFont.DM_SANS_BOLD.deriveFont(configuration.getOddsFontSize()), Color.white);
                }
                // Add selection to content
                contentBuilder
                        .addSection(sectionBuilder.build(), sectionWidth, sectionHeight, (sectionWidth * col) + getLeftPadding() , (sectionHeight * row) + bodyTopPadding);
            }
        }

        return contentBuilder.build();
    }

    BufferedImage buildFooter(int width, int height, BetSlipImageGenerationRequest request) {
        BetSlipImageGenerationRequest.FooterContent footer = request.getFooter();

        BufferedImageBuilder totalWager = new BufferedImageBuilder(width / 5, height , BufferedImage.TYPE_INT_ARGB)
                .addText(footer.getTotalWagerText(), XAlignment.LEFT, 57, YAlignment.TOP, 51, SystemFont.DM_SANS_BOLD.deriveFont(25f), SystemColor.BET_RIVERS_YELLOW_ALPHA_70)
                .addText(footer.getTotalWagerAmount(), XAlignment.RIGHT, 16, YAlignment.TOP, 86, SystemFont.DM_SANS_BOLD.deriveFont(32f), SystemColor.BET_RIVERS_YELLOW);

        if (request.getBadgesEnum() != null && request.getBadgesEnum().contains(Badge.FREE_BET)) {
            totalWager.addImage(getClass().getResource("/images/badges/badge-freebet.png"), 196, 79, (width / 5) - 188, 110);
        }

        BufferedImage totalWagerSection = totalWager.build();

        BufferedImageBuilder potentialPayout =  new BufferedImageBuilder(width, height, BufferedImage.TYPE_INT_ARGB)
                .addOverlay(SystemColor.FILL_FOOTER_COLOR, null, 50, Position.BOTTOM)
                .addSection(totalWagerSection,width / 5, height, 0,0)
                .addText(footer.getPotentialPayoutText(), XAlignment.LEFT, 267, YAlignment.TOP, 37, SystemFont.DM_SANS_MEDIUM.deriveFont(41f), SystemColor.BET_RIVERS_WHITE_ALPHA_70)
                .addText(footer.getPotentialPayoutAmount(), XAlignment.LEFT, 267, YAlignment.TOP, 76, SystemFont.DM_SANS_BOLD.deriveFont(60f), Color.white)
                .addText(footer.getGamblingProblemLine1(), XAlignment.RIGHT, 58, YAlignment.TOP, 52, SystemFont.DM_SANS_MEDIUM.deriveFont(21f), Color.white)
                .addText(footer.getGamblingProblemLine2(), XAlignment.RIGHT, 58, YAlignment.TOP, 79, SystemFont.DM_SANS_MEDIUM.deriveFont(21f), Color.white)
                .addText(footer.getBetDateTime(), XAlignment.RIGHT, 58, YAlignment.TOP, 126, SystemFont.DM_SANS_MEDIUM.deriveFont(21f), SystemColor.BET_RIVERS_WHITE_ALPHA_70);

        if (request.getBadgesEnum() != null && request.getBadgesEnum().contains(Badge.PROFIT_BOOST)) {
            potentialPayout.addImage(getClass().getResource("/images/badges/badge-profitboost.png"), 314, 97, 235, 118);
        }

        return potentialPayout.build();
    }

    private <T> List<List<T>> choppedListToParts(List<T> list, final int numOfParts) {

        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += numOfParts) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(N, i + numOfParts))));
        }
        return parts;
    }

    private SelectionLayoutConfiguration getConfigurationForSelectionSize(int numOfRows) {

        switch (numOfRows) {
            case 1:
                //TODO
            case 2:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(50)
                        .oddsFontSize(50)
                        .marketTypeFontSize(26)
                        .eventNameFontSize(25)
                        .eventStartDateFontSize(25)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.9))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.9))
                        .flashY(91)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.9))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.9))
                        .oddsBoostedBadgeY(135)
                        .build();
            case 3:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(50)
                        .oddsFontSize(50)
                        .marketTypeFontSize(26)
                        .eventNameFontSize(25)
                        .eventStartDateFontSize(25)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.9))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.9))
                        .flashY(40)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.8))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.8))
                        .oddsBoostedBadgeY(85)
                        .build();
            case 4:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(40)
                        .oddsFontSize(40)
                        .marketTypeFontSize(26)
                        .eventNameFontSize(25)
                        .eventStartDateFontSize(25)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.6))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.6))
                        .flashY(30)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.7))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.7))
                        .oddsBoostedBadgeY(65)
                        .build();
            case 5:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(30)
                        .oddsFontSize(30)
                        .marketTypeFontSize(24)
                        .eventNameFontSize(23)
                        .eventStartDateFontSize(23)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.5))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.5))
                        .flashY(21)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.7))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.7))
                        .oddsBoostedBadgeY(50)
                        .build();
            case 6:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(25)
                        .oddsFontSize(25)
                        .marketTypeFontSize(18)
                        .eventNameFontSize(16)
                        .eventStartDateFontSize(16)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.45))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.45))
                        .flashY(18)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.55))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.55))
                        .oddsBoostedBadgeY(42)
                        .build();
            case 7:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(23)
                        .oddsFontSize(23)
                        .marketTypeFontSize(18)
                        .eventNameFontSize(16)
                        .eventStartDateFontSize(16)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.45))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.45))
                        .flashY(10)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.5))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.5))
                        .oddsBoostedBadgeY(35)
                        .build();
            case 8:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(23)
                        .oddsFontSize(23)
                        .marketTypeFontSize(18)
                        .eventNameFontSize(16)
                        .eventStartDateFontSize(16)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.45))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.45))
                        .flashY(45)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.5))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.5))
                        .oddsBoostedBadgeY(67)
                        .build();
            case 9:
            case 10:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(23)
                        .oddsFontSize(23)
                        .marketTypeFontSize(18)
                        .eventNameFontSize(16)
                        .eventStartDateFontSize(16)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.45))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.45))
                        .flashY(28)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.6))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.6))
                        .oddsBoostedBadgeY(50)
                        .build();

            case 11:
            case 12:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(23)
                        .oddsFontSize(23)
                        .marketTypeFontSize(18)
                        .eventNameFontSize(16)
                        .eventStartDateFontSize(16)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(3)
                        .bulletDiameter(20)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.45))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.45))
                        .flashY(18)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.58))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.58))
                        .oddsBoostedBadgeY(40)
                        .build();
            case 13:
            case 14:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(23)
                        .oddsFontSize(23)
                        .marketTypeFontSize(18)
                        .eventNameFontSize(16)
                        .eventStartDateFontSize(16)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(2)
                        .bulletDiameter(13)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.45))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.45))
                        .flashY(11)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.54))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.54))
                        .oddsBoostedBadgeY(34)
                        .build();
            case 15:
            case 16:
                return SelectionLayoutConfiguration.builder()
                        // Font sizes
                        .labelFontSize(20)
                        .oddsFontSize(20)
                        .marketTypeFontSize(16)
                        .eventNameFontSize(14)
                        .eventStartDateFontSize(14)
                        // Lines
                        .dividerLineThickness(1)
                        .ovalLineThickness(2)
                        .bulletDiameter(10)
                        // OddsBoosted
                        .flashWidth((int) (FLASH_WIDTH_SIZE * 0.35))
                        .flashHeight((int) (FLASH_HEIGHT_SIZE * 0.35))
                        .flashY(13)
                        .oddsBoostedBadgeWidth((int) (ODDS_BOOST_BADGE_WIDTH_SIZE * 0.45))
                        .oddsBoostedBadgeHeight((int) (ODDS_BOOST_BADGE_HEIGHT_SIZE * 0.45))
                        .oddsBoostedBadgeY(30)
                        .build();
            default:
                throw new RuntimeException("Invalid selections size");
        }
    }

    public int getTextWidth(String text, Font font){
        Canvas canvas = new Canvas();
        FontMetrics fontMetrics = canvas.getFontMetrics(font);
        return fontMetrics.stringWidth(text);
    }

    public int getTextHeight(Font font){
        Canvas canvas = new Canvas();
        FontMetrics fontMetrics = canvas.getFontMetrics(font);
        return fontMetrics.getHeight();
    }

    public int getTextAscent(Font font){
        Canvas canvas = new Canvas();
        FontMetrics fontMetrics = canvas.getFontMetrics(font);
        return fontMetrics.getAscent();
    }

}
