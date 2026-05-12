package dev.vrcposter.renderer;

import dev.vrcposter.util.ImageIngester;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public final class PosterRenderer {

    private static final Color BACKGROUND_FALLBACK = new Color(0x1a, 0x1a, 0x2e);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final String FONT_NAME = "Ethnocentric";

    private PosterRenderer() {}

    /** Renders at the canonical 1080×1920 export resolution. */
    public static BufferedImage render(PosterSnapshot snapshot) {
        return render(snapshot, 1080, 1920);
    }

    /** Pure function: PosterSnapshot → BufferedImage at the given dimensions (ARCH-12). */
    public static BufferedImage render(PosterSnapshot snapshot, int width, int height) {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Dark background fill (always drawn first; image overlaid on top)
            g.setColor(BACKGROUND_FALLBACK);
            g.fillRect(0, 0, width, height);

            drawBackground(g, snapshot, width, height);

            int padding = (int)(width * 0.03);
            int topZoneEnd = (int)(height * 0.22);
            int bottomZoneStart = (int)(height * 0.75);

            drawTitleZone(g, snapshot, width, height, padding, topZoneEnd);
            drawLineupZone(g, snapshot, width, height, padding, topZoneEnd, bottomZoneStart);
            drawBottomZone(g, snapshot, width, height, padding);

        } finally {
            g.dispose();
        }
        return canvas;
    }

    // ── Layout regions ────────────────────────────────────────────────────

    private static void drawBackground(Graphics2D g, PosterSnapshot snapshot, int width, int height) {
        if (snapshot.backgroundPath == null) return;
        // Load at natural resolution (ImageIngester skips scaling when bounds exceed image dims),
        // then apply cover-scale ourselves so the background fills the full canvas with no letterboxing.
        Optional<BufferedImage> opt = ImageIngester.load(snapshot.backgroundPath, Integer.MAX_VALUE, Integer.MAX_VALUE);
        opt.ifPresent(img -> {
            double scaleX = (double) width  / img.getWidth();
            double scaleY = (double) height / img.getHeight();
            double scale  = Math.max(scaleX, scaleY);
            int scaledW = (int) Math.round(img.getWidth()  * scale);
            int scaledH = (int) Math.round(img.getHeight() * scale);
            int x = (width  - scaledW) / 2;
            int y = (height - scaledH) / 2;
            g.drawImage(img, x, y, scaledW, scaledH, null);
        });
    }

    private static void drawTitleZone(Graphics2D g, PosterSnapshot snapshot, int width, int height,
                                      int padding, int topZoneEnd) {
        g.setColor(TEXT_WHITE);

        int titleSize    = (int)(width * 0.07);
        int subtitleSize = (int)(width * 0.04);
        int lineSpacing  = (int)(height * 0.015);

        // Start a quarter into the top zone to leave breathing room at the very top
        int y = (int)(topZoneEnd * 0.20) + padding;

        // Title
        String title = snapshot.title != null ? snapshot.title : "";
        if (!title.isBlank()) {
            Font f = new Font(FONT_NAME, Font.BOLD, titleSize);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            y += fm.getAscent();
            g.drawString(title, (width - fm.stringWidth(title)) / 2, y);
            y += fm.getDescent() + lineSpacing;
        }

        // Date (rendered between title and subtitle)
        String date = snapshot.date != null ? snapshot.date : "";
        if (!date.isBlank()) {
            Font f = new Font(FONT_NAME, Font.PLAIN, subtitleSize);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            y += fm.getAscent();
            g.drawString(date, (width - fm.stringWidth(date)) / 2, y);
            y += fm.getDescent() + lineSpacing;
        }

        // Subtitle
        String subtitle = snapshot.subtitle != null ? snapshot.subtitle : "";
        if (!subtitle.isBlank()) {
            Font f = new Font(FONT_NAME, Font.BOLD, subtitleSize);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            y += fm.getAscent();
            g.drawString(subtitle, (width - fm.stringWidth(subtitle)) / 2, y);
        }
    }

    private static void drawLineupZone(Graphics2D g, PosterSnapshot snapshot, int width, int height,
                                       int padding, int topZoneEnd, int bottomZoneStart) {
        List<PosterSnapshot.TimeslotSnapshot> timeslots = snapshot.timeslots;
        if (timeslots == null || timeslots.isEmpty()) return;

        int centerZoneHeight = bottomZoneStart - topZoneEnd;
        int rowHeight = Math.min(centerZoneHeight / timeslots.size(), (int)(height * 0.12));
        // Center the lineup vertically in the center zone
        int startY = topZoneEnd + (centerZoneHeight - rowHeight * timeslots.size()) / 2;

        int fontSize = Math.max((int)(rowHeight * 0.35 / 2), 10);
        Font rowFont = new Font(FONT_NAME, Font.BOLD, fontSize);

        for (int i = 0; i < timeslots.size(); i++) {
            drawTimeslotRow(g, timeslots.get(i), startY + i * rowHeight, rowHeight, width, padding, rowFont);
        }
    }

    private static void drawTimeslotRow(Graphics2D g, PosterSnapshot.TimeslotSnapshot slot,
                                        int rowY, int rowHeight, int width, int padding, Font font) {
        g.setColor(TEXT_WHITE);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = rowHeight / 2;

        // Line 1: artist name — left-aligned
        String artistName = slot.assignedDjArtistName() != null ? slot.assignedDjArtistName() : "";
        if (!artistName.isBlank()) {
            g.drawString(artistName, padding, rowY + fm.getAscent());
        }

        // Line 2: "time – genre" — left-aligned
        String time = slot.startTime() != null ? slot.startTime() : "";
        String genre = slot.genre() != null ? slot.genre() : "";
        String line2 = genre.isBlank() ? time : (time + " – " + genre);
        if (!line2.isBlank()) {
            g.drawString(line2, padding, rowY + lineHeight + fm.getAscent());
        }

        // DJ logo — right-aligned, vertically centered across full row height
        if (slot.assignedDjLogoPath() != null) {
            int logoSize = rowHeight - (padding / 2);
            Optional<BufferedImage> opt = ImageIngester.load(slot.assignedDjLogoPath(), logoSize, logoSize);
            opt.ifPresent(logo -> {
                int logoX = width - logoSize - padding;
                int logoY = rowY + (rowHeight - logo.getHeight()) / 2;
                g.drawImage(logo, logoX, logoY, null);
            });
        }
    }

    private static void drawBottomZone(Graphics2D g, PosterSnapshot snapshot, int width, int height,
                                       int padding) {
        int logoSize = (int)(width * 0.18);
        int footerTextSize = (int)(width * 0.025);

        // Fixed text (FR16) — centered, roughly 91% down the canvas
        g.setColor(TEXT_WHITE);
        Font footerFont = new Font(Font.SANS_SERIF, Font.PLAIN, footerTextSize);
        g.setFont(footerFont);
        FontMetrics fm = g.getFontMetrics();
        String fixedText = "Please use an optimized Avatar";
        int fixedX = (width - fm.stringWidth(fixedText)) / 2;
        int fixedY = (int)(height * 0.91) + fm.getAscent();
        g.drawString(fixedText, fixedX, fixedY);

        // Club logo — bottom-left
        if (snapshot.clubLogoPath != null) {
            Optional<BufferedImage> opt = ImageIngester.load(snapshot.clubLogoPath, logoSize, logoSize);
            opt.ifPresent(img -> g.drawImage(img, padding, height - img.getHeight() - padding, null));
        }

        // QR code — bottom-right
        if (snapshot.qrCodePath != null) {
            Optional<BufferedImage> opt = ImageIngester.load(snapshot.qrCodePath, logoSize, logoSize);
            opt.ifPresent(img -> g.drawImage(img, width - img.getWidth() - padding, height - img.getHeight() - padding, null));
        }
    }

}
