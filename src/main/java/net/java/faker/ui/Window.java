/*
 * This file is part of faker - https://github.com/o1seth/faker
 * Copyright (C) 2024 o1seth
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.java.faker.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import net.java.faker.AppInfo;
import net.java.faker.Proxy;
import net.java.faker.proxy.dhcp.Dhcp;
import net.java.faker.ui.tab.*;
import net.java.faker.util.Sys;
import net.java.faker.util.Util;
import net.java.faker.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Window extends JFrame {
    private static Window INSTANCE;

    public static synchronized Window getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Window();
        }
        return INSTANCE;
    }

    public static final int BORDER_PADDING = 10;
    public static final int BODY_BLOCK_PADDING = 10;

    private final AnimatedBackgroundPanel backgroundPanel = new AnimatedBackgroundPanel();
    public final JTabbedPane contentPane = new JTabbedPane();
    private final JTextField searchField = new JTextField();
    private final JLabel searchFeedback = new JLabel();
    private final List<UITab> tabs = new ArrayList<>();
    private Color defaultTabForeground = UIManager.getColor("TabbedPane.foreground");
    private final Color accentColor = new Color(126, 255, 198);

    public final GeneralTab generalTab = registerTab(new GeneralTab(this));
    public final AdvancedTab advancedTab = registerTab(new AdvancedTab(this));
    public final AccountsTab accountsTab = registerTab(new AccountsTab(this));
    public final UISettingsTab uiSettingsTab = registerTab(new UISettingsTab(this));
    public final DHCPTab dhcpTab;

    private Window() {
        if (Sys.isWindows()) {
            dhcpTab = registerTab(new DHCPTab(this));
        } else {
            dhcpTab = null;
        }
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> showException(e));
        this.setLookAndFeel();
        this.initWindow();
        this.initTabs();
        this.configureSearch();
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(10_000);

        SwingUtilities.updateComponentTreeUI(this);

        this.reveal();
    }

    private void setLookAndFeel() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("TextComponent.arc", 18);
            UIManager.put("Component.arc", 18);
            UIManager.put("Button.arc", 20);
            UIManager.put("TabbedPane.tabArc", 20);
            UIManager.put("TabbedPane.selectedBackground", new Color(41, 45, 60));
            Color surface = new Color(34, 37, 48);
            Color background = new Color(24, 27, 36);
            Color textPrimary = new Color(222, 229, 240);
            UIManager.put("Panel.background", surface);
            UIManager.put("TabbedPane.background", surface);
            UIManager.put("TabbedPane.foreground", textPrimary);
            UIManager.put("TabbedPane.unselectedBackground", surface);
            UIManager.put("ScrollPane.background", surface);
            UIManager.put("Viewport.background", surface);
            UIManager.put("Label.foreground", textPrimary);
            UIManager.put("Component.focusColor", accentColor);
            UIManager.put("ComboBox.background", surface);
            UIManager.put("ComboBox.foreground", textPrimary);
            UIManager.put("List.background", background);
            UIManager.put("List.foreground", textPrimary);
        } catch (Throwable t) {
            Logger.error("Failed set look and feel", t);
        }
    }

    private void initWindow() {
        this.setTitle(AppInfo.NAME + " " + Proxy.VERSION);
        try {
            List<Image> icons = new ArrayList<>();
            Image icon32 = Util.getResourceImage(AppInfo.RESOURCE_ROOT + "/icon/icon_32.png");
            Image icon64 = Util.getResourceImage(AppInfo.RESOURCE_ROOT + "/icon/icon_64.png");
            if (icon32 != null) {
                icons.add(icon32);
            }
            if (icon64 != null) {
                icons.add(icon64);
            }
            this.setIconImages(icons);
        } catch (Exception ignored) {
        }

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                generalTab.applyGuiState();
                advancedTab.applyGuiState();
                Proxy.getConfig().save();
                if (!Proxy.isStarted() && !Dhcp.isStarted()) {
                    System.exit(0);
                }
            }
        });

        this.setSize(1100, 720);
        this.setMinimumSize(new Dimension(960, 620));
        this.setLocationRelativeTo(null);

        contentPane.setOpaque(false);
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        contentPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, "center");
        contentPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        contentPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ARC, 20);
        contentPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 48);
        contentPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_SELECTION_HEIGHT, 4);
        contentPane.putClientProperty(FlatClientProperties.TABBED_PANE_HAS_FULL_BORDER, false);
        contentPane.setFont(contentPane.getFont().deriveFont(Font.BOLD, 15f));
        contentPane.setForeground(new Color(209, 218, 234));

        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(new EmptyBorder(32, 32, 32, 32));

        JPanel overlay = new JPanel(new BorderLayout(24, 24));
        overlay.setOpaque(false);
        overlay.add(createHeaderPanel(), BorderLayout.NORTH);
        overlay.add(createContentPanel(), BorderLayout.CENTER);

        backgroundPanel.add(overlay, BorderLayout.CENTER);
        this.setContentPane(backgroundPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setOpaque(false);
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(AppInfo.NAME);
        title.setForeground(new Color(225, 229, 240));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 36f));

        JLabel subtitle = new JLabel("Adaptive proxy control inspired by ChatGPT aesthetics");
        subtitle.setForeground(new Color(152, 162, 184));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 16f));

        titleContainer.add(title);
        titleContainer.add(Box.createVerticalStrut(6));
        titleContainer.add(subtitle);

        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setOpaque(false);
        searchContainer.setPreferredSize(new Dimension(340, 54));

        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search features…");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchField.setOpaque(false);
        searchField.setForeground(new Color(220, 228, 238));
        searchField.setCaretColor(accentColor);
        searchField.setFont(searchField.getFont().deriveFont(Font.PLAIN, 16f));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(66, 74, 92), 1, true),
                new EmptyBorder(8, 14, 8, 14)));

        searchContainer.add(searchField, BorderLayout.CENTER);

        searchFeedback.setForeground(new Color(160, 170, 188));
        searchFeedback.setFont(searchFeedback.getFont().deriveFont(Font.PLAIN, 13f));
        searchFeedback.setBorder(new EmptyBorder(6, 2, 0, 0));
        updateSearchFeedback(null, 0);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BorderLayout());
        right.add(searchContainer, BorderLayout.NORTH);
        right.add(searchFeedback, BorderLayout.CENTER);

        header.add(titleContainer, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createContentPanel() {
        JPanel glass = new GlassPanel();
        glass.setLayout(new BorderLayout());
        glass.setBorder(new EmptyBorder(24, 24, 24, 24));
        glass.add(contentPane, BorderLayout.CENTER);
        return glass;
    }

    private void configureSearch() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleSearch(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleSearch(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleSearch(false);
            }
        });
        searchField.addActionListener(e -> handleSearch(true));
    }

    private void handleSearch(boolean forceSelect) {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        int matches = highlightMatchingTabs(query, forceSelect);
        updateSearchFeedback(query.isEmpty() ? null : query, matches);
    }

    private int highlightMatchingTabs(String query, boolean forceSelect) {
        int matches = 0;
        for (int i = 0; i < contentPane.getTabCount(); i++) {
            String title = contentPane.getTitleAt(i);
            boolean isMatch = query != null && !query.isEmpty() && title != null && title.toLowerCase(Locale.ROOT).contains(query);
            contentPane.setForegroundAt(i, isMatch ? accentColor : defaultTabForeground);
            if (isMatch) {
                matches++;
            }
        }
        if (matches == 0) {
            resetTabColors();
        }
        if ((forceSelect || !query.isEmpty()) && matches > 0) {
            for (int i = 0; i < contentPane.getTabCount(); i++) {
                String title = contentPane.getTitleAt(i);
                if (title != null && title.toLowerCase(Locale.ROOT).contains(query)) {
                    contentPane.setSelectedIndex(i);
                    break;
                }
            }
        }
        return matches;
    }

    private void resetTabColors() {
        for (int i = 0; i < contentPane.getTabCount(); i++) {
            contentPane.setForegroundAt(i, defaultTabForeground);
        }
    }

    private void updateSearchFeedback(String query, int matches) {
        if (query == null || query.isEmpty()) {
            searchFeedback.setText("Navigate tabs or search for a feature");
        } else if (matches == 0) {
            searchFeedback.setText("No features found for \"" + query + "\"");
        } else if (matches == 1) {
            searchFeedback.setText("Showing 1 matching feature for \"" + query + "\"");
        } else {
            searchFeedback.setText("Showing " + matches + " matching features for \"" + query + "\"");
        }
    }

    public void reveal() {
        if (!isVisible()) {
            setVisible(true);
        }
        if ((getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED) {
            setExtendedState(getExtendedState() & ~Frame.ICONIFIED);
        }
        toFront();
        requestFocus();
    }

    private <T extends UITab> T registerTab(UITab tab) {
        this.tabs.add(tab);
        return (T) tab;
    }

    private void initTabs() {
        for (UITab tab : this.tabs) {
            tab.add(this.contentPane);
        }

        if (contentPane.getTabCount() > 0) {
            defaultTabForeground = contentPane.getForegroundAt(0);
            resetTabColors();
        }

        this.contentPane.addChangeListener(e -> {
            int selectedIndex = contentPane.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < Window.this.tabs.size()) {
                Window.this.tabs.get(selectedIndex).onTabOpened();
            }
        });
    }

    public static void openURL(final String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Throwable t) {
            showInfo("generic.could_not_open_url " + url);
        }
    }

    public static void showException(final Throwable t) {
        Logger.error("Caught exception in thread " + Thread.currentThread().getName() + t);
        StringBuilder builder = new StringBuilder("An error occurred:\n");
        builder.append("[").append(t.getClass().getSimpleName()).append("] ").append(t.getMessage()).append("\n");
        for (StackTraceElement element : t.getStackTrace()) builder.append(element.toString()).append("\n");
        showError(builder.toString());
    }

    public static void showInfo(final String message) {
        showNotification(message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(final String message) {
        showNotification(message, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(final String message) {
        showNotification(message, JOptionPane.ERROR_MESSAGE);
    }

    public static void showNotification(final String message, final int type) {
        JOptionPane.showMessageDialog(Window.getInstance(), message, AppInfo.NAME, type);
    }

    public static int showDialog(JPanel panel) {
        return JOptionPane.showConfirmDialog(null, panel, AppInfo.NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private static class GlassPanel extends JPanel {
        private GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 36, 36);
            g2.setColor(new Color(29, 32, 46, 220));
            g2.fill(shape);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(255, 255, 255, 32));
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class AnimatedBackgroundPanel extends JPanel {
        private final Timer timer;
        private float hue = 0.58f;
        private float phase = 0f;

        private AnimatedBackgroundPanel() {
            setOpaque(true);
            timer = new Timer(40, e -> {
                hue += 0.0015f;
                phase += 0.01f;
                repaint();
            });
            timer.start();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            timer.start();
        }

        @Override
        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color top = new Color(20, 23, 33);
            Color bottom = new Color(14, 16, 24);
            GradientPaint gradient = new GradientPaint(0, 0, top, getWidth(), getHeight(), bottom);
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            float radius = Math.max(getWidth(), getHeight());
            float cx = (float) (Math.sin(phase) * getWidth() * 0.25 + getWidth() * 0.5);
            float cy = (float) (Math.cos(phase * 0.8) * getHeight() * 0.25 + getHeight() * 0.5);
            Color accent = Color.getHSBColor(hue % 1f, 0.45f, 0.45f);
            RadialGradientPaint paint = new RadialGradientPaint(new Point2D.Float(cx, cy), radius,
                    new float[]{0f, 1f},
                    new Color[]{new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110), new Color(0, 0, 0, 0)});
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.dispose();
        }
    }
}
